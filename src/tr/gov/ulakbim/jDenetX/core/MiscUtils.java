/*
 *    MiscUtils.java
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

import weka.core.Utils;

import java.io.*;
import java.util.Random;

public class MiscUtils {

    public static int[] removeIntElement(int[] source, int rem) {
        int del = 0;
        for (int i = 0; i < source.length; i++) {
            if (source[i] == rem) {
                del = i;
                break;
            }
        }
        int result[] = new int[source.length - 1];
        System.arraycopy(source, 0, result, 0, del);
        if (source.length != del) {
            System.arraycopy(source, del + 1, result, del, source.length - del - 1);
        }
        return result;
    }


    public static int chooseRandomIndexBasedOnWeights(double[] weights,
                                                      Random random) {
        double probSum = Utils.sum(weights);
        double val = random.nextDouble() * probSum;
        int index = 0;
        double sum = 0.0;
        while ((sum <= val) && (index < weights.length)) {
            sum += weights[index++];
        }
        return index - 1;
    }

    public static int poisson(double lambda, Random r) {
        if (lambda < 100.0) {
            double product = 1.0;
            double sum = 1.0;
            double threshold = r.nextDouble() * Math.exp(lambda);
            int i = 1;
            int max = Math.max(100, 10 * (int) Math.ceil(lambda));
            while ((i < max) && (sum <= threshold)) {
                product *= (lambda / i);
                sum += product;
                i++;
            }
            return i - 1;
        }
        double x = lambda + Math.sqrt(lambda) * r.nextGaussian();
        if (x < 0.0) {
            return 0;
        }
        return (int) Math.floor(x);
    }


    public static int getAgreementsInComitee(int votes[], int vote) {
        int agreements = 0;
        for (int currentVote : votes) {
            if (currentVote == vote) {
                agreements++;
            }
        }
        return agreements;
    }

    //Borrowed from: http://www.javaworld.com/javaworld/javatips/jw-javatip76.html?page=2
    // returns a deep copy of an object
    public static Object deepCopy(Object oldObj) throws Exception {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos =
                    new ByteArrayOutputStream(); // A
            oos = new ObjectOutputStream(bos); // B
            // serialize and pass the object
            oos.writeObject(oldObj);   // C
            oos.flush();               // D
            ByteArrayInputStream bin =
                    new ByteArrayInputStream(bos.toByteArray()); // E
            ois = new ObjectInputStream(bin);                  // F
            // return the new object
            return ois.readObject(); // G
        } catch (Exception e) {
            System.out.println("Exception in ObjectCloner = " + e);
            throw (e);
        } finally {
            oos.close();
            ois.close();
        }
    }

    public static String getStackTraceString(Exception ex) {
        StringWriter stackTraceWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stackTraceWriter));
        return "*** STACK TRACE ***\n" + stackTraceWriter.toString();
    }

}
