/*
 *    MakeObject.java
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
package tr.gov.ulakbim.jDenetX;

import tr.gov.ulakbim.jDenetX.core.Globals;
import tr.gov.ulakbim.jDenetX.core.SerializeUtils;
import tr.gov.ulakbim.jDenetX.options.ClassOption;

import java.io.File;
import java.io.Serializable;

public class MakeObject {

    public static void main(String[] args) {
        try {
            System.err.println();
            System.err.println(Globals.getWorkbenchInfoString());
            System.err.println();
            if (args.length < 2) {
                System.err.println("usage: java " + MakeObject.class.getName()
                        + " outputfile.moa \"<object name> <options>\"");
                System.err.println();
            } else {
                String filename = args[0];
                // build a single string by concatenating cli options
                StringBuilder cliString = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    cliString.append(" " + args[i]);
                }
                // parse options
                System.err.println("Making object...");
                Object result = ClassOption.cliStringToObject(cliString
                        .toString(), Object.class, null);
                System.err.println("Writing object to file: " + filename);
                SerializeUtils.writeToFile(new File(filename),
                        (Serializable) result);
                System.err.println("Done.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
