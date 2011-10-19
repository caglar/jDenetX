
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

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author peter
 * 
 *         A java interface to Tobi Oetikers rrdtool. This provides rrd
 *         operations to java programs. It puts a layer on top of the actual job
 *         to invoke rrdtool functionality. By default, it uses the rrdtool pipe
 *         mode of operation with a single instance of rrdtool. It can use other
 *         backends as well, eg. a RRDCommandPool. The entire machinery is quite
 *         flexible.
 * 
 */

public class RRDToolService implements RRD {
    RRDCommandPool pool;
    private RRDCachedClient cachedClient = null;
    
    public RRDToolService(RRDCommandPool pool) {
	this.pool = pool;
    }
    
    /* (non-Javadoc)
     * @see net.stamfest.rrd.RRD#info(java.lang.String)
     */
    public CommandResult info(String filename) throws Exception {
	return pool.command(new String[] { "info", filename });
    }

    public CommandResult update(String filename, String arg) throws Exception {
	if (cachedClient != null) {
	    return cachedClient.update(filename, new String[] { arg });
	} else {
	    String cmd[] = new String[] { "update", filename, arg };
	    return pool.command(cmd);
	}
    }
    
    @Override
    public CommandResult update(String filename, String args[]) {
	String cmd[] = new String[args.length + 2];
        CommandResult cmdResult = null;
	cmd[0] = "update";
	cmd[1] = filename;
	for (int i = 0, j = 2 ; i < args.length ; i++, j++) {
	    cmd[j] = args[i];
	}
        try {
            cmdResult = pool.command(cmd);
        } catch (Exception ex) {
            Logger.getLogger(RRDToolService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cmdResult;
    }
    
    /* (non-Javadoc)
     * @see net.stamfest.rrd.RRD#graphv(java.lang.String[])
     */
    public CommandResult graphv(String[] cmdin) throws Exception {
	String cmd[] = new String[cmdin.length + 2];
	cmd[0] = "graphv";
	cmd[1] = "-";
	
	for (int i = 0, j = 2 ; i < cmdin.length ; i++, j++) {
	    cmd[j] = cmdin[i];
	}

	return pool.command(cmd);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        pool.finish();
    }

    static final String pwd[] = new String[] { "pwd" };
    public static String getCwd(RRDCommand rrdcmd) throws Exception {
	CommandResult r = rrdcmd.command(pwd);
	if (! r.ok) throw new Exception("Problem executing 'pwd' command");
	
	return r.output.trim();
    }
    
    public static boolean chdir(RRDCommand rrdcmd, String dir) throws Exception {
	String cmd[] = new String[] { "cd", dir	};
	CommandResult r = rrdcmd.command(cmd);
	return r.ok;
    }
    
    
    private List<String> splitFilename(String filename) {
	ArrayList<String> l = new ArrayList<String>();
	int i,st = 0, len = filename.length();
	
	for (i = 0 ; i < len ; i++) {
	    if (filename.charAt(i) == File.separatorChar) {
		if (st != i) {
		    addPathElement(l, filename.substring(st, i));
		}
		st = i + 1;
	    }
	}
	if (st != i) addPathElement(l, filename.substring(st, i));
	return l;
    }

    /**
     * @param l
     * @param e
     */
    private void addPathElement(ArrayList<String> l, String e) {
	/* handle "." and ".." directory entries */
	if (! e.equals(".")) {
	    if (e.equals("..")) {
		if (l.size() > 0) l.remove(l.size() - 1);
	    } else {
		l.add(e);
	    }
	}
    }
    
    @Override
    public CommandResult create(String filename, String[] args)
	throws Exception
    {
	// 	split filename 
	List<String> pel = splitFilename(filename);
	RRDCommand rrdcmd = pool.getConnection();
	String cwd = null;
	try {
	    cwd = getCwd(rrdcmd);
	
	    if (pel.size() > 0) pel.remove(pel.size() - 1);
	    for (String pe : pel) { 
		if (chdir(rrdcmd, pe)) continue;
		if (!mkdir(rrdcmd, pe)) throw new Exception("Cannot mkdir directory");
		chdir(rrdcmd, pe);
	    }
	} finally {
	    try {
		if (cwd != null) rrdcmd.command(new String[] { "cd", cwd });
	    } finally {
		pool.done(rrdcmd);
	    }
	}
	
	
	String cmd[] = new String[args.length + 2];
	cmd[0] = "create";
	cmd[1] = filename;
	
	for (int i = 0, j = 2 ; i < args.length ; i++, j++) {
	    cmd[j] = args[i];
	}

	return pool.command(cmd);
    }
    
    @Override
    public CommandResult tune(String filename, String[] args) throws Exception {
	String cmd[] = new String[args.length + 2]; 
	cmd[0] = "tune";
	cmd[1] = filename;
	for (int i = 0, j = 2 ; i < args.length ; i++, j++) {
	    cmd[j] = args[i];
	}
	return pool.command(cmd);
    }
    
    public static boolean mkdir(RRDCommand rrdcmd, String dir) throws Exception {
	CommandResult r = rrdcmd.command(new String[] { "mkdir", dir });
	if (r == null) return false;
	return r.ok;
    }

    public boolean exists(String filename) throws Exception {
	if (!filename.endsWith(".rrd")) 
	    throw new Exception("Can only check for the existance of .rrd files");
	
	File f = new File(filename);
	File dir = f.getParentFile();
	
	String dirstr = dir.getPath();
	// line we await in the output of the ls command.
	String await = "- " + f.getName();
	
	RRDCommand rrdcmd = pool.getConnection();
	String current = null;
	try {
	    current = getCwd(rrdcmd);
	    String cmd[] = new String[] { "ls" };

	    if (! chdir(rrdcmd, dirstr)) return false;
	
	    CommandResult r = rrdcmd.command(cmd);
	    // parse output - a directory listing
	    if (! r.ok) return false;
	    // System.err.println("ls OUTPUT:\n" + r.output);
	    
	    BufferedReader br = new BufferedReader(new StringReader(r.output));
	    String line;
	    while ((line = br.readLine()) != null) {
		if (line.equals(await)) return true;
	    }
	    
	    return false;	    
	} finally {
	    try {
		if (current != null) chdir(rrdcmd, current);
	    } finally {
		pool.done(rrdcmd);
	    }
	    
	}
    }

    public RRDCachedClient getCachedClient() {
        return cachedClient;
    }

    public void setCachedClient(RRDCachedClient cachedClient) {
        this.cachedClient = cachedClient;
    }

}
