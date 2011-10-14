package tr.gov.ulakbim.jDenetX.sampling;

import weka.core.Instance;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Nov 22, 2010
 * Time: 10:37:59 AM
 * To change this template use File | Settings | File Templates.
 */

public class Reservoir {

    private ArrayList<Instance> ReservoirInstances;
    private int ReservoirLimit = 10000;

    public Reservoir() {
        ReservoirInstances = new ArrayList<Instance>();
    }

    public Reservoir(int resSize) {
        ReservoirInstances = new ArrayList<Instance>();
        ReservoirLimit = resSize;
    }

    public void addInstance(Instance inst) {
        ReservoirInstances.add(inst);
    }

    public int getCurrentResSize() {
        return ReservoirInstances.size();
    }

    public void setInstance(int loc, Instance inst) {
        ReservoirInstances.set(loc, inst);
    }

    public void setReservoirLimit(int limit) {
        ReservoirLimit = limit;
    }

    public boolean isReservoirFilled() {
        return (ReservoirInstances.size() >= ReservoirLimit);
    }

    public Instance getInstance(int instNo) {
        return ReservoirInstances.get(instNo);
    }

    public ArrayList<Instance> getResData() {
        return ReservoirInstances;
    }

    public void cleanReservoir() {
        ReservoirInstances.clear();
    }

}