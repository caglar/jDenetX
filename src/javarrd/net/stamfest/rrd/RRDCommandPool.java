
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
import java.util.logging.Level;
import java.util.logging.Logger;

class RRDPoolMember {
    int requestcount = 0;
    boolean inuse = false;
    RRDCommand rrdp;
}

class RRDPoolMemberWrapper implements RRDCommand {
    RRDPoolMember member;

    public RRDPoolMemberWrapper(RRDPoolMember member) {
	this.member = member;
    }

    @Override
    public void finish() {
	member.rrdp.finish();
	member.rrdp = null;
    }

    @Override
    public CommandResult command(String[] cmd) throws Exception {
	return member.rrdp.command(cmd);
    }
};

public class RRDCommandPool implements RRDCommand {
    
    private int poolsize = 4;
    private int maxRequestsPerMember = 100;
    private boolean finished = false;
    
    private RRDCommandFactory factory = null;
    
    RRDPoolMember pool[] = null;
    
    public RRDCommandPool(String basedir, String cachedAddress) {
	init(4, basedir, cachedAddress);
    }
    
    public RRDCommandPool(int poolsize, String basedir, String cachedAddress) {
	init(poolsize, basedir, cachedAddress);
    }
    
    public RRDCommandPool(int poolsize, RRDCommandFactory factory) {
	init(poolsize, factory);
    }
    
    private void init(int poolsize, 
                      final String basedir,
                      final String cachedAddress) {
	init(poolsize, new RRDCommandFactory() {
	    @Override
	    public RRDp createRRDCommand() {
	        RRDp rrdp = null;
                try {
                    rrdp = new RRDp(basedir, cachedAddress);
                } catch (IOException ex) {
                    Logger.getLogger(RRDCommandPool.class.getName()).log(Level.SEVERE, null, ex);
                }
                return rrdp;
	    }
	});
    }

    /**
     * @param poolsize
     */
    private void init(int poolsize, RRDCommandFactory factory) {
	this.poolsize = poolsize;
	this.factory = factory;
	
	pool = new RRDPoolMember[poolsize];
    }

    
    public RRDCommand getConnection() throws Exception {
	if (finished) throw new Exception("Already finished");
	RRDPoolMember member = null;

	synchronized(this) {
	    while (true) {
		for (int i = 0 ; i < poolsize ; i++) {
		    if (pool[i] == null) {
			pool[i] = new RRDPoolMember();
			member = pool[i];
			break;
		    } else if (! pool[i].inuse) {
			member = pool[i];
			break;
		    }
		}
		if (member != null) {
		    member.inuse = true;
		    break;
		} else {
		    try {
			wait();
		    } catch (InterruptedException e) {
			// 		ignore
		    }
		    // next loop iteration 
		}
	    }
	}
	
	member.requestcount++;
	if (member.rrdp == null) {
	    member.requestcount = 0;
	    member.rrdp = factory.createRRDCommand();
	}
	
	return new RRDPoolMemberWrapper(member);
    }
    
    public void done(RRDCommand cmd) {
	if (cmd instanceof RRDPoolMemberWrapper) {
	    RRDPoolMember member = ((RRDPoolMemberWrapper) cmd).member;
	    
	    if (member.requestcount > maxRequestsPerMember) {
		if (member.rrdp != null) {
		    // this may happen in case of a previous error
		    member.rrdp.finish();
		}
		member.requestcount = 0;
		member.rrdp = null;
	    }
	    
	    synchronized (this) {
		member.inuse = false;
		notify();
	    }
	}
    }
    
    private void kick(RRDCommand cmd) {
	if (cmd instanceof RRDPoolMemberWrapper) {
	    RRDPoolMember member = ((RRDPoolMemberWrapper) cmd).member;
	    
	    if (member.rrdp != null) {
		// this may happen in case of a previous error
		member.rrdp.finish();
	    }
	    member.requestcount = 0;
	    member.rrdp = null;
	    
	    synchronized (this) {
		member.inuse = false;
		notify();
	    }
	}
    }


    @Override
    public CommandResult command(String[] cmd) throws Exception {
	RRDCommand rrdcmd = getConnection();
	try {
	    return rrdcmd.command(cmd);
	} catch (Exception e) {
	    kick(rrdcmd);
	    throw e;
	} finally {
	    done(rrdcmd);
	}
    }

    public int getMaxRequestsPerMember() {
        return maxRequestsPerMember;
    }

    public void setMaxRequestsPerMember(int maxRequestsPerMember) {
        this.maxRequestsPerMember = maxRequestsPerMember;
    }

    public int getPoolsize() {
        return poolsize;
    }

    @Override
    public void finish() {
	finished = true;
	synchronized (this) {
	    boolean alldone = false;
	    
	    while (!alldone) {
		alldone = true;
		for (int i = 0 ; i < poolsize ; i++) {
		    if (pool[i] == null) continue;
		    if (! pool[i].inuse) {
			if (pool[i].rrdp != null) {
			    pool[i].rrdp.finish();
			    pool[i].rrdp = null;
			    pool[i] = null;
			}
			continue;
		    } else {
			alldone = false;
			// in use - have to wait
			try {
			    wait();
			} catch (InterruptedException e) {
			    // 	ignore
			}
			break;
		    }
		}
	    }
	}
    }

    public RRDCommandFactory getFactory() {
        return factory;
    }

    public void setFactory(RRDCommandFactory factory) {
        this.factory = factory;
    }
}
