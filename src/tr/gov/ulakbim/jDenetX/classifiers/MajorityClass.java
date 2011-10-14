/*
 *    MajorityClass.java
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
package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.core.DoubleVector;
import tr.gov.ulakbim.jDenetX.core.Measurement;
import tr.gov.ulakbim.jDenetX.core.StringUtils;
import weka.core.Instance;

public class MajorityClass extends AbstractClassifier {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("hiding")
    public static final String classifierPurposeString = "Majority class classifier: always predicts the class that has been observed most frequently the in the training data.";

    protected DoubleVector observedClassDistribution;

    @Override
    public void resetLearningImpl() {
        this.observedClassDistribution = new DoubleVector();
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        this.observedClassDistribution.addToValue((int) inst.classValue(), inst
                .weight());
    }

    public double[] getVotesForInstance(Instance i) {
        return this.observedClassDistribution.getArrayCopy();
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        Measurement[] measurement = null;
        return null;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        StringUtils.appendIndented(out, indent, "Predicted majority ");
        out.append(getClassNameString());
        out.append(" = ");
        out.append(getClassLabelString(this.observedClassDistribution
                .maxIndex()));
        StringUtils.appendNewline(out);
        for (int i = 0; i < this.observedClassDistribution.numValues(); i++) {
            StringUtils.appendIndented(out, indent, "Observed weight of ");
            out.append(getClassLabelString(i));
            out.append(": ");
            out.append(this.observedClassDistribution.getValue(i));
            StringUtils.appendNewline(out);
        }
    }

    public boolean isRandomizable() {
        return false;
    }

}
