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

/**
 *
 * @author Cathy
 */
public class NetMessage implements Serializable {
    
    public static final int JOIN = 0;
    public static final int CHOICE = 1;
    public static final int CHAT = 2;
    public static final int BEGIN = 3;
    public static final int TARGET = 4;
    public static final int GUESS = 5;
    public static final int INFORM_LOSS = 6;
    public static final int INFORM_WIN = 7;
    public static final int UPDATE_LIST = 8;
    public static final int SET_TURN = 9;
    
    private int m_message, m_game;
    private Object[] m_data;
    
    public NetMessage(int message, int game, Object[] data) {
        m_message = message;
        m_data = data;
        m_game = game;
    }
    
    public int getMessage() {
        return m_message;
    }
    
    public int getGame() {
        return m_game;
    }
    
    public Object[] getData() {
        return m_data;
    }
}