/*
 *    TimingUtils.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package tr.gov.ulakbim.jDenetX.core;

public class TimingUtils {

    protected static boolean preciseThreadTimesAvailable = false;

    public static boolean enablePreciseTiming() {
        if (!preciseThreadTimesAvailable) {
            try {
                java.lang.management.ThreadMXBean tmxb = java.lang.management.ManagementFactory
                        .getThreadMXBean();
                if (tmxb.isCurrentThreadCpuTimeSupported()) {
                    tmxb.setThreadCpuTimeEnabled(true);
                    preciseThreadTimesAvailable = true;
                }
            } catch (Throwable e) {
                // ignore problems, just resort to inaccurate timing
            }
        }
        return preciseThreadTimesAvailable;
    }

    public static long getNanoCPUTimeOfCurrentThread() {
        return getNanoCPUTimeOfThread(Thread.currentThread().getId());
    }

    public static long getNanoCPUTimeOfThread(long threadID) {
        if (preciseThreadTimesAvailable) {
            long time = java.lang.management.ManagementFactory
                    .getThreadMXBean().getThreadCpuTime(threadID);
            if (time != -1) {
                return time;
            }
        }
        return System.nanoTime();
    }

    public static double nanoTimeToSeconds(long nanoTime) {
        return nanoTime / 1000000000.0;
    }
}
