/*
 *    FailedTaskReport.java
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
package tr.gov.ulakbim.jDenetX.tasks;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import tr.gov.ulakbim.jDenetX.core.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class FailedTaskReport extends AbstractMOAObject {

    private static final long serialVersionUID = 1L;

    protected Throwable failureReason;

    public FailedTaskReport(Throwable failureReason) {
        this.failureReason = failureReason;
    }

    public Throwable getFailureReason() {
        return this.failureReason;
    }

    public void getDescription(StringBuilder sb, int indent) {
        sb.append("Failure reason: ");
        sb.append(this.failureReason.getMessage());
        StringUtils.appendNewlineIndented(sb, indent, "*** STACK TRACE ***");
        StringWriter stackTraceWriter = new StringWriter();
        this.failureReason.printStackTrace(new PrintWriter(stackTraceWriter));
        sb.append(stackTraceWriter.toString());
    }

}
