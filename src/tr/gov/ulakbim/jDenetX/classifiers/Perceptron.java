package tr.gov.ulakbim.jDenetX.classifiers;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: 2/14/12
 * Time: 10:44 PM
 * To change this template use File | Settings | File Templates.
 */
/*
 *    Perceptron.java
 *    Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
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

import tr.gov.ulakbim.jDenetX.core.Measurement;
import tr.gov.ulakbim.jDenetX.options.FloatOption;
import weka.core.Instance;

/**
 * Single perceptron classifier.
 *
 * <p>Performs classic perceptron multiclass learning incrementally.</p>
 *
 * <p>Parameters:</p>
 * <ul>
 * <li>-r : Learning ratio of the classifier</li>
 * </ul>
 *
 * @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
 * @version $Revision: 7 $
 */
public class Perceptron extends AbstractClassifier {

    private static final long serialVersionUID = 221L;

    @SuppressWarnings("hiding")
    public static final String classifierPurposeString = "Perceptron classifier: Single perceptron classifier.";

    public FloatOption learningRatioOption = new FloatOption("learningRatio", 'r', "Learning ratio", 1);

    protected double[][] weightAttribute;

    protected boolean reset;

    protected int numberAttributes;

    protected int numberClasses;

    protected int numberDetections;

    @Override
    public void resetLearningImpl() {
        this.reset = true;
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {

        //Init Perceptron
        if (this.reset) {
            this.reset = false;
            this.numberAttributes = inst.numAttributes();
            this.numberClasses = inst.numClasses();
            this.weightAttribute = new double[inst.numClasses()][inst.numAttributes()];
            for (int i = 0; i < inst.numClasses(); i++) {
                for (int j = 0; j < inst.numAttributes(); j++) {
                    weightAttribute[i][j] = 0.2 * Math.random() - 0.1;
                }
            }
        }

        double[] preds = new double[inst.numClasses()];
        for (int i = 0; i < inst.numClasses(); i++) {
            preds[i] = prediction(inst, i);
        }
        double learningRatio = learningRatioOption.getValue();

        int actualClass = (int) inst.classValue();
        for (int i = 0; i < inst.numClasses(); i++) {
            double actual = (i == actualClass) ? 1.0 : 0.0;
            double delta = (actual - preds[i]) * preds[i] * (1 - preds[i]);
            for (int j = 0; j < inst.numAttributes() - 1; j++) {
                this.weightAttribute[i][j] += learningRatio * delta * inst.value(j);
            }
            this.weightAttribute[i][inst.numAttributes() - 1] += learningRatio * delta;
        }
    }

    public void setWeights(double[][] w) {
        //Perceptron Hoeffding Tree
        this.weightAttribute = w;
    }

    public double[][] getWeights() {
        //Perceptron Hoeffding Tree
        return this.weightAttribute;
    }

    public int getNumberAttributes() {
        //Perceptron Hoeffding Tree
        return this.numberAttributes;
    }

    public int getNumberClasses() {
        //Perceptron Hoeffding Tree
        return this.numberClasses;
    }

    public double prediction(Instance inst, int classVal) {
        double sum = 0.0;
        for (int i = 0; i < inst.numAttributes() - 1; i++) {
            sum += weightAttribute[classVal][i] * inst.value(i);
        }
        sum += weightAttribute[classVal][inst.numAttributes() - 1];
        return 1.0 / (1.0 + Math.exp(-sum));
    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        double[] votes = new double[inst.numClasses()];
        if (!this.reset) {
            for (int i = 0; i < votes.length; i++) {
                votes[i] = prediction(inst, i);
            }
            try {
                weka.core.Utils.normalize(votes);
            } catch (Exception e) {
                // ignore all zero votes error
            }
        }
        return votes;
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return null;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
    }

    @Override
    public boolean isRandomizable() {
        return false;
    }
}