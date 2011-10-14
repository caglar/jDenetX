package tr.gov.ulakbim.jDenetX.core;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import weka.core.Instance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class VotedInstancePool extends AbstractMOAObject implements Iterable<VotedInstance> {

    /**
     * Serial Version UID
     */

    private static final long serialVersionUID = -8436429750022781583L;
    private static final int MAX_NO_OF_CLASSES = 100;

    //TreeSet is used as a RB tree
    private static TreeSet<VotedInstance> InstConfPairs = new TreeSet<VotedInstance>();
    private static int NoOfClasses = 0;
    ArrayList<Integer> ClassesMap = new ArrayList<Integer>();

    public VotedInstancePool() {
        for (int i = 0; i < MAX_NO_OF_CLASSES; i++) {
            ClassesMap.add(i, 0);
        }
    }

    public void addVotedInstance(Instance inst, double conf, double activeLearningRatio) {
        VotedInstance instConf = new VotedInstance();
        instConf.setActiveLearningRatio(activeLearningRatio);
        instConf.setInstance(inst);
        instConf.setConfidence(conf); //This is redundant

        InstConfPairs.add(instConf);
        if (ClassesMap.get((int) inst.classValue()) != 1) {
            NoOfClasses++;
        }
        ClassesMap.set((int) inst.classValue(), 1);
    }

    public void addVotedInstancePool(VotedInstance vInst) {
        InstConfPairs.add(vInst);
    }

    public Iterator<VotedInstance> iterator() {
        return InstConfPairs.iterator();
    }

    public int getSize() {
        return InstConfPairs.size();
    }


    public int getNoOfClasses() {
        return NoOfClasses;
    }

    public void setNoOfClasses(int noOfClasses) {
        NoOfClasses = noOfClasses;
    }

    public void getDescription(StringBuilder sb, int indent) {
    }
}