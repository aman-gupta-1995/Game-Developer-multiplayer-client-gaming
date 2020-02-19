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
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class represents an online client.
 * @author Cathy
 */
public class NetClient extends MessageHandler {
    
    private Socket m_client;
    private SenseServer m_server;
    private String m_name = "";
    private int m_idx = -1;
    
    /** Creates a new instance of NetClient */
    public NetClient(SenseServer server, Socket socket) throws IOException {
        m_server = server;
        m_client = socket;
        m_input = m_client.getInputStream();
        m_output = m_client.getOutputStream();
    }
    
    public void close() {       
        if (m_name.length() != 0) {
            // TODO: Remove the client from his or her games.

        }

        System.out.println("Client disconnected: "
                + m_client.getInetAddress().getHostName()
                + ".");
        
        try {
            m_running = false;
            m_client.close();
        } catch (IOException e) {
            
        }
    }
    
    public int hashCode() {
        return m_name.hashCode();
    }
    
    public String getUserName() {
        return m_name;
    }
    
    protected void informReadError(Throwable e) {
        if (m_running) {
            System.out.println("informReadError: " + e.getMessage());
            m_server.removeClient(this);
            m_server.updateUserList();
        }
    }
    
    public void startGame(SenseGame game, int idx, String[] players) {
        m_idx = idx;
        int hash = game.hashCode();
        sendMessage(new NetMessage(NetMessage.BEGIN, hash,
                new Object[] { new Integer(idx), new Integer(hash), players }));
    }
    
    public int getIdx() {
        return m_idx;
    }
    
    public void run() {
        try {
            NetMessage raw = (NetMessage)getNextMessage();
            if (raw.getMessage() == NetMessage.JOIN) {
                String name = (String)raw.getData()[0];
                name = name.trim();
                if (!m_server.isLoggedOn(name)) {
                    m_name = name;
                }
            }
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
            
        }
        
        if (m_name.length() == 0) {
            m_server.removeClient(this);
            return;
        }
        
        
        m_server.updateUserList();

        m_server.broadcast(new NetMessage(NetMessage.CHAT, -1, new Object[] {
                m_name + " has joined the server."
            }));
        
        super.run();
    }
    
    public void sendMessage(NetMessage msg) {
        super.sendMessage(msg);
    }
    
    protected void executeMessage(NetMessage raw) {
        switch (raw.getMessage()) {
            case NetMessage.CHAT: {
                String message = ((String)raw.getData()[0]).trim();
                if (message.length() != 0) {
                    m_server.broadcast(new NetMessage(NetMessage.CHAT, -1,
                            new Object[] { m_name + ": " + message }));
                }
            } break;
            
            
            
            case NetMessage.CHOICE:
                m_server.getGame(raw.getGame()).addPlayer(this, (String)raw.getData()[0]);
                break;
                
            case NetMessage.TARGET: {
                SenseGame game = m_server.getGame(raw.getGame());
                if (game.getCurrentTurn() == m_idx) {
                    Integer[] obj = (Integer[])raw.getData();
                    game.processTarget(obj[0].intValue(), obj[1].intValue());
                }
            } break;
            
            case NetMessage.GUESS: {
                SenseGame game = m_server.getGame(raw.getGame());
                if (game.getCurrentTurn() == m_idx) {
                    Object[] o = raw.getData();
                    String player = (String)o[0];
                    String file = (String)o[1];
                    game.processGuess(player, file);
                }
            } break;
            
            case NetMessage.BEGIN: {
                try {
                    Object[] o = raw.getData();
                    SenseGame game = new SenseGame(m_server, new ImageSet(m_server.getImages()), o.length);
                    int hash = game.hashCode();
                    m_server.putGame(hash, game);
                    for (int i = 0; i < o.length; ++i) {
                        String s = (String)o[i];
                        m_server.getClientByName(s).sendMessage(new NetMessage(NetMessage.CHOICE, hash, null));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } break;
        }
    }
    
}
