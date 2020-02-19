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
import java.util.*;
import java.awt.event.*;
import shoddysenseserver.ImageSet;

/**
 *
 * @author Cathy
 */
public class ImageSelector extends JPanel {
    
    /**
     * Width and height to use for each image.
     */
    private int m_width = 20, m_height = 20;
    
    /**
     * Spacing between each image.
     */
    private int m_spacingX = 5, m_spacingY = 5;
    
    /**
     * Effective height and width of each image.
     */
    private int m_effWidth = m_width + m_spacingX;
    private int m_effHeight = m_height + m_spacingY;
    
    /**
     * Set of images.
     */
    private ImageSet m_images;
    
    /**
     * Whether each image is crossed out.
     */
    private boolean[] m_crossed;
    
    /**
     * The image to highlight.
     */
    private int m_highlight = -1;
    
    /** Creates a new instance of ImageSelector */
    public ImageSelector(ImageSet set, final ImageSelectListener listener) {
        setOpaque(false);
        
        m_images = set;
        set.loadImages(this, m_width, m_height);
        m_crossed = new boolean[m_images.getImages().size()];
        
        addMouseListener(new MouseListener() {
                public void mouseExited(MouseEvent e) {
                    m_highlight = -1;
                    repaint();
                }
                public void mouseEntered(MouseEvent e) { }
                public void mouseReleased(MouseEvent e) { }
                public void mousePressed(MouseEvent e) { }
                public void mouseClicked(MouseEvent e) {
                    int idx = getIdx(e);
                    Set s = m_images.getFiles();
                    if ((idx != -1) && (idx < s.size())) {
                        String file = (String)s.toArray(new String[s.size()])[idx];
                        listener.choseImage(ImageSelector.this, file);
                    }
                }
            });

        addMouseMotionListener(new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) { }
                public void mouseMoved(MouseEvent e) {
                    m_highlight = getIdx(e);
                    repaint();
                }
            });
    }
    
    /**
     * Get the index of a file.
     */
    public int getIdxFromFile(String file) {
        Set s = m_images.getFiles();
        String[] files = (String[])s.toArray(new String[s.size()]);
        for (int i = 0; i < files.length; ++i) {
            if (files[i].equals(file)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Toggle the display of a cross over one of the images.
     */
    public void toggleCross(int idx) {
        m_crossed[idx] = !m_crossed[idx];
    }
    
    private int getIdx(MouseEvent e) {
        int x = e.getX() / m_effWidth;
        int modX = e.getX() % m_effWidth;
        int y = e.getY() / m_effHeight;
        int modY = e.getY() % m_effHeight;
        if ((modX <= m_width) && (modY <= m_height)) {
            int columns = getWidth() / m_effWidth;
            return y * columns + x;
        }
        return -1;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Collection c = m_images.getImages();
        int images = c.size();
        int columns = getWidth() / m_effWidth;
        
        Iterator itr = c.iterator();
        int idx = -1;
        for (int i = 0; ; ++i) {
            int y = i * m_effHeight;
            if (y > getHeight()) {
                return;
            }
            for (int j = 0; j < columns; ++j) {
                if (!itr.hasNext()) {
                    return;
                }
                int x = j * m_effWidth;
                g.drawImage((Image)itr.next(), x, y, m_width, m_height, this);
                if (++idx == m_highlight) {
                    g.setColor(Color.ORANGE);
                    g.drawRect(x, y, m_width, m_height);
                }
                if (m_crossed[idx]) {
                    g.setColor(Color.RED);
                    g.drawLine(x, y, x + m_width, y + m_height);
                    g.drawLine(x + m_width, y, x, y + m_height);
                }
            }
        }
    }
    
}
