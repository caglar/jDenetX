package tr.gov.ulakbim.jDenetX.core;

import weka.core.Instances;

import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: May 27, 2010
 * Time: 10:15:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class InstancesHistMap {

    public int ClassesInstsMap[][];
    public Hashtable<Integer, Instances> refinedInstances;

    public InstancesHistMap(int noOfClasses, int noOfClusters) {
        ClassesInstsMap = new int[noOfClasses][noOfClusters];
    }

    private void findMajorityClasses(InstanceClassesClusterPool instHists) {

    }
}
