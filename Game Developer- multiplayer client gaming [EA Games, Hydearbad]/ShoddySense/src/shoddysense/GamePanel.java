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
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import shoddysenseserver.ImageSet;
import shoddysenseserver.NetMessage;

/**
 *
 * @author Cathy
 */
public class GamePanel extends JPanel {
    
    private GameBoardPanel[] m_boards;
    private JLabel[] m_labels;
    private JButton[] m_buttons;
    private GameLobby m_lobby;
    private boolean[] m_inactive;
    private String[] m_players;
    private int m_turn = -1, m_idx = -1;
    private int m_height = 0;
    private int m_hash = -1;
    
    /** Creates a new instance of GamePanel */
    public GamePanel(GameLobby lobby, int hash, int idx, final String[] players) {
        m_lobby = lobby;
        m_idx = idx;
        m_players = players;
        m_hash = hash;
        setOpaque(false);
        
        m_boards = new GameBoardPanel[players.length];
        m_labels = new JLabel[players.length];
        m_buttons = new JButton[players.length];
        m_inactive = new boolean[players.length];
        setLayout(null);
        for (int i = 0; i < players.length; ++i) {
            int y = 205 * i + 5;
            m_height = y + 250;
            
            boolean[][] data;
            if (i != idx) {
                data = new boolean[10][10];
            } else {
                ServerLink server = lobby.getServer();
                data = ImageSet.getDataFromImage(server.getImages().getImage(server.getImage(hash)));
            }
            m_boards[i] = new GameBoardPanel(data, new GameBoardListener() {
                    public void spaceClicked(int x, int y) {
                        if (m_turn != m_idx) return;
                        
                        boolean smart = false;
                        for (int i = 0; i < m_boards.length; ++i) {
                            if (i == m_idx) continue;
                            if (!m_boards[i].isAttacked(x, y)) {
                                smart = true;
                                break;
                            }
                        }
                        if (!smart) {
                            return;
                        }
                        
                        ServerLink link = m_lobby.getServer();
                        Integer obj[] = { new Integer(x), new Integer(y) };
                        link.sendMessage(new NetMessage(NetMessage.TARGET, m_hash, obj));
                    }
                });
            m_boards[i].setHighlighting(false);
            m_boards[i].setVisible(true);
            m_boards[i].setSize(200, 200);
            m_boards[i].setLocation(200, y);
            add(m_boards[i]);
            
            JLabel label = m_labels[i] = new JLabel(players[i]);
            label.setFont(label.getFont().deriveFont(24f));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVisible(true);
            label.setLocation(0, y);
            label.setSize(200, 200);
            add(label);
            
            if (i == idx) {
                continue;
            }
            
            ImageSelector selector = new ImageSelector(m_lobby.getServer().getImages(),
                    new ImageSelectListener() {
                        public void choseImage(ImageSelector selector, String file) {
                            selector.toggleCross(selector.getIdxFromFile(file));
                            selector.repaint();
                        }
                    }
                );
            selector.setVisible(true);
            selector.setSize(200, 200);
            selector.setLocation(200 + 220, y);
            selector.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            add(selector);
            
            JButton button = m_buttons[i] = new JButton("Guess");
            button.setVisible(true);
            final int fi = i;
            button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        if (m_turn != m_idx) return;
                        
                        final ServerLink server = m_lobby.getServer();
                        ChooseImage chooser = new ChooseImage(server,
                                server.getImages(),
                                new ImageSelectListener() {
                                    public void choseImage(ImageSelector selector, String str) {
                                        server.sendMessage(
                                                new NetMessage(NetMessage.GUESS,
                                                m_hash,
                                                new Object[] {
                                                    players[fi], str }));
                                    }
                                }
                            );
                        chooser.setText("Choose the image that " + players[fi] + " has.");
                        chooser.setVisible(true);
                    }
                });
            button.setLocation(50, y + 140);
            button.setSize(100, 30);
            add(button);
        }
    }
    
    public void deactivatePlayer(String name) {
        for (int i = 0; i < m_players.length; ++i) {
            if (m_players[i].equals(name)) {
                m_inactive[i] = true;
                break;
            }
        }
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(650, m_height);
    }
    
    public void updateData(int idx, int x, int y, boolean[] hit) {
        for (int i = 0; i < m_boards.length; ++i) {
            if (i == idx) {
                continue;
            }
            boolean[][] data = m_boards[i].getData();
            data[y][x] = hit[i];
            if ((i == m_idx) || !hit[i]) {
                m_boards[i].setAttacked(x, y, true);
            }
            m_boards[i].repaint();
        }
    }
    
    public void setTurn(int idx) {
        m_turn = idx;
        boolean active = (idx == m_idx);
        for (int i = 0; i < m_labels.length; ++i) {
            boolean player = (i == idx);
            Color c = (player ? Color.RED : Color.BLACK);
            m_labels[i].setForeground(c);
            m_boards[i].setHighlighting(active && !player);
            if (m_buttons[i] != null) {
                m_buttons[i].setEnabled(active && !m_inactive[i]);
            }
        }
    }
    
}
