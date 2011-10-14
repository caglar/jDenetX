package tr.gov.ulakbim.jDenetX.core;

/*
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 2, 2010
 * Time: 2:47:04 PM
 * To change this template use File | Settings | File Templates.
 */

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;

public class InstanceClassesPool extends AbstractMOAObject {

    public Instances[] InstancesClassPool = null;
    private static int NoOfClasses = 0;
    private static int Size = 0;

    public InstanceClassesPool() {
    }

    public InstanceClassesPool(int noOfClasses, ArrayList<Attribute> attList, int winSize) {
        NoOfClasses = noOfClasses;
        InstancesClassPool = new Instances[noOfClasses];
        Size = 0;
        for (int i = 0; i < noOfClasses; i++) {
            InstancesClassPool[i] = new Instances("classList", attList, winSize);
            InstancesClassPool[i].setClassIndex(attList.size() - 1);
        }
    }

    public boolean isInitialized() {
        boolean rtnVal = false;
        if (InstancesClassPool != null && NoOfClasses > 0 && (InstancesClassPool.length == NoOfClasses)) {
            rtnVal = true;
        }
        return rtnVal;
    }

    public void initialize(int noOfClasses, ArrayList<Attribute> attList, int winSize) {
        NoOfClasses = noOfClasses;
        InstancesClassPool = new Instances[noOfClasses];
        Size = 0;
        for (int i = 0; i < noOfClasses; i++) {
            InstancesClassPool[i] = new Instances("classList", attList, winSize);
            InstancesClassPool[i].setClassIndex(attList.size() - 1);
        }
    }

    public int getNoOfClasses() {
        return NoOfClasses;
    }

    public void setNoOfClasses(int noOfClasses) {
        NoOfClasses = noOfClasses;
    }

    public void addInstance(Instance inst) {
        if (InstancesClassPool != null) {
            InstancesClassPool[(int) inst.classValue()].add(inst);
            //InstancesClassPool[(int)inst.classValue()].setClassIndex(inst.classIndex());
            Size++;
        }
    }

    public void addInstances(Instances insts, int class_) {
        if (InstancesClassPool != null) {
            InstancesClassPool[class_].addAll(insts);
            Size += insts.size();
        }
    }

    public void setInstancesToClass(int class_, Instances instances) {
        if (InstancesClassPool[class_] != null) {
            Size -= InstancesClassPool[class_].size();
        }
        InstancesClassPool[class_] = instances;
        Size += instances.size();
    }

    public void addInstancesToPool(Instances data) {
        for (int i = 0; i < data.size(); i++) {
            addInstance(data.get(i));
        }
        Size += data.size();
    }

    public Instances getInstancesInClass(int class_) {
        return InstancesClassPool[class_];
    }

    public int size() {
        return Size;
    }

    public int calculateSize() {
        int size = 0;
        for (int i = 0; i < NoOfClasses; i++) {
            size += InstancesClassPool[i].size();
        }
        return size;
    }

    public boolean checkPoolSize(int poolSize) {
        boolean result = false;
        boolean sizeFlag = true;
        int size = 0;
        for (int i = 0; i < NoOfClasses; i++) {
            size += InstancesClassPool[i].size();
            if (InstancesClassPool[i].size() < NoOfClasses - 1) {
                sizeFlag = false;
                break;
            }
        }
        if (sizeFlag && size >= poolSize) {
            result = true;
        }
        return result;
    }

    public void clear() {
        for (int i = 0; i < NoOfClasses; i++) {
            InstancesClassPool[i].clear();
        }
        Size = 0;
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}