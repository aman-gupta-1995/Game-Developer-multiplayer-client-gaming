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
import java.awt.image.BufferedImage;

/**
 * This class represents the Sixth Sense game board. It is a grid in which
 * each square is either on or off; the data is loaded from a boolean[][].
 * When a square is clicked, the object calls a specific method in a
 * GameBoardListener. As the mouse hovers over the board, squares light up.
 * The component is drawn to use all the space that has been assigned to it.
 *
 * @author Cathy
 */
public class GameBoardPanel extends JPanel {
    
    /**
     * The game board data to display.
     */
    private boolean m_data[][];
    private boolean m_attacked[][];
    
    /**
     * The point to highlight.
     */
    private Point m_highlight;
    
    /**
     * Whether highlighting is enabled.
     */
    private boolean m_bHighlight = true;
    
    /** Creates a new instance of GameBoardPanel */
    public GameBoardPanel(boolean[][] data, final GameBoardListener listener) {
        setData(data);
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        addMouseListener(new MouseListener() {
                public void mouseExited(MouseEvent e) {
                    setHighlightPoint(null);
                    repaint();
                }
                public void mouseEntered(MouseEvent e) { }
                public void mouseReleased(MouseEvent e) { }
                public void mousePressed(MouseEvent e) { }
                public void mouseClicked(MouseEvent e) {
                    Point p = getSpace(e);
                    int x = (int)p.getX();
                    int y = (int)p.getY();
                    if ((x < m_data.length) && (y < m_data[0].length)) {
                        listener.spaceClicked(x, y);
                    }
                }
            });

        addMouseMotionListener(new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) { }
                public void mouseMoved(MouseEvent e) {
                    setHighlightPoint(getSpace(e));
                    repaint();
                }
            });
        setOpaque(true);
    }
    
    /**
     * Set the point to highlight for all GameBoardPanels in this container.
     */
    private void setHighlightPoint(Point p) {
        m_highlight = p;
        repaint();
        Container parent = getParent();
        if (parent == null) {
            return;
        }
        Component[] comps = parent.getComponents();
        for (int i = 0; i < comps.length; ++i) {
            if (comps[i] instanceof GameBoardPanel) {
                GameBoardPanel panel = (GameBoardPanel)comps[i];
                panel.m_highlight = p;
                panel.repaint();
            }
        }
    }
    
    /**
     * Change the set of data.
     */
    public void setData(boolean[][] data) {
        m_data = data;
        if (m_data != null) {
            m_attacked = new boolean[m_data.length][m_data[0].length];
        }
    }
    
    /**
     * Attack a space.
     */
    public void setAttacked(int x, int y, boolean attacked) {
        m_attacked[y][x] = attacked;
    }
    
    /**
     * Return whether a space has been attacked.
     */
    public boolean isAttacked(int x, int y) {
        return m_attacked[y][x];
    }
    
    /**
     * Get the underlying data.
     */
    public boolean[][] getData() {
        return m_data;
    }
    
    /**
     * Set whether highlighting is enabled.
     */
    public void setHighlighting(boolean enabled) {
        m_bHighlight = enabled;
    }
    
    /**
     * Get the space based on a mouse event.
     */
    private Point getSpace(MouseEvent e) {
        if (m_data == null) {
            return new Point(-1, -1);
        }
        
        int x = e.getX() / (getWidth() / m_data.length);
        int y = e.getY() / (getHeight() / m_data[0].length);
        return new Point(x, y);
    }
    
    /**
     * Draw a grid with coloured squares to represent the game board.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (m_data == null) {
            return;
        }
        
        int width = getWidth() / m_data.length;
        int height = getHeight() / m_data[0].length;
        
        boolean consider = ((m_highlight != null) && m_bHighlight);
        
        int hx = consider ? (int)m_highlight.getX() : -1;
        int hy = consider ? (int)m_highlight.getY() : -1;
        
        for (int i = 0; i < m_data.length; ++i) {
            int y = i * height;
            boolean[] row = m_data[i];
            for (int j = 0; j < row.length; ++j) {
                int x = j * width;
                boolean highlight = ((j == hx) && (i == hy));
                if (highlight) {
                    g.setColor(Color.ORANGE);
                } else if (row[j]) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(x, y, width, height);
            }
            System.out.println();
        }
        
        /**
         * Draw in a grey grid.
         */
        g.setColor(Color.GRAY);
        for (int i = 0; i < m_data.length; ++i) {
            int y = i * height;
            g.drawLine(0, y, getWidth(), y);
            boolean[] row = m_data[i];
            for (int j = 0; j < row.length; ++j) {
                int x = j * width;
                g.drawLine(x, 0, x, getHeight());
                if (m_attacked[i][j]) {
                    g.drawLine(x, y, x + width, y + width);
                    g.drawLine(x + width, y, x, y + width);
                }
            }
        }
    }
    
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
}
