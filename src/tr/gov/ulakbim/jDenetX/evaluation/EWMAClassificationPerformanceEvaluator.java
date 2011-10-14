/*
 *    EWMAClassificationPerformanceEvaluator.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
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
package tr.gov.ulakbim.jDenetX.evaluation;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import tr.gov.ulakbim.jDenetX.core.Measurement;
import weka.core.Utils;

public class EWMAClassificationPerformanceEvaluator extends AbstractMOAObject
        implements ClassificationPerformanceEvaluator {

    private static final long serialVersionUID = 1L;

    protected double TotalweightObserved;

    protected double alpha;

    protected Estimator weightCorrect;

    protected class Estimator {

        protected double alpha;
        protected double estimation;

        public Estimator(double a) {
            alpha = a;
            estimation = 0;
        }

        public void add(double value) {
            estimation += alpha * (value - estimation);
        }

        public double estimation() {
            return estimation;
        }
    }

    public void setalpha(double a) {
        this.alpha = a;
        reset();
    }

    public void reset() {
        weightCorrect = new Estimator(this.alpha);
    }

    public void addClassificationAttempt(int trueClass, double[] classVotes,
                                         double weight) {
        if (weight > 0.0) {
            this.TotalweightObserved += weight;
            if (Utils.maxIndex(classVotes) == trueClass) {
                this.weightCorrect.add(1);
            } else
                this.weightCorrect.add(0);
        }
    }

    public Measurement[] getPerformanceMeasurements() {
        return new Measurement[]{
                new Measurement("classified instances",
                        this.TotalweightObserved),
                new Measurement("classifications correct (percent)",
                        getFractionCorrectlyClassified() * 100.0)};
    }

    public double getTotalWeightObserved() {
        return this.TotalweightObserved;
    }

    public double getFractionCorrectlyClassified() {
        return this.weightCorrect.estimation();
    }

    public double getFractionIncorrectlyClassified() {
        return 1.0 - getFractionCorrectlyClassified();
    }

    public void getDescription(StringBuilder sb, int indent) {
        Measurement.getMeasurementsDescription(getPerformanceMeasurements(),
                sb, indent);
    }

}
