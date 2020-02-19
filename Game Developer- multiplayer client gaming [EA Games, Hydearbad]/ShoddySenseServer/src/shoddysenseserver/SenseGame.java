/**
 * This file is part of Shoddy Sense.
 * Copyright (C) 2007 Cathy Fitzpatrick <cathy@cathyjf.com>
 * Created in March 2007.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package shoddysenseserver;
import java.util.*;
import java.awt.image.BufferedImage;
import java.awt.Component;

/**
 *
 * @author Cathy
 */
public class SenseGame {
    
    private SenseServer m_server;
    private HashMap m_map = new HashMap();
    private HashMap m_data = new HashMap();
    private int m_players = 0, m_expectation = -1;
    private int m_turn = -1;
    private SensePlayer[] m_clients;
    private boolean m_finished = false;
    
    /** Creates a new instance of SenseGame */
    public SenseGame(SenseServer server, ImageSet images, int expect) {
        m_expectation = expect;
        m_server = server;
        Set s = images.getFiles();
        Collection c = images.getImages();
        Iterator i = s.iterator(), j = c.iterator();
        while (i.hasNext()) {
            String file = (String)i.next();
            boolean[][] data = ImageSet.getDataFromImage((BufferedImage)j.next());
            m_map.put(file, data);
        }
    }
    
    /**
     * A player in ShoddySense.
     */
    private class SensePlayer {
        private boolean m_active;
        public boolean[][] data;
        public String image;
        public NetClient client;
        
        public SensePlayer(NetClient c, String img, boolean[][] pData) {
            image = img;
            m_active = true;
            client = c;
            data = pData;
            /**data = new boolean[pData.length][pData[0].length];
            for (int i = 0; i < data.length; ++i) {
                boolean[] row = data[i];
                for (int j = 0; j < row.length; ++j) {
                    row[j] = pData[i][j];
                }
            }**/
        }
        
        public boolean isActive() {
            return m_active;
        }
        
        public void remove() {
            m_active = false;
            m_server.broadcast(new NetMessage(NetMessage.INFORM_LOSS,
                    SenseGame.this.hashCode(),
                    new Object[] { client.getUserName() }));
            checkWin();
        }
    }
    
    /**
     * Add a player to the game.
     */
    public boolean addPlayer(NetClient client, String image) {
        Object obj = m_map.get(image);
        if (obj == null) {
            return false;
        }
        String name = client.getUserName();
        if (m_data.get(name) != null) {
            return false;
        }
        m_data.put(name, new SensePlayer(client, image, (boolean[][])obj));
        System.out.println(m_players + 1);
        if (++m_players == m_expectation) {
            System.out.println("Starting...");
            startGame();
        }
        return true;
    }
    
    /**
     * Remove a player from the game.
     */
    public void removePlayer(NetClient client) {
        Object obj = m_data.get(client.getUserName());
        if (obj != null) {
            ((SensePlayer)obj).remove();
        }
    }
    
    /**
     * Start the game.
     */
    public void startGame() {
        m_players = m_data.size();
        Collection c = m_data.values();
        m_clients = (SensePlayer[])c.toArray(new SensePlayer[c.size()]);
        String[] players = new String[m_clients.length];
        for (int i = 0; i < m_clients.length; ++i) {
            players[i] = m_clients[i].client.getUserName();
        }
        for (int i = 0; i < m_clients.length; ++i) {
            m_clients[i].client.startGame(this, i, players);
        }
        setTurn(0);
    }
    
    /**
     * Get the current player's id.
     */
    public int getCurrentTurn() {
        return m_turn;
    }
    
    /**
     * Check for a win.
     */
    public void checkWin() {
        String client = null;
        for (int i = 0; i < m_players; ++i) {
            if (m_clients[i].isActive()) {
                if (client != null) {
                    return;
                }
                client = m_clients[i].client.getUserName();
            }
        }
        if ((client != null) && !m_finished) {
            m_server.broadcast(new NetMessage(NetMessage.INFORM_WIN,
                    hashCode(),
                    new Object[] { client }));
            m_finished = true;
        }
    }
    
    /**
     * End the current player's turn.
     */
    public void endTurn() {
        for (int i = m_turn + 1; i < m_players + m_turn; ++i) {
            int pos = -1;
            if (i < m_players) {
                pos = i;
            } else {
                pos = i - m_players;
            }
            if (m_clients[pos].isActive()) {
                setTurn(pos);
                break;
            }
        }
    }
    
    /**
     * Process a guess from the active player.
     */
    public void processGuess(String opponent, String image) {
        SensePlayer player = (SensePlayer)m_data.get(opponent);
        if (player == null) {
            return;
        }
        if (player.image.equals(image)) {
            player.remove();
        } else {
            player = m_clients[m_turn];
            player.remove();
            endTurn();
        }
    }
    
    /**
     * Set the active turn.
     */
    private void setTurn(int idx) {
        m_turn = idx;
        m_server.broadcast(new NetMessage(NetMessage.SET_TURN, hashCode(),
                new Object[] { new Integer(idx) }));
    }
    
    /**
     * Process a target from the active player.
     */
    public void processTarget(int x, int y) {
        System.out.println("Processing " + y + ", " + x);
        boolean[] result = new boolean[m_clients.length];
        for (int i = 0; i < m_clients.length; ++i) {
            if (i == m_turn) continue;

            try {
                result[i] = m_clients[i].data[y][x];
                System.out.println("Client " + i + ": " + result[i]);
            } catch (ArrayIndexOutOfBoundsException e) {
                
            }
        }
        m_server.broadcast(new NetMessage(NetMessage.TARGET, hashCode(),
                new Object[] { new Integer(x), new Integer(y), new Integer(m_turn), result }));
        endTurn();
    }
}
