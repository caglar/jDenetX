package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.classifiers.ensemble.QBC;
import tr.gov.ulakbim.jDenetX.core.*;
import tr.gov.ulakbim.jDenetX.options.ClassOption;
import tr.gov.ulakbim.jDenetX.options.FlagOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import weka.core.Instance;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 28, 2010
 * Time: 12:13:04 PM
 * To change this template use File | Settings | File Templates.
 */

public class SelfOzaBoost extends AbstractClassifier {

    private static final long serialVersionUID = 1L;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "HoeffdingTree");

    public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's',
            "The number of models to boost.", 10, 1, Integer.MAX_VALUE);

    public FlagOption pureBoostOption = new FlagOption("pureBoost", 'p',
            "Boost with weights only; no poisson.");

    private static VotedInstancePool instConfPool = new VotedInstancePool();

    protected Classifier[] ensemble;

    protected double[] scms;

    protected double[] swms;

    public static int instConfCount = 0;

    private static final double confidenceThreshold = 0.98;

    protected static final double errorRatio = 0.23;

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
        baseLearner.resetLearning();
        for (int i = 0; i < this.ensemble.length; i++) {
            this.ensemble[i] = baseLearner.copy();
        }
        instConfCount = 0;
        this.scms = new double[this.ensemble.length];
        this.swms = new double[this.ensemble.length];
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        double lambda_d = 1.0;
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

    protected double getEnsembleMemberWeight(int i) {
        double em = this.swms[i] / (this.scms[i] + this.swms[i]);
        if ((em == 0.0) || (em > 0.5)) {
            return 0.0;
        }
        double Bm = em / (1.0 - em);
        return Math.log(1.0 / Bm);
    }

    protected double getEnsembleMemberError(int i) {
        double em = this.swms[i] / (this.scms[i] + this.swms[i]);
        return em;
    }

    public double getActiveLearningRatio(double qbcEntropy, DoubleVector combinedVote) {
        int maxIndex = combinedVote.maxIndex();
        int ensembleLength = this.ensemble.length;
        double maxVote = combinedVote.getValue(maxIndex);
        double activeLearningRatio = (qbcEntropy) * (maxVote / ensembleLength);
        return activeLearningRatio;
    }

    int count = 0;

    public double[] getVotesForInstance(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        DoubleVector confidenceVec = new DoubleVector();

        int success = 0;
        double[] ensembleVotes = new double[inst.numClasses()];
        double[] ensMemberWeights = new double[this.ensemble.length];
        boolean[] ensMemberFlags = new boolean[this.ensemble.length];
        double confidence = 0.0;

        for (int i = 0; i < this.ensemble.length; i++) {
            if (!ensMemberFlags[i]) {
                ensMemberWeights[i] = getEnsembleMemberWeight(i);
            }
            if (ensMemberWeights[i] > 0.0) {
                DoubleVector vote = new DoubleVector(this.ensemble[i]
                        .getVotesForInstance(inst));
                if (vote.sumOfValues() > 0.0) {
                    vote.normalize();
                    vote.scaleValues(ensMemberWeights[i]);
                    combinedVote.addValues(vote);
                    if (getEnsembleMemberError(i) < errorRatio) {
                        //
                        // these are the votes of the ensembles for the classes
                        //
                        success++;
                        //successFlag = true;
                        confidenceVec.addValues(vote);
                        ensembleVotes[confidenceVec.maxIndex()] += confidenceVec.getValue(confidenceVec.maxIndex());
                    }
                }
            } else {
                break;
            }
        }

        confidenceVec = (DoubleVector) combinedVote.copy();
        confidenceVec.normalize();
        confidence = confidenceVec.getValue(confidenceVec.maxIndex());
        //Reconfigure the activeLearningRatio
        //For confidence measure add to the pool  and in order to fit the confidence value between 0 and 1 divide by success val
        if (confidence > confidenceThreshold) {
            double qbcEntropy = QBC.queryByCommitee(ensembleVotes, inst.numClasses(), success, ensemble.length);
            Math.pow(qbcEntropy, 2);
            System.out.println("QBC Entropy: " + qbcEntropy);
            double activeLearningRatio = getActiveLearningRatio(qbcEntropy, combinedVote);
            inst.setClassValue(combinedVote.maxIndex()); //Set the class value of the instance
            instConfPool.addVotedInstance(inst, combinedVote
                    .getValue(combinedVote.maxIndex()), activeLearningRatio);
            instConfCount++;
        }

        return combinedVote.getArrayRef();
    }

    public static VotedInstancePool getVotedInstancePool() {
        return instConfPool;
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
