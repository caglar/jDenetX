/*
 *    SerializeUtils.java
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

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SerializeUtils {

    public static class ByteCountingOutputStream extends OutputStream {

        protected int numBytesWritten = 0;

        public int getNumBytesWritten() {
            return this.numBytesWritten;
        }

        @Override
        public void write(int b) throws IOException {
            this.numBytesWritten++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            this.numBytesWritten += len;
        }

        @Override
        public void write(byte[] b) throws IOException {
            this.numBytesWritten += b.length;
        }
    }

    public static void writeToFile(File file, Serializable obj)
            throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(
                new BufferedOutputStream(new FileOutputStream(file))));
        out.writeObject(obj);
        out.flush();
        out.close();
    }

    public static Object readFromFile(File file) throws IOException,
            ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(
                new BufferedInputStream(new FileInputStream(file))));
        Object obj = in.readObject();
        in.close();
        return obj;
    }

    public static Object copyObject(Serializable obj) throws Exception {
        ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(baoStream));
        out.writeObject(obj);
        out.flush();
        out.close();
        byte[] byteArray = baoStream.toByteArray();
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
                new ByteArrayInputStream(byteArray)));
        Object copy = in.readObject();
        in.close();
        return copy;
    }

    public static int measureObjectByteSize(Serializable obj) throws Exception {
        ByteCountingOutputStream bcoStream = new ByteCountingOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(bcoStream));
        out.writeObject(obj);
        out.flush();
        out.close();
        return bcoStream.getNumBytesWritten();
    }

}
