/*
 *    InfoGainSplitCriterion.java
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
package tr.gov.ulakbim.jDenetX.classifiers.splits;

import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
import tr.gov.ulakbim.jDenetX.options.AbstractOptionHandler;
import tr.gov.ulakbim.jDenetX.options.FloatOption;
import tr.gov.ulakbim.jDenetX.tasks.TaskMonitor;
import weka.core.Utils;

public class InfoGainSplitCriterion extends AbstractOptionHandler implements
        SplitCriterion {

    private static final long serialVersionUID = 1L;
    private static final double MINVAL = 1.6009E-16;

    public FloatOption minBranchFracOption = new FloatOption("minBranchFrac",
            'f',
            "Minimum fraction of weight required down at least two branches.",
            0.01, 0.0, 0.5);

    public double getMeritOfSplit(double[] preSplitDist,
                                  double[][] postSplitDists) {
        if (numSubsetsGreaterThanFrac(postSplitDists, this.minBranchFracOption
                .getValue()) < 2) {
            return Double.NEGATIVE_INFINITY;
        }
         return computeEntropy(preSplitDist) - computeEntropy(postSplitDists);
    }

    public double getRangeOfMerit(double[] preSplitDist) {
        int numClasses = preSplitDist.length > 2 ? preSplitDist.length : 2;
        return Utils.log2(numClasses);
    }

    public static double computeEntropy(double[] dist) {
        double entropy = 0.0;
        double sum = 0.0;
        for (double d : dist) {
            if (d > MINVAL) {
                entropy -= d * Utils.log2(d);
                sum += d;
            }
        }
        return sum > 0.0 ? (entropy + sum * Utils.log2(sum)) / sum : 0.0;
    }

    /**
     * Compute the weighted sums of distributions.
     * @param dists
     * @return entropy of the matrix
     */
    public static double computeEntropy(double[][] dists) {
        double totalWeight = 0.0;
        double entropy = 0.0;
        for (double []dist: dists) {
            double distWeight = Utils.sum(dist);
            entropy += distWeight * computeEntropy(dist);
            totalWeight += distWeight;
        }
        return entropy / totalWeight;
    }

    public static int numSubsetsGreaterThanFrac(double[][] distributions,
                                                double minFrac) {
        double totalWeight = 0.0;
        double[] distSums = new double[distributions.length];
        int numGreater = 0;

        for (int i = 0; i < distSums.length; i++) {
            distSums[i] = Utils.sum(distributions[i]);
            totalWeight += distSums[i];
        }
        for (double d : distSums) {
            double frac = d / totalWeight;
            if (frac > minFrac) {
                numGreater++;
            }
        }
        return numGreater;
    }

    public void getDescription(StringBuilder sb, int indent) {}

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
                                     ObjectRepository repository) {}
}
