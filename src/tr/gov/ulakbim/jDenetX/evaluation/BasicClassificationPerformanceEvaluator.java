/*
 *    BasicClassificationPerformanceEvaluator.java
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

package tr.gov.ulakbim.jDenetX.evaluation;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import tr.gov.ulakbim.jDenetX.core.Measurement;
import weka.core.Utils;

public class BasicClassificationPerformanceEvaluator extends AbstractMOAObject
        implements ClassificationPerformanceEvaluator {

    private static final long serialVersionUID = 1L;

    protected double weightObserved;

    protected double weightCorrect;

    protected double[] columnKappa;

    protected double[] rowKappa;

    protected int[] instanceClassesMap;

    protected int numClasses;

    private double SE = 0.0;

    private int NoOfProcessedInstances = 0;

    public void reset() {
        reset(this.numClasses);
    }

    public void reset(int numClasses) {
        this.numClasses = numClasses;
        this.rowKappa = new double[numClasses];
        this.columnKappa = new double[numClasses];
        instanceClassesMap = new int[numClasses];

        for (int i = 0; i < this.numClasses; i++) {
            this.rowKappa[i] = 0;
            this.columnKappa[i] = 0;
        }
        this.SE = 0.0;
        NoOfProcessedInstances = 0;
        this.weightObserved = 0.0;
        this.weightCorrect = 0.0;
    }

    public void addClassificationAttempt(int trueClass, double[] classVotes,
                                         double weight) {
        if (weight > 0.0) {
            NoOfProcessedInstances++;
            if (this.weightObserved == 0) {
                reset(classVotes.length > 1 ? classVotes.length : 2);
            }
            this.weightObserved += weight;
            int predictedClass = Utils.maxIndex(classVotes);
            if (predictedClass == trueClass) {
                this.weightCorrect += weight;
            }
            this.SE += Evaluation.getSqError(trueClass, classVotes, weight);
            this.rowKappa[predictedClass] += weight;
            this.columnKappa[trueClass] += weight;
            instanceClassesMap[trueClass]++;
        }
    }

    public Measurement[] getPerformanceMeasurements() {
        return new Measurement[]{
                new Measurement("classified instances",
                        getTotalWeightObserved()),
                new Measurement("classifications correct (percent)",
                        getFractionCorrectlyClassified() * 100.0),
                new Measurement("Kappa Statistic (percent)",
                        getKappaStatistic() * 100.0),
                new Measurement("Mean Square Error ",
                        getMSE()),
                new Measurement("Root Mean Square Error ",
                        getRMSE())
        };
    }

    public double getTotalWeightObserved() {
        return this.weightObserved;
    }

    public double getMSE() {
        return (SE / (double) NoOfProcessedInstances);
    }

    public double getRMSE() {
        return Math.sqrt(SE / (double) NoOfProcessedInstances);
    }

    public double getFractionCorrectlyClassified() {
        return this.weightObserved > 0.0 ? this.weightCorrect
                / this.weightObserved : 0.0;
    }

    public double getFractionIncorrectlyClassified() {
        return 1.0 - getFractionCorrectlyClassified();
    }

    public double getKappaStatistic() {
        if (this.weightObserved > 0.0) {
            double p0 = getFractionCorrectlyClassified();
            double pc = 0.0;
            for (int i = 0; i < this.numClasses; i++) {
                pc += (this.rowKappa[i] / this.weightObserved) *
                        (this.columnKappa[i] / this.weightObserved);

            }
            return (p0 - pc) / (1.0 - pc);
        } else {
            return 0;
        }
    }

    public void getDescription(StringBuilder sb, int indent) {
        Measurement.getMeasurementsDescription(getPerformanceMeasurements(),
                sb, indent);
    }

}