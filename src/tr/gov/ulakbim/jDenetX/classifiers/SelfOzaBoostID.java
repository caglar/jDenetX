package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.classifiers.ensemble.QBC;
import tr.gov.ulakbim.jDenetX.core.*;
import tr.gov.ulakbim.jDenetX.options.ClassOption;
import tr.gov.ulakbim.jDenetX.options.FlagOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Oct 18, 2010
 * Time: 9:23:26 AM
 * To change this template use File | Settings | File Templates.
 */

public class SelfOzaBoostID extends AbstractClassifier {

    private static final long serialVersionUID = 1L;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "ASHoeffdingOptionTree");

    public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's',
            "The number of models to boost.", 10, 1, Integer.MAX_VALUE);

    public FlagOption pureBoostOption = new FlagOption("pureBoost", 'p',
            "Boost with weights only; no poisson.");

    public FlagOption resetTreesOption = new FlagOption("resetTrees", 'r',
            "Reset trees when size is higher than the max. Requires ASHoeffdingOptionTree");

    public IntOption firstClassifierSizeOption = new IntOption(
            "firstClassifierSize", 'f',
            "The size of first classifier in the bag. This option will have effect with only ASHoeffdingOptionTree", 80, 1, Integer.MAX_VALUE);

    private static VotedInstancePool instConfPool = new VotedInstancePool();

    protected Classifier[] ensemble;

    private ArrayList<Attribute> AttList = null;

    private Instances ClassificationInstsPool = null;

    EuclideanSimilarityDiscoverer TrainingSimilarity = null;

    EuclideanSimilarityDiscoverer ClassificationSimilarity = null;

    protected double[] scms;

    protected double[] swms;

    private static final double ConfidenceThreshold = 0.94;

    private static final int ReservoirSize = 10000;

    protected static final double ErrorRatio = 0.28;

    private DoubleVector QBCs = new DoubleVector();

    private static DoubleVector Confidences = new DoubleVector();

    private int ClassifiedInstances = 0;

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
        Classifier baseLearner = (Classifier) getPreparedClassOption(this.baseLearnerOption);
        QBCs = new DoubleVector();
        ClassifiedInstances = 0;
        ClassificationSimilarity = null;
        baseLearner.resetLearning();
        int pow = this.firstClassifierSizeOption.getValue(); // EXTENSION TO ASHT
        for (int i = 0; i < this.ensemble.length; i++) {
            this.ensemble[i] = baseLearner.copy();
            if ((this.resetTreesOption != null)
                    && this.resetTreesOption.isSet()) {
                ((ASHoeffdingOptionTree) this.ensemble[i]).setMaxSize(pow); // EXTENSION TO ASHT
                ((ASHoeffdingOptionTree) this.ensemble[i]).setResetTree();
                pow *= 2; //EXTENSION TO ASHT
            }
        }
        this.scms = new double[this.ensemble.length];
        this.swms = new double[this.ensemble.length];
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        double lambda_d = 1.0;
        if (TrainingSimilarity == null) {
            AttList = (ArrayList<Attribute>) Collections.list(inst.enumerateAttributes()); //new ArrayList<Attribute>();
            AttList.add(inst.attribute(inst.classIndex()));
            TrainingSimilarity = new EuclideanSimilarityDiscoverer(AttList);
        }
        TrainingSimilarity.addInstance(inst);
        for (int i = 0; i < this.ensemble.length; i++) {
            double k = this.pureBoostOption.isSet() ? lambda_d : MiscUtils
                    .poisson(lambda_d, this.classifierRandom);
            if (k > 0.0) {
                Instance weightedInst = (Instance) inst.copy();
                weightedInst.setWeight(inst.weight() * k);
                this.ensemble[i].trainOnInstance(weightedInst);
            }
            if (this.ensemble[i].correctlyClassifies(inst)) {
                this.scms[i] += lambda_d;
                lambda_d *= this.trainingWeightSeenByModel / (2 * this.scms[i]);
            } else {
                this.swms[i] += lambda_d;
                lambda_d *= this.trainingWeightSeenByModel / (2 * this.swms[i]);
            }
        }
    }

    public double getEnsembleMemberError(int i) {
        double em = this.swms[i] / (this.scms[i] + this.swms[i]);
        return em;
    }

    protected double getEnsembleMemberWeight(int i) {
        double em = getEnsembleMemberError(i);
        if ((em == 0.0) || (em > 0.5)) {
            return 0.0;
        }
        double Bm = em / (1.0 - em);
        return Math.log(1.0 / Bm);
    }

    public double unlabeledSimilarity(Instance inst, double beta) {
        double unlabeledSimilarity = Math.pow(1 / (ClassificationSimilarity.findDistanceToCenteroid(inst)), beta);
        return unlabeledSimilarity;
    }

    public double labeledSimilarity(Instance inst) {
        double labeledSimilarity = 10 * Math.exp(-(1 / TrainingSimilarity.findDistanceToCenteroid(inst)));
        return labeledSimilarity;
    }

    public double getActiveLearningRatio(Instance inst, double qbc) {
        double activeLearnRatio;
        double beta = 1;
        activeLearnRatio = qbc * unlabeledSimilarity(inst, beta) * labeledSimilarity(inst);
        return activeLearnRatio;
    }

    private void addToVotedInstances(Instance inst, double qbc, double confidence) {
        if (confidence > ConfidenceThreshold) {
            double activeLearningRatio = getActiveLearningRatio(inst, qbc);
            instConfPool.addVotedInstance(inst, confidence, activeLearningRatio);
        }
    }

    public DoubleVector calculateVotesForInstance(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        DoubleVector confidenceVote = new DoubleVector();
        double[] ensMemberWeights = new double[this.ensemble.length];
        boolean[] ensMemberFlags = new boolean[this.ensemble.length];
        double[][] comitteeVotes = new double[this.ensemble.length][inst.numClasses()];

        int success = 0;
        double qbc;
        double confidence = 0.0;

        for (int i = 0; i < this.ensemble.length; i++) {
            if (!ensMemberFlags[i]) {
                ensMemberWeights[i] = getEnsembleMemberWeight(i);
                ensMemberFlags[i] = true;
            }
            if (ensMemberWeights[i] > 0.0) {
                DoubleVector vote = new DoubleVector(this.ensemble[i]
                        .getVotesForInstance(inst));
                if (vote.sumOfValues() > 0.0) {
                    vote.scaleValues(ensMemberWeights[i]);
                    vote.normalize();
                    combinedVote.addValues(vote);
                    if (getEnsembleMemberError(i) < ErrorRatio && (((ASHoeffdingOptionTree) this.ensemble[i]).measureTreeDepth() > 1)) {
                        success++;
                        confidenceVote.addValues(vote);
                        comitteeVotes[i] = vote.getArrayRef();
                    }
                }
            } else {
                break;
            }
        }

        if (confidenceVote.numValues() > 0) {
            confidenceVote.normalize();
            confidence = confidenceVote.maxValue();
        }

        qbc = QBC.getKullbackLeiblerDiv(comitteeVotes, confidenceVote, success);
        combinedVote = (DoubleVector) confidenceVote.copy();

        //Similar to the reservoir sampling
        if (ClassifiedInstances == ReservoirSize) {
            QBCs.setValue(ClassifiedInstances, qbc);
            ClassificationInstsPool.add(inst);
            for (int j = 0; j < ClassificationInstsPool.size(); j++) {
                ClassificationInstsPool.get(j).setClassValue(combinedVote.maxIndex()); //Set the class value of the instance
                addToVotedInstances(ClassificationInstsPool.get(j), QBCs.getValue(j), Confidences.getValue(j));
            }
            addToVotedInstances(inst, qbc, confidence);
        } else if (ClassifiedInstances > ReservoirSize) {
            inst.setClassValue(combinedVote.maxIndex());
            addToVotedInstances(inst, qbc, confidence);
        } else {
            QBCs.setValue(ClassifiedInstances, qbc);
            ClassificationInstsPool.add(inst);
            Confidences.setValue(ClassifiedInstances, confidence);
        }
        ClassificationSimilarity.addInstance(inst);
        return combinedVote;
    }

    public double[] getVotesForInstance(Instance inst) {
        DoubleVector combinedVote;
        if (ClassificationSimilarity == null) {
            if (AttList != null) {
                ClassificationSimilarity = new EuclideanSimilarityDiscoverer(AttList);
            } else {
                AttList = Collections.list(inst.enumerateAttributes()); //new ArrayList<Attribute>();
                AttList.add(inst.attribute(inst.classIndex()));
                ClassificationSimilarity = new EuclideanSimilarityDiscoverer(AttList);
            }
        }

        if (ClassificationInstsPool == null) {
            ClassificationInstsPool = new Instances("instancePool", AttList, ReservoirSize);
            ClassificationInstsPool.setClassIndex(AttList.size() - 1);
        }
        combinedVote = calculateVotesForInstance(inst);
        ClassifiedInstances++;
        return combinedVote.getArrayRef();
    }

    public boolean isRandomizable() {
        return true;
    }

    public static VotedInstancePool getVotedInstancePool() {
        return instConfPool;
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