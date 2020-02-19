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
import java.util.zip.*;
import java.util.Enumeration;
import java.io.*;
import java.util.TreeMap;
import java.util.Set;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.util.Collection;
import java.util.Iterator;
import java.awt.*;
import java.net.*;
import java.util.jar.*;

/**
 *
 * @author Cathy
 */
public class ImageSet {
    
    /**
     * A map of file names to BufferedImages.
     */
    private TreeMap m_map = new TreeMap();
    
    /**
     * Constructs a new ImageSet based on a file name.
     * @throws IOExecption if the file could not be opened
     */
    public ImageSet(String file) throws IOException {
        String path = "jar:" + file + "!/";
        JarURLConnection url = null;
        url = (JarURLConnection)new URL(path).openConnection();
        
        if (url == null)
            return;
        
        url.connect();
        JarFile zip = url.getJarFile();
        Enumeration e = zip.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)e.nextElement();
            String name = entry.getName();
            BufferedImage image = ImageIO.read(zip.getInputStream(entry));
            m_map.put(name, image);
        }
    }
    
    /**
     * Get an array of data by reading a BufferedImage. Black is treated as
     * an "unset" position; everything else is considered set.
     */
    public static boolean[][] getDataFromImage(BufferedImage image) {
        boolean[][] data = new boolean[image.getWidth()][image.getHeight()];
        for (int i = 0; i < data.length; ++i) {
            boolean[] column = data[i];
            for (int j = 0; j < data.length; ++j) {
                column[j] = (image.getRGB(j, i) == Color.BLACK.getRGB());
            }
        }
        return data;
    }
    
    /**
     * Wait until all the images in this set are loaded.
     */
    public void loadImages(Component c, int width, int height) {
        MediaTracker tracker = new MediaTracker(c);
        Iterator i = m_map.values().iterator();
        int idx = 0;
        while (i.hasNext()) {
            BufferedImage image = (BufferedImage)i.next();
            tracker.addImage(image, idx++, width, height);
        }
        try {
            tracker.waitForAll();
        } catch (/*Interrupted*/Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get a list files in this image set.
     */
    public Set getFiles() {
        return m_map.keySet();
    }
    
    /**
     * Get a list of images in this image set.
     */
    public Collection getImages() {
        return m_map.values();
    }
    
    /**
     * Get a buffered image by file name.
     */
    public BufferedImage getImage(String file) {
        return (BufferedImage)m_map.get(file);
    }
    
}
