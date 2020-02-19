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
import java.net.*;
import java.io.*;

/**
 *
 * @author Cathy
 */
public class SenseServer implements Runnable {
    
    private List m_clients = Collections.synchronizedList(new ArrayList());
    private int m_port;
    private String m_images;
    private HashMap m_games = new HashMap();
    
    /** Creates a new instance of BattleServer */
    public SenseServer(int port, String images) {
        m_port = port;
        m_images = images;
    }
    
    public String getImages() {
        return m_images;
    }
    
    /**
     * Get an instance of SenseGame.
     */
    public SenseGame getGame(int hash) {
        return (SenseGame)m_games.get(new Integer(hash));
    }
    
    public void putGame(int hash, SenseGame game) {
        m_games.put(new Integer(hash), game);
    }
    
    /**
     * Run the server.
     */
    public void run() {
        System.out.println("Opening a server socket on port " + m_port + "...");
        ServerSocket server = null;
        try {
            server = new ServerSocket(m_port);
        } catch (IOException e) { }
        
        if (server == null) {
            return;
        }
        
        NetMessage msg = new NetMessage(NetMessage.JOIN, -1, new Object[] { m_images });
        
        System.out.println("Server is running.");
        while (true) {
            try {
                Socket socket = server.accept();
                NetClient client = new NetClient(this, socket);
                m_clients.add(client);
                client.sendMessage(msg);
                client.start();
                System.out.println("Accepted new client: "
                        + socket.getInetAddress().getHostName()
                        + ".");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
    /**
     * Get a list of user names.
     */
    public String[] getClientNames() {
        synchronized (m_clients) {
            String[] ret = new String[m_clients.size()];
            Iterator i = m_clients.iterator();
            int j = 0;
            while (i.hasNext()) {
                String name = ((NetClient)i.next()).getUserName();
                ret[j++] = name;
            }
            return ret;
        }
    }
    
    /**
     * Get a client by his user name.
     */
    public NetClient getClientByName(String name) {
        synchronized (m_clients) {
            Iterator i = m_clients.iterator();
            while (i.hasNext()) {
                NetClient client = ((NetClient)i.next());
                if (name.equalsIgnoreCase(client.getUserName())) {
                    return client;
                }
            }
            return null;
        }
    }
    
    /**
     * Determine whether a user is logged on.
     */
    public boolean isLoggedOn(String name) {
        return (getClientByName(name) != null);
    }
    
    /**
     * Remove a client.
     */
    public void removeClient(NetClient client) {
        client.close();
        m_clients.remove(client);
        updateUserList();
    }
    
    /**
     * Update the user name list.
     */
    public void updateUserList() {
        broadcast(new NetMessage(NetMessage.UPDATE_LIST, -1, getClientNames()));
    }
    
    /**
     * Read input from stdin.
     */
    public void readInput() {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                String line = input.readLine().trim();
                if (line.length() == 0) {
                    continue;
                }
                String[] parts = line.split(" +");
                String command = parts[0].substring(1);
                if (command.equals("start")) {
                    // Start the game.
                    //m_game.startGame();
                } else if (command.equals("wall")) {
                    // Output chat text.
                    StringBuffer buffer = new StringBuffer();
                    for (int i = 1; i < parts.length; ++i) {
                        buffer.append(parts[i]);
                        if ((i + 1) != parts.length) {
                            buffer.append(" ");
                        }
                    }
                    broadcast(new NetMessage(NetMessage.CHAT, -1, new Object[] { new String(buffer) }));
                } else {
                    System.out.println("No such command: " + command);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Broadcast a message to all clients.
     */
    public void broadcast(NetMessage msg) {
        synchronized (m_clients) {
            Iterator i = m_clients.iterator();
            while (i.hasNext()) {
                NetClient client = (NetClient)i.next();
                client.sendMessage(msg);
            }
        }
    }
    
}
