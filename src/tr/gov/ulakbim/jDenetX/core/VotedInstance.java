package tr.gov.ulakbim.jDenetX.core;

import weka.core.Instance;

/**
 * @author caglar
 *         Defines the interface for the Instance and Confidence pairs.
 */

public class VotedInstance implements Comparable<Object> {

    private int Priority;

    private Instance Instance;

    private double Confidence;

    private double Entropy;

    private double ActiveLearningRatio;

    public void setInstance(Instance instance) {
        this.Instance = instance;
    }

    public Instance getInstance() {
        return this.Instance;
    }

    public void setConfidence(double confidence) {
        this.Confidence = confidence;
    }

    public double getConfidence() {
        return this.Confidence;
    }

    public void setPriority(int p) {
        this.Priority = p;
    }

    public int getPriority() {
        return this.Priority;
    }

    public void setEntropy(double entropy) {
        this.Entropy = entropy;
    }

    public double getEntropy() {
        return this.Entropy;
    }

    public void setActiveLearningRatio(double activeLearningRatio) {
        ActiveLearningRatio = activeLearningRatio;
    }

    public double getActiveLearningRatio() {
        return ActiveLearningRatio;
    }

    public int compareTo(Object o) {
        if (ActiveLearningRatio == ((VotedInstance) o).getActiveLearningRatio()) {
            return 0;
        } else if (ActiveLearningRatio < ((VotedInstance) o).getActiveLearningRatio()) {
            return 1;
        } else {
            return -1;
        }
    }
}
