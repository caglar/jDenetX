
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


package javarrd.net.stamfest.rrd.tests;

import javarrd.net.stamfest.rrd.RRD;
import javarrd.net.stamfest.rrd.RRDCommandPool;
import javarrd.net.stamfest.rrd.RRDToolService;


public class Test2 {
    public static void main(final String argv[]) throws Exception {
	RRDCommandPool pool = new RRDCommandPool(20, ".", null);
	pool.setMaxRequestsPerMember(100);
	final RRD rrd = new RRDToolService(pool);
	
	
	int total = 1000;
	int threads = 6;
	final int perthread = total  / threads;
	
	for (int t = 0 ; t < threads ; t++) {
	    Thread th = new Thread() {
		public void run() {
		    System.err.println(System.currentTimeMillis());
		    for (int i = 0 ; i < perthread ; i++) {
			try {
			    rrd.graphv(argv);
			} catch (Exception e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
			
			//for (String k : r.info.keySet()) {
			// System.out.printf("%s=%s\n", k, r.info.get(k));
			//}
			System.out.println(i);
		    }
		    System.err.println(System.currentTimeMillis());
		};
	    };
	    th.start();
	}
    }
}
