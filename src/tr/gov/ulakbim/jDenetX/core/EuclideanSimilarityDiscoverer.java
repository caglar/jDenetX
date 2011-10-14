package tr.gov.ulakbim.jDenetX.core;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import weka.core.*;
import weka.core.logging.Logger;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Oct 18, 2010
 * Time: 9:42:50 AM
 * To change this template use File | Settings | File Templates.
 */

public class EuclideanSimilarityDiscoverer extends AbstractMOAObject {

    private Instances SimilarityInstances;
    private ArrayList<Attribute> AttList;
    private static final int DefaultCapacity = 2;
    private Instance Center;
    private static int ProcessedInstances = 0;

    public EuclideanSimilarityDiscoverer(ArrayList<Attribute> attList) {
        AttList = attList;
        SimilarityInstances = new Instances("similarity_insts", attList, DefaultCapacity);
        SimilarityInstances.setClassIndex(attList.size() - 1);
        Center = new DenseInstance(attList.size());
    }

    public void addInstance(Instance inst) {
        if (SimilarityInstances.size() == 0) {
            SimilarityInstances.add(inst);
            for (int i = 0; i < Center.numAttributes(); i++) {
                Center.setValue(i, inst.value(i));
            }
        } else {

            if (SimilarityInstances.size() == 1) {
                SimilarityInstances.add(1, inst);
            } else {
                SimilarityInstances.set(1, inst);
            }
            try {
                ProcessedInstances++;
                Center = getCenter();
            } catch (Exception e) {
                Logger.log(Logger.Level.WARNING, e.getMessage());
            }
            SimilarityInstances.set(0, Center);
        }
    }

    protected Instance getCenter() throws Exception {
        if (SimilarityInstances.size() > 0) {
            Center = new DenseInstance(SimilarityInstances.numAttributes());
            Center.setDataset(SimilarityInstances);

            final double InstanceWeight = 1 / ProcessedInstances;
            final double CenterWeight = 1 - InstanceWeight;

            SimilarityInstances.get(0).setWeight(CenterWeight);
            SimilarityInstances.get(1).setWeight(InstanceWeight);

            for (int i = 0; i < Center.numAttributes() - 1; i++) {
                Center.setValue(i, SimilarityInstances.meanOrMode(i));
            }
        } else {
            throw new Exception("Warning SimilarityInstances is empty, can't find the center!");
        }
        return Center;
    }

    public double findDistanceToCenteroid(Instance inst) {
        double distance = 0.0;
        try {
            Center = SimilarityInstances.firstInstance();
            if (Center != null) {
                EuclideanDistance eucDistFun = new EuclideanDistance();
                eucDistFun.setInstances(SimilarityInstances);
                distance = eucDistFun.distance(inst, Center);
            } else {
                throw new NullPointerException("Center is null!");
            }
        } catch (Exception e) {
            Logger.log(Logger.Level.WARNING, e.getMessage());
            e.printStackTrace();
            System.out.println("Center" + Center);
            System.out.println("Instance" + inst);
        }
        return distance;
    }

    public void clear() {
        if (SimilarityInstances != null || SimilarityInstances.size() > 0) {
            SimilarityInstances.clear();
        }
    }

    public int size() {
        return SimilarityInstances.size();
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * MultilabelInstancesHeader.
     */
    public static class MultilabelInstancesHeader extends InstancesHeader {

        private int m_NumLabels = -1;

        public MultilabelInstancesHeader(Instances i, int numLabels) {
            super(i);
            m_NumLabels = numLabels;
        }

        public int getNumClassLabels() {
            return m_NumLabels;
        }
    }
}