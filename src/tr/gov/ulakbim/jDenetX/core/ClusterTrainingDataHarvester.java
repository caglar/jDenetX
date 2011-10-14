package tr.gov.ulakbim.jDenetX.core;

import weka.clusterers.SimpleKMeans;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 3, 2010
 * Time: 11:27:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class ClusterTrainingDataHarvester {

    private int NoOfClusters = 0;
    private Instances Centroids;
    private static DoubleVector Weights = new DoubleVector();

    public ClusterTrainingDataHarvester(int noOfClusters) {
        NoOfClusters = noOfClusters;
    }

    //Put the closest cluster first

    public Instances[] getEnsembleTrainingData(Instances classInstances, int noOfClasses) {
        DoubleVector weights = new DoubleVector();
        Instances[] resultData = new Instances[NoOfClusters];
        ArrayList attList = Collections.list(classInstances.enumerateAttributes());
        attList.add(classInstances.classAttribute());
        try {
            for (int i = 0; i < resultData.length; i++) {
                resultData[i] = new Instances("resultData", attList, classInstances.size());
                resultData[i].setClassIndex(attList.size() - 1);
            }

            Instance classCentroid = getClassCentroid(classInstances);
            EuclideanDistance distanceFun = new EuclideanDistance();

            int[] assignments = getClusterAssignments(classInstances);
            int[] pointList = new int[Centroids.size()];
            int[] orderedClusterList = new int[Centroids.size()];

            Instances classCentroids = (Instances) MiscUtils.deepCopy(Centroids);
            int idx = 0;
            distanceFun.setInstances(classCentroids);

            double distance;
            for (int i = 0; i < Centroids.size(); i++) {
                distance = distanceFun.distance(classCentroid, classCentroids.get(i));
                weights.setValue(i, distance);
            }
            weights.normalize();

            for (int i = 0; i < Centroids.size(); i++) {
                pointList[i] = i;
            }

            //TODO: Fix the bug here
            //When no points left in the pointlist, this code may throw an ArrayIndexOutOfBounds exception
            for (int i = 0; i < Centroids.size(); i++) {
                idx = distanceFun.closestPoint(classCentroid, classCentroids, pointList);
                orderedClusterList[idx] = i;
                pointList = MiscUtils.removeIntElement(pointList, idx);
            }

            for (int i = 0; i < classInstances.size(); i++) {
                resultData[orderedClusterList[assignments[i]]].add(classInstances.get(i));
            }

            if (Weights.numValues() > 0) {
                //Get average of them
                Weights.addValues(weights);
                Weights.scaleValues(0.5);

            } else {
                Weights = (DoubleVector) weights.copy();
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return resultData;
    }

    private Instance getClassCentroid(Instances classInstances) throws Exception {
        SimpleKMeans kmeans;
        Instances centroids;
        Instances filteredData;
        Remove filter;

        Instances insts = (Instances) MiscUtils.deepCopy(classInstances);

        kmeans = new SimpleKMeans();
        filter = new Remove();

        insts.setClassIndex(insts.numAttributes() - 1);

        filter.setAttributeIndices("" + (insts.classIndex() + 1));
        filter.setInputFormat(insts);
        filteredData = Filter.useFilter(insts, filter);

        kmeans.setNumClusters(1);
        kmeans.setMaxIterations(500);
        kmeans.buildClusterer(filteredData);

        return kmeans.getClusterCentroids().firstInstance();
    }

    private int[] getClusterAssignments(Instances classInstances) throws Exception {
        SimpleKMeans kmeans;
        Instances centroids;
        Instances filteredData;
        Remove filter;

        Instances insts = (Instances) MiscUtils.deepCopy(classInstances);

        kmeans = new SimpleKMeans();
        filter = new Remove();

        insts.setClassIndex(classInstances.numAttributes() - 1);

        filter.setAttributeIndices("" + (insts.classIndex() + 1));
        filter.setInputFormat(insts);
        filteredData = Filter.useFilter(insts, filter);

        kmeans.setNumClusters(NoOfClusters);
        kmeans.setMaxIterations(500);
        kmeans.setPreserveInstancesOrder(true);
        kmeans.buildClusterer(filteredData);

        int[] assignments = kmeans.getAssignments();
        Centroids = kmeans.getClusterCentroids();

        return assignments;
    }

    public DoubleVector getWeights() {
        return Weights;
    }

    public void clearWeights() {
        Weights = new DoubleVector();
    }

}
