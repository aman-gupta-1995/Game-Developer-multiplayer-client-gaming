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

package shoddysense;
import java.net.*;
import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;
import java.security.*;
import javax.swing.*;
import shoddysenseserver.ImageSet;
import shoddysenseserver.MessageHandler;
import shoddysenseserver.NetMessage;

/**
 *
 * @author Cathy
 */
public class ServerLink extends MessageHandler {
    
    private Socket m_server;
    private ImageSet m_images;
    private GameLobby m_lobby;
    private HashMap m_map = new HashMap();
    
    /** Creates a new instance of ServerLink */
    public ServerLink(String host, int port, String user) throws IOException, UnknownHostException, ClassNotFoundException {
        System.out.println("About to create a client socket.");
        m_server = new Socket(InetAddress.getByName(host), port);
        System.out.println("About to open an output stream.");
        m_output = m_server.getOutputStream();
        System.out.println("About to open an input stream.");
        m_input = m_server.getInputStream();
        
        System.out.println("About to pull a message off of the queue.");
        NetMessage msg = getNextMessage();
        System.out.println("About to cast the first member of the data array to a string.");
        String images = (String)msg.getData()[0];
        
        System.out.println("About to send a message with our user name to the server.");
        sendMessage(new NetMessage(NetMessage.JOIN, -1, new Object[] { user }));
        
        System.out.println("About to read the pixels of fifty images into boolean[][] arrays.");
        m_images = new ImageSet(images);
        System.out.println("About to create the game lobby.");
        m_lobby = new GameLobby(ServerLink.this);
        System.out.println("About to show the game lobby.");
        m_lobby.setVisible(true);
        
        System.out.println("About to start the message thread.");
        start();
        System.out.println("Started.");
    }
    
    public String getImage(int game) {
        return (String)m_map.get(new Integer(game));
    }
    
    public ImageSet getImages() {
        return m_images;
    }
    
    protected void executeMessage(final NetMessage raw) {
        switch (raw.getMessage()) {
            case NetMessage.UPDATE_LIST:
                m_lobby.setPlayers((String[])raw.getData());
                break;
                
            case NetMessage.CHAT:
                m_lobby.addMessage((String)raw.getData()[0]);
                break;
                
            case NetMessage.BEGIN: {
                int idx = ((Integer)raw.getData()[0]).intValue();
                int hash = ((Integer)raw.getData()[1]).intValue();
                String[] players = (String[])raw.getData()[2];
                m_lobby.startGame(idx, hash, players);
            } break;
                
            case NetMessage.TARGET: {
                Object[] o = raw.getData();
                int x = ((Integer)o[0]).intValue();
                int y = ((Integer)o[1]).intValue();
                int idx = ((Integer)o[2]).intValue();
                boolean hit[] = (boolean[])o[3];
                
                m_lobby.getGame(raw.getGame()).updateData(idx, x, y, hit);
            } break;
            
            case NetMessage.SET_TURN:
                m_lobby.getGame(raw.getGame()).setTurn(((Integer)raw.getData()[0]).intValue());
                break;
                
            case NetMessage.INFORM_WIN:
                JOptionPane.showMessageDialog(null, (String)raw.getData()[0] + " has won!");
                break;
                
            case NetMessage.INFORM_LOSS: {
                String name = (String)raw.getData()[0];
                JOptionPane.showMessageDialog(null, name + " has lost!");
                m_lobby.getGame(raw.getGame()).deactivatePlayer(name);
            } break;
            
            case NetMessage.CHOICE: {
                new ChooseImage(this, m_images, new ImageSelectListener() {
                        public void choseImage(ImageSelector selector, String image) {
                            int hash = raw.getGame();
                            m_map.put(new Integer(hash), image);
                            sendMessage(new NetMessage(NetMessage.CHOICE,
                                    hash,
                                    new Object[] { image }));
                        }
                    }).setVisible(true);
            } break;
        }
    }
    
    protected void informReadError(Throwable e) {
        if (!m_running) {
            return;
        }
        
        String err =
                "An error occurred while reading a message from the server: "
                + e.getMessage()
                + ".\n\nDisconnect from the server? (Try again at least once.)";
        
        int result = JOptionPane.showConfirmDialog(
                null,
                err,
                "Error",
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            m_running = false;
            System.exit(0);
        }
    }
    
}
