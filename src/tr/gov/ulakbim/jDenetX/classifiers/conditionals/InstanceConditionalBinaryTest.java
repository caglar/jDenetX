/*
 *    InstanceConditionalBinaryTest.java
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

package tr.gov.ulakbim.jDenetX.classifiers.conditionals;

import weka.core.Instance;

public abstract class InstanceConditionalBinaryTest extends
        InstanceConditionalTest {

    private static final long serialVersionUID = 549698120023371710L;

    /**
     * Check if the instance passes the test
     * @param inst
     * @return the result, true if it passes, false otherwise
     */
    public boolean passesTest(Instance inst) {
        return branchForInstance(inst) == 0;
    }

    /**
     * Check if the instance fails the test
     * @param inst
     * @return the result of the test, true if it is fails, false otherwise
     */
    public boolean failsTest(Instance inst) {
        return branchForInstance(inst) == 1;
    }

    @Override
    public int maxBranches() {
        return 2;
    }

}
