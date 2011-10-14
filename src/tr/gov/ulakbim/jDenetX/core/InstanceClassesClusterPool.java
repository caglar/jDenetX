package tr.gov.ulakbim.jDenetX.core;

import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: May 20, 2010
 * Time: 4:42:58 PM
 * To change this template use File | Settings | File Templates.
 */

public class InstanceClassesClusterPool {

    public ArrayList<Instance>[][] InstancesClassClusterPool;
    private int NoOfClasses;
    private int NoOfClusters;

    public InstanceClassesClusterPool(int noOfClasses, int noOfClusters) {
        NoOfClasses = noOfClasses;
        NoOfClusters = noOfClusters;
        InstancesClassClusterPool = (ArrayList<Instance>[][]) new ArrayList[noOfClasses][noOfClusters];
    }

    public int getNoOfClusters() {
        return NoOfClusters;
    }

    public void setNoOfClusters(int noOfClusters) {
        NoOfClusters = noOfClusters;
    }


    public int getNoOfClasses() {
        return NoOfClasses;
    }

    public void setNoOfClasses(int noOfClasses) {
        NoOfClasses = noOfClasses;
    }

    public void addInstance(Instance inst, int class_, int cluster) {
        if (InstancesClassClusterPool != null) {
            InstancesClassClusterPool[class_][cluster].add(inst);
        }
    }

    public void addInstancesToClass(int class_, ArrayList<Instance>[] instances) {
        InstancesClassClusterPool[class_] = instances;
    }

    public void addInstancesToClusterClass(ArrayList<Instance> instances, int class_, int cluster) {
        if (instances != null) {
            if (class_ < NoOfClasses && cluster < NoOfClusters) {
                InstancesClassClusterPool[class_][cluster].addAll(instances);
            }
        }
    }

    public void addInstancesToPool(Instances data, int assignments[]) {
        for (int i = 0; i < assignments.length; i++) {
            addInstance(data.get(i), (int) data.get(i).classValue(), assignments[i]);
        }
    }

    public void addInstancesToPool(ArrayList<Instance> data, int assignments[]) {
        for (int i = 0; i < assignments.length; i++) {
            addInstance(data.get(i), (int) data.get(i).classValue(), assignments[i]);
        }
    }

    public ArrayList<Instance> getInstancesForClusterInClass(int class_, int cluster) {
        return InstancesClassClusterPool[class_][cluster];
    }

    public ArrayList<Instance>[] getInstancesInClass(int class_) {
        return InstancesClassClusterPool[class_];
    }

    public ArrayList<Instance> getInstancesOfClustersInClass(int cluster) {
        ArrayList<Instance> rtnClusterInstances = new ArrayList<Instance>();
        for (int i = 0; i < NoOfClasses; i++) {
            rtnClusterInstances.addAll(InstancesClassClusterPool[i][cluster]);
        }
        return rtnClusterInstances;
    }
}