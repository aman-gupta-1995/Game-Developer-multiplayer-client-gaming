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
import java.net.*;
import java.io.*;

/**
 * Processes input from a socket.
 * @author Cathy
 */
public abstract class MessageHandler extends Thread {
    
    protected InputStream m_input;
    protected OutputStream m_output;
    protected boolean m_running = true;
    
    /**
     * Prevents creation of MessageHandler externally.
     */
    protected MessageHandler() {
    }
    
    /**
     * Exexcute a message.
     */
    protected abstract void executeMessage(NetMessage msg);
    
    /**
     * Inform of a read error.
     */
    protected abstract void informReadError(Throwable e);
    
    /**
     * Send a message.
     */
    public void sendMessage(NetMessage message) {
        try {
            ObjectOutputStream obj = new ObjectOutputStream(m_output);
            obj.writeObject(message);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Pull a message off the queue.
     */
    public NetMessage getNextMessage() throws IOException, ClassNotFoundException {
        ObjectInputStream obj = new ObjectInputStream(m_input);
        return (NetMessage)obj.readObject();
    }
    
    public void run() {
        while (m_running) {
            try {
                executeMessage(getNextMessage());
            } catch (Throwable e) {
                e.printStackTrace();
                informReadError(e);
            }
        }
    }
    
}
