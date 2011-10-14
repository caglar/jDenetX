package tr.gov.ulakbim.jDenetX.core;

import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: May 20, 2010
 * Time: 4:42:58 PM
 * To change this template use File | Settings | File Templates.
 */

public class InstancesHists {

    public Hashtable<Integer, Instances> InstsClasses;
    public Hashtable<Integer, Instances> InstsBuffer;
    public int ClassesInstsMap[][];
    public Instances insts;
    public int NoOfClasses;
    public int NoOfClusters;
    private int ClusterInstancesRatio;
    public ArrayList<Integer> BestClassesForClusters;

    public InstancesHists(int noOfClasses, int noOfClusters) {
        NoOfClasses = noOfClasses;
        NoOfClusters = noOfClusters;
        ClusterInstancesRatio = 2;
        ClassesInstsMap = new int[noOfClusters][noOfClasses];
        BestClassesForClusters = new ArrayList<Integer>();
        InstsClasses = new Hashtable<Integer, Instances>();
    }

    public int getNoOfClasses() {
        return NoOfClasses;
    }

    public void addInstance(Instance inst, Integer cluster) {
        Instances insts = InstsClasses.get(cluster);
        insts.add(inst);
        InstsClasses.put(cluster, insts);
    }

    public int getClusterInstancesRatio() {
        return ClusterInstancesRatio;
    }

    public void setClusterInstancesRatio(int clusterInstancesRatio) {
        ClusterInstancesRatio = clusterInstancesRatio;
    }

    public void setNoOfClasses(int noOfClasses) {
        NoOfClasses = noOfClasses;
    }

    public void getMajorityClasses() {
        Enumeration keys = InstsClasses.keys();
        Integer key = 0;
        Instances values = null;
        Instance inst = null;
        while (keys.hasMoreElements()) {
            key = (Integer) keys.nextElement();
            values = InstsClasses.get(key);
            for (int i = 0; i < values.numInstances(); i++) {
                inst = values.instance(i);
                ClassesInstsMap[key][(int) inst.classValue()]++;
            }
        }
        findBestClassesForCluster();
    }

    public void findBestClassesForCluster() {
        for (int i = 0; i < NoOfClusters; i++) {
            int maxFreq = 0;
            int currentClass = 0;

            for (int j = 0; j < NoOfClasses; j++) {
                if (maxFreq < ClassesInstsMap[i][j]) {
                    maxFreq = ClassesInstsMap[i][j];
                    currentClass = j;
                }
            }
            if (BestClassesForClusters.contains((Integer) currentClass) &&
                    (ClassesInstsMap[i][(Integer) BestClassesForClusters.get(i)] / InstsClasses.get(i).numInstances()) > ClusterInstancesRatio) {
                BestClassesForClusters.set(i, -1);
            } else {
                BestClassesForClusters.set(i, currentClass);
            }
        }
    }

    public Instances getInstancesForCluster(Integer clusterNo) {
        return InstsClasses.get(clusterNo);
    }
}
