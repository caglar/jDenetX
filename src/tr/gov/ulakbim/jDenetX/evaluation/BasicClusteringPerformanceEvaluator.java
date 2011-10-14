/*
 *    BasicClusteringPerformanceEvaluator.java
 *    Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet@cs.waikato.ac.nz)
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

public class BasicClusteringPerformanceEvaluator extends AbstractMOAObject
        implements LearningPerformanceEvaluator {

    private static final long serialVersionUID = 1L;

    protected double weightObserved;

    protected double weightCorrect;

    public void reset() {
        this.weightObserved = 0.0;
        this.weightCorrect = 0.0;
    }

    public void addLearningAttempt(int trueClass, double[] classVotes,
                                   double weight) {
        if (weight > 0.0) {
            this.weightObserved += weight;
            if (Utils.maxIndex(classVotes) == trueClass) {
                this.weightCorrect += weight;
            }
        }
    }

    public Measurement[] getPerformanceMeasurements() {
        return new Measurement[]{
                new Measurement("instances",
                        getTotalWeightObserved())
                //,new Measurement("classifications correct (percent)",
                //		getFractionCorrectlyClassified() * 100.0)
        };
    }

    public double getTotalWeightObserved() {
        return this.weightObserved;
    }

    public double getFractionCorrectlyClassified() {
        return this.weightObserved > 0.0 ? this.weightCorrect
                / this.weightObserved : 0.0;
    }

    public double getFractionIncorrectlyClassified() {
        return 1.0 - getFractionCorrectlyClassified();
    }

    public void getDescription(StringBuilder sb, int indent) {
        Measurement.getMeasurementsDescription(getPerformanceMeasurements(),
                sb, indent);
    }

}
