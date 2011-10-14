/*
 *    CachedInstancesStream.java
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
package tr.gov.ulakbim.jDenetX.streams;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import tr.gov.ulakbim.jDenetX.core.InstancesHeader;
import weka.core.Instance;
import weka.core.Instances;

public class CachedInstancesStream extends AbstractMOAObject implements
        InstanceStream {

    private static final long serialVersionUID = 1L;

    protected Instances toStream;

    protected int streamPos;

    public CachedInstancesStream(Instances toStream) {
        this.toStream = toStream;
    }

    public InstancesHeader getHeader() {
        return new InstancesHeader(this.toStream);
    }

    public long estimatedRemainingInstances() {
        return this.toStream.numInstances() - this.streamPos;
    }

    public boolean hasMoreInstances() {
        return this.streamPos < this.toStream.numInstances();
    }

    public Instance nextInstance() {
        return this.toStream.instance(this.streamPos++);
    }

    public boolean isRestartable() {
        return true;
    }

    public void restart() {
        this.streamPos = 0;
    }

    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub

    }
}
