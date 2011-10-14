/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tr.gov.ulakbim.jDenetX.evaluation;


import tr.gov.ulakbim.jDenetX.cluster.Cluster;
import tr.gov.ulakbim.jDenetX.cluster.Clustering;
import tr.gov.ulakbim.jDenetX.gui.visualization.DataPoint;

import java.util.ArrayList;

/**
 * @author jansen
 */
public class SilhouetteCoefficient extends MeasureCollection {
    private static final long serialVersionUID = 1L;
    private double pointInclusionProbThreshold = 0.8;

    public SilhouetteCoefficient() {
        super();
    }

    @Override
    protected boolean[] getDefaultEnabled() {
        boolean[] defaults = {false};
        return defaults;
    }

    @Override
    public String[] getNames() {
        String[] names = {"SilhCoeff"};
        return names;
    }

    public void evaluateClustering(Clustering clustering, Clustering trueClustering, ArrayList<DataPoint> points) {
        int numFCluster = clustering.size();

        double[][] pointInclusionProbFC = new double[points.size()][numFCluster];
        for (int p = 0; p < points.size(); p++) {
            DataPoint point = points.get(p);
            for (int fc = 0; fc < numFCluster; fc++) {
                Cluster cl = clustering.get(fc);
                pointInclusionProbFC[p][fc] = cl.getInclusionProbability(point);
            }
        }

        double silhCoeff = 0.0;
        int totalCount = 0;
        for (int p = 0; p < points.size(); p++) {
            DataPoint point = points.get(p);
            ArrayList<Integer> ownClusters = new ArrayList<Integer>();
            for (int fc = 0; fc < numFCluster; fc++) {
                if (pointInclusionProbFC[p][fc] > pointInclusionProbThreshold) {
                    ownClusters.add(fc);
                }
            }

            if (ownClusters.size() > 0) {
                double[] distanceByClusters = new double[numFCluster];
                int[] countsByClusters = new int[numFCluster];
                //calculate averageDistance of p to all cluster
                for (int p1 = 0; p1 < points.size(); p1++) {
                    DataPoint point1 = points.get(p1);
                    if (p1 != p && point1.classValue() != -1) {
                        for (int fc = 0; fc < numFCluster; fc++) {
                            double distance = distance(point, point1);
                            if (pointInclusionProbFC[p1][fc] > pointInclusionProbThreshold) {
                                distanceByClusters[fc] += distance;
                                countsByClusters[fc]++;
                            }
                        }
                    }
                }

                //find closest OWN cluster as clusters might overlap
                double minAvgDistanceOwn = Double.MAX_VALUE;
                int minOwnIndex = -1;
                for (int fc : ownClusters) {

                    double normDist = distanceByClusters[fc] / (double) countsByClusters[fc];
                    if (normDist < minAvgDistanceOwn && pointInclusionProbFC[p][fc] > pointInclusionProbThreshold) {
                        minAvgDistanceOwn = normDist;
                        minOwnIndex = fc;
                    }
                }


                //find closest other (or other own) cluster
                double minAvgDistanceOther = Double.MAX_VALUE;
                for (int fc = 0; fc < numFCluster; fc++) {
                    if (fc != minOwnIndex) {
                        double normDist = distanceByClusters[fc] / (double) countsByClusters[fc];
                        if (normDist < minAvgDistanceOther) {
                            minAvgDistanceOther = normDist;
                        }
                    }
                }

                double silhP = (minAvgDistanceOther - minAvgDistanceOwn) / Math.max(minAvgDistanceOther, minAvgDistanceOwn);
                point.setMeasureValue("SC - own", minAvgDistanceOwn);
                point.setMeasureValue("SC - other", minAvgDistanceOther);
                point.setMeasureValue("SC", silhP);

                silhCoeff += silhP;
                totalCount++;
                //System.out.println(point.getTimestamp()+" Silh "+silhP+" / "+avgDistanceOwn+" "+minAvgDistanceOther+" (C"+minIndex+")");
            }
        }
        if (totalCount > 0)
            silhCoeff /= (double) totalCount;
        //normalize from -1, 1 to 0,1
        silhCoeff = (silhCoeff + 1) / 2.0;
        addValue(0, silhCoeff);
    }

    private double distance(DataPoint inst1, DataPoint inst2) {
        double distance = 0.0;
        int numDims = inst1.numAttributes();
        for (int i = 0; i < numDims; i++) {
            double d = inst1.value(i) - inst2.value(i);
            distance += d * d;
        }
        return Math.sqrt(distance);
    }
}
