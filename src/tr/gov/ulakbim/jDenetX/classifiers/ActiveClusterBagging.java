package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.core.*;
import tr.gov.ulakbim.jDenetX.options.ClassOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Collections;

public class ActiveClusterBagging extends AbstractClassifier {

    private static final long serialVersionUID = 1L;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "ASHoeffdingTreeNB");

    public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's',
            "The number of models in the bag.", 10, 1, Integer.MAX_VALUE);

    public IntOption windowSizeOption = new IntOption(
            "windowSize", 'w',
            "The size of window.", 1000, 10, Integer.MAX_VALUE);

    protected InstanceClassesPool ClassPool = new InstanceClassesPool();

    protected Classifier[] ensemble;

    @Override
    public int measureByteSize() {
        int size = (int) SizeOf.sizeOf(this);
        for (Classifier classifier : this.ensemble) {
            size += classifier.measureByteSize();
        }
        return size;
    }

    @Override
    public void resetLearningImpl() {
        this.ensemble = new Classifier[this.ensembleSizeOption.getValue()];
        ClassPool = new InstanceClassesPool();
        Classifier baseLearner = (Classifier) getPreparedClassOption(this.baseLearnerOption);
        baseLearner.resetLearning();
        for (int i = 0; i < this.ensemble.length; i++) {
            this.ensemble[i] = baseLearner.copy();
        }
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        int trueClass = (int) inst.classValue();
        int windowSize = windowSizeOption.getValue();
        if (!ClassPool.isInitialized()) {
            ClassPool.initialize(inst.numClasses(), Collections.list(inst.enumerateAttributes()), windowSize);
        }
        ClassPool.addInstance(inst);

        if (ClassPool.checkPoolSize(windowSize)) {
            ClusterTrainingDataHarvester ctdh = new ClusterTrainingDataHarvester(this.ensemble.length);
            for (int c = 0; c < inst.numClasses(); c++) {
                Instances[] insts = ctdh.getEnsembleTrainingData(ClassPool.getInstancesInClass(c), ClassPool.getNoOfClasses());
                for (int i = 0; i < this.ensemble.length; i++) {
                    for (int j = 0; j < insts[i].size(); j++) {
                        this.ensemble[i].trainOnInstance(insts[i].get(j));
                    }
                }
            }
            ClassPool.clear();
        }
    }

    public double[] getVotesForInstance(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        for (int i = 0; i < this.ensemble.length; i++) {
            DoubleVector vote = new DoubleVector(this.ensemble[i]
                    .getVotesForInstance(inst));
            if (vote.sumOfValues() > 0.0) {
                vote.normalize();
                combinedVote.addValues(vote);
            }
        }
        return combinedVote.getArrayRef();
    }

    public boolean isRandomizable() {
        return true;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        // TODO Auto-generated method stub
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return new Measurement[]{new Measurement("ensemble size",
                this.ensemble != null ? this.ensemble.length : 0)};
    }

    @Override
    public Classifier[] getSubClassifiers() {
        return this.ensemble.clone();
    }

}
