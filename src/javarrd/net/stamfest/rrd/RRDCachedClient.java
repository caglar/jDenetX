
/*

 Copyright (c) 2010 by Peter Stamfest <peter@stamfest.at>

 This file is part of java-rrd.

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 Except as contained in this notice, the name of Peter Stamfest shall not 
 be used in advertising or otherwise to promote the sale, use or other 
 dealings in this Software without prior written authorization from 
 Peter Stamfest.

*/


package javarrd.net.stamfest.rrd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Because Java does not support UNIX Sockets, this can only be 
 * used with rrdcached listening on a TCP socket.
 * 
 * @author peter
 *
 */
public class RRDCachedClient implements RRDUpdate {
    private Socket socket;
    private OutputStream writer;
    private InputStream reader;
    private String host;
    private int port; 
    
    public RRDCachedClient(String host, int port) throws UnknownHostException, IOException {
	init(host, port);
    }
    
    private void init(String host, int port) throws UnknownHostException, IOException {
	this.host = host;
	this.port = port;
	socket = new Socket(host, port);
	socket.setKeepAlive(true);
	socket.setSoTimeout(60000);
	writer = socket.getOutputStream();
	reader = socket.getInputStream();
    }

    private void reconnect() throws UnknownHostException, IOException {
	try {
	    writer = null;
	    reader = null;
	    socket.close();
	} catch (Exception ignore) {
	}
	init(host, port);
    }
    
    private String readLine() throws IOException {
	int c;
	byte b[] = new byte[128];
	int pos = 0;
	
	while (true) {
	    c = reader.read();
	    if (c == -1 || c == '\n') break;
	    if (pos == b.length) {
		b = Arrays.copyOf(b, b.length * 2);
	    }
	    b[pos++] = (byte) c;
	}
	
	return new String(b, 0, pos);
    }

    private static Pattern response = Pattern.compile("^(-?\\d+) (.*)$");
    private synchronized CommandResult sendCommand(String cmd1[], 
                                                   String cmd2[]) throws Exception {
	if (writer == null) {
	    reconnect();
	}
	
	CommandResult r = new CommandResult();
	
	StringBuffer sb = new StringBuffer();
	
	if (cmd1 != null) {
	    for (int i = 0 ; i < cmd1.length ; i++) {
		if (sb.length() > 0) sb.append(' ');
		sb.append(cmd1[i]);
	    }
	}
	if (cmd2 != null) {
	    System.out.println("CMD2 " + cmd2.length);
	    for (int i = 0 ; i < cmd2.length ; i++) {
		if (sb.length() > 0) sb.append(' ');
		sb.append(cmd2[i]);
	    }
	}
	if (sb.length() == 0) return null;
	
	sb.append('\n');
	
	try {
	    writer.write(sb.toString().getBytes());
	    writer.flush();
	} catch (Exception e) {
	    reconnect();
	    if (writer == null) throw e;
	    writer.write(sb.toString().getBytes());
	    writer.flush();
	}

	try {
	    String line = readLine();

	    // System.out.println("R:" + line);
	    Matcher m = response.matcher(line);
	    if (m.find()) {
		int rc = Integer.parseInt(m.group(1));
		r.error = line; /*
				 * not using group(2), because sometimes
				 * rrdcached says "0 errors, ...." which would
				 * put "error, ..." into this field regardless
				 * of the positive outcome of the command. This
				 * could be misleading
				 */
		if (rc < 0) {
		    r.ok = false;
		} else {
		    sb.setLength(0);
		    r.ok = true;
		    while (rc-- > 0) {
			sb.append(readLine()).append('\n');
		    }
		    r.output = sb.toString();
		}
	    } else {
		try {
		    writer = null;
		    reader = null;
		    socket.close();
		} catch (Exception ignore) {
		}
		throw new Exception("Protocol error");
	    }
	} catch (Exception e) {
	    try {
		writer = null;
		reader = null;
		socket.close();
		throw e;
	    } catch (Exception ignore) {
	    }
	}
	return r;
    }
    
    @Override
    public CommandResult update(String filename, String[] args)
	
    {
        CommandResult result = null;
        try {
            result = sendCommand(new String[] { "UPDATE", filename }, args);
        } catch (Exception ex) {
            Logger.getLogger(RRDCachedClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public CommandResult flush(String filename) throws Exception {
	return sendCommand(new String[] { "FLUSH", filename }, null);
    }

    public CommandResult flushall(String filename) throws Exception {
	return sendCommand(new String[] { "FLUSHALL" }, null);
    }

    public void close() throws IOException {
	if (socket != null) socket.close();
	socket = null;
	writer = null;
	reader = null;
    }

}
