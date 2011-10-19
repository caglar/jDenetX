
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RRDp implements RRDCommand {
    static private Logger logger = Logger.getLogger(RRDp.class.getName());
    Process rrdtool = null;
    Socket socket = null;
   
    private OutputStream writer;
    private InputStream reader;

    public RRDp(String basedir, String cachedAddress) throws IOException {
	String cmd[] = new String[] { "rrdtool", "-", basedir };
	
	ProcessBuilder pb = new ProcessBuilder(cmd);
	if (cachedAddress != null) {
	    pb.environment().put("RRDCACHED_ADDRESS", cachedAddress);
	}
	
	rrdtool = pb.start();
	
	InputStream r = rrdtool.getInputStream();
	reader = r;
	writer = rrdtool.getOutputStream();
    }
    
    public RRDp(String host, int port) throws IOException {
	socket = new Socket(host, port);
	
	reader = socket.getInputStream();
	writer = socket.getOutputStream();
    }
    
    // OK u:0.00 s:0.00 r:17.20
    static private Pattern okpat   = Pattern.compile("^OK u:([0-9.]+) s:([0-9.]+) r:([0-9.]+)"); 
    static private Pattern infoPat = Pattern.compile("^\\s*(.*?)\\s*=\\s*(.*?)\\s*$");
    protected synchronized CommandResult sendCommand(String command[]) throws Exception {
	if (command == null || command.length == 0) {
	    throw new IllegalArgumentException();
	}
	
	if (command[0].equals("graph")) {
	    throw new Exception("Use graphv instead"); 
	}
	
	if (rrdtool == null && socket == null) {
	    throw new Exception("No subprocess available (already closed?)");
	}
	StringBuffer sb = new StringBuffer();
	for (String c: command) {
	    if (sb.length() > 0) sb.append(' ');
	    sb.append(c);
	}
	sb.append('\n');

	if (logger.isLoggable(Level.FINER)) {
	    logger.finer(sb.toString());
	}

	writer.write(sb.toString().getBytes());
	writer.flush();
	
	String line;
	
	CommandResult r = new CommandResult();
	sb.setLength(0);
	HashMap<String, String> data = null;
	Matcher m;
	
	while ((line = readLine()) != null) {
	    if (line.startsWith("ERROR")) {
		r.ok = false;
		r.error = line;
		break;
	    }
	    if (line.startsWith("OK ")) {
		m = okpat.matcher(line);
		
		if (m.find()) {
		    r.user = Float.parseFloat(m.group(1));
		    r.system = Float.parseFloat(m.group(2));
		    r.total= Float.parseFloat(m.group(3));
		    r.ok = true;
		    break;
		}
	    }
	    
	    m = infoPat.matcher(line);
	    if (m.find()) {
		if (data == null) data = new HashMap<String, String>();
		
		String k = m.group(1);
		String v = m.group(2);

		if (k.equals("image") && v.startsWith("BLOB_SIZE:")) {
		    int len = Integer.parseInt(v.substring(10));
		    
		//    System.err.println("BLOB " + len);
		    byte img[] = new byte[len];
		    int pos = 0;
		    while (pos != len) {
			int n = reader.read(img, pos, len - pos);
			if (n <= 0) throw new Exception("protocol error");
			pos += n;
		    }
		    r.image = img;
		}

		data.put(k, v);
		if (logger.isLoggable(Level.FINEST)) {
		    logger.finest(String.format("put info: %s=%s", k, v));
		}
		continue;
	    }
	    
	    sb.append(line).append("\n");
	}
	r.output = sb.toString();
	r.info = data;
	
	if (logger.isLoggable(Level.FINEST)) {
	    logger.finest(r.toString());
	}
	
	if (logger.isLoggable(Level.FINER)) {
	    logger.finer(r.ok + ":" + r.error);
	}
	
	return r;
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

    public void finish() {
	if (rrdtool != null || socket != null) {
	    try {
		if (socket != null) socket.close();
		writer.close();
		reader.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    rrdtool = null;
	    socket = null;
	    writer = null;
	    reader = null;
	}
    }
    
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        finish();
    }
    
    public CommandResult command(String cmd[]) throws Exception {
	try {
	    return sendCommand(cmd);
	} catch (Exception e) {
	    if (rrdtool == null) throw e;
	    InputStream error = rrdtool.getErrorStream();
	    byte buf[] = new byte[1024];
	    while (error.available() > 0) {
		int n = error.read(buf);
		System.err.write(buf, 0, n);
	    }
	    throw e;
	}
    }
    
}
