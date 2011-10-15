/*
 *    NaiveBayes.java
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

import tr.gov.ulakbim.jDenetX.classifiers.attributes.AttributeClassObserver;
import tr.gov.ulakbim.jDenetX.classifiers.attributes.GaussianNumericAttributeClassObserver;
import tr.gov.ulakbim.jDenetX.classifiers.attributes.NominalAttributeClassObserver;
import tr.gov.ulakbim.jDenetX.core.AutoExpandVector;
import tr.gov.ulakbim.jDenetX.core.DoubleVector;
import tr.gov.ulakbim.jDenetX.core.Measurement;
import tr.gov.ulakbim.jDenetX.core.StringUtils;
import weka.core.Instance;

public class NaiveBayes extends AbstractClassifier {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("hiding")
    public static final String classifierPurposeString = "Naive Bayes classifier: performs classic bayesian prediction while making naive assumption that all inputs are independent.";

    protected DoubleVector observedClassDistribution;

    protected AutoExpandVector<AttributeClassObserver> attributeObservers;

    @Override
    public void resetLearningImpl() {
        this.observedClassDistribution = new DoubleVector();
        this.attributeObservers = new AutoExpandVector<AttributeClassObserver>();
    }

    @Override
    public String getPurposeString() {
        return super.getPurposeString();
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        this.observedClassDistribution.addToValue((int) inst.classValue(), inst
                .weight());
        for (int i = 0; i < inst.numAttributes() - 1; i++) {
            int instAttIndex = modelAttIndexToInstanceAttIndex(i, inst);
            AttributeClassObserver obs = this.attributeObservers.get(i);
            if (obs == null) {
                obs = inst.attribute(instAttIndex).isNominal() ? newNominalClassObserver()
                        : newNumericClassObserver();
                this.attributeObservers.set(i, obs);
            }
            obs.observeAttributeClass(inst.value(instAttIndex), (int) inst
                    .classValue(), inst.weight());
        }
    }

    public double[] getVotesForInstance(Instance inst) {
        return doNaiveBayesPrediction(inst, this.observedClassDistribution,
                this.attributeObservers);
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        Measurement[] measurement = null;
        return null;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        for (int i = 0; i < this.observedClassDistribution.numValues(); i++) {
            StringUtils.appendIndented(out, indent, "Observations for ");
            out.append(getClassNameString());
            out.append(" = ");
            out.append(getClassLabelString(i));
            out.append(":");
            StringUtils.appendNewlineIndented(out, indent + 1,
                    "Total observed weight = ");
            out.append(this.observedClassDistribution.getValue(i));
            out.append(" / prob = ");
            out.append(this.observedClassDistribution.getValue(i)
                    / this.observedClassDistribution.sumOfValues());
            for (int j = 0; j < this.attributeObservers.size(); j++) {
                StringUtils.appendNewlineIndented(out, indent + 1,
                        "Observations for ");
                out.append(getAttributeNameString(j));
                out.append(": ");
                out.append(this.attributeObservers.get(j));
            }
            StringUtils.appendNewline(out);
        }
    }

    public boolean isRandomizable() {
        return false;
    }

    protected AttributeClassObserver newNominalClassObserver() {
        return new NominalAttributeClassObserver();
    }

    protected AttributeClassObserver newNumericClassObserver() {
        return new GaussianNumericAttributeClassObserver();
    }

    public static double[] doNaiveBayesPrediction(Instance inst,
                                                  DoubleVector observedClassDistribution,
                                                  AutoExpandVector<AttributeClassObserver> attributeObservers) {
        double[] votes = new double[observedClassDistribution.numValues()];
        double observedClassSum = observedClassDistribution.sumOfValues();
        for (int classIndex = 0; classIndex < votes.length; classIndex++) {
            votes[classIndex] = observedClassDistribution.getValue(classIndex)
                    / observedClassSum;
            for (int attIndex = 0; attIndex < inst.numAttributes() - 1; attIndex++) {
                int instAttIndex = modelAttIndexToInstanceAttIndex(attIndex,
                        inst);
                AttributeClassObserver obs = attributeObservers.get(attIndex);
                if ((obs != null) && !inst.isMissing(instAttIndex)) {
                    votes[classIndex] *= obs
                            .probabilityOfAttributeValueGivenClass(inst
                                    .value(instAttIndex), classIndex);
                }
            }
        }
        // TODO: need logic to prevent underflow?
        return votes;
    }

    public void manageMemory(int currentByteSize, int maxByteSize) {
    }
}
