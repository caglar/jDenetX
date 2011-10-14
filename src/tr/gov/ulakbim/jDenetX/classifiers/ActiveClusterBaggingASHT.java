package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.classifiers.ensemble.QBC;
import tr.gov.ulakbim.jDenetX.core.ClusterTrainingDataHarvester;
import tr.gov.ulakbim.jDenetX.core.DoubleVector;
import tr.gov.ulakbim.jDenetX.core.InstanceClassesPool;
import tr.gov.ulakbim.jDenetX.core.VotedInstancePool;
import tr.gov.ulakbim.jDenetX.options.FlagOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 1, 2010
 * Time: 2:55:17 PM
 * To change this template use File | Settings | File Templates.
 */

public class ActiveClusterBaggingASHT extends ActiveClusterBagging {

    private static final long serialVersionUID = 1L;

    public IntOption firstClassifierSizeOption = new IntOption(
            "firstClassifierSize", 'f',
            "The size of first classifier in the bag.", 1, 1, Integer.MAX_VALUE);

    public FlagOption useWeightOption = new FlagOption("useWeight", 'u',
            "Enable weight classifiers.");

    private Instances Insts = null;

    public FlagOption resetTreesOption = new FlagOption("resetTrees", 'r',
            "Reset trees when size is higher than the max.");

    private static VotedInstancePool instConfPool = new VotedInstancePool();

    protected double[] error;

    private final static boolean PoolFlag = false;

    protected final static double errorRatio = 0.23;

    protected double alpha = 0.01;

    public static int instConfCount = 0;

    private final static double confidenceThreshold = 0.97;

    private static boolean checkSize = true;

    @Override
    public void resetLearningImpl() {
        this.ensemble = new Classifier[this.ensembleSizeOption.getValue()];
        this.error = new double[this.ensembleSizeOption.getValue()];
        instConfPool = new VotedInstancePool();
        instConfCount = 0;
        Classifier baseLearner = (Classifier) getPreparedClassOption(this.baseLearnerOption);
        baseLearner.resetLearning();
        ClassPool = new InstanceClassesPool();
        int pow = this.firstClassifierSizeOption.getValue(); // EXTENSION TO ASHT
        checkSize = true;

        for (int i = 0; i < this.ensemble.length; i++) {
            this.ensemble[i] = baseLearner.copy();
            this.error[i] = 0.0;
            ((ASHoeffdingOptionTree) this.ensemble[i]).setMaxSize(pow); // EXTENSION TO ASHT
            if ((this.resetTreesOption != null)
                    && this.resetTreesOption.isSet()) {
                ((ASHoeffdingOptionTree) this.ensemble[i]).setResetTree();
            }
            pow *= 2; // EXTENSION TO ASHT
        }
    }

    public void updateError(Instance inst, int ensembleNo) {
        int trueClass = (int) inst.classValue();
        if (Utils.maxIndex(this.ensemble[ensembleNo].getVotesForInstance(inst)) == trueClass) { // Here we used the getVotesForInstanceFunction of HoeffdingTree
            this.error[ensembleNo] += alpha * (0.0 - this.error[ensembleNo]); // EWMA
        } else {
            this.error[ensembleNo] += alpha * (1.0 - this.error[ensembleNo]); // EWMA
        }
    }

    public void trainByClassPool(Instance inst, int windowSize) {
        int instsInitSize = 0;
        int instsSecondarySize = 0;
        if (!ClassPool.isInitialized()) {
            ArrayList attList = Collections.list(inst.enumerateAttributes());
            attList.add(inst.attribute(inst.classIndex()));
            ClassPool.initialize(inst.numClasses(), attList, windowSize);
        }
        ClassPool.addInstance(inst);
        if (ClassPool.checkPoolSize(windowSize) && checkSize) {
            ClusterTrainingDataHarvester ctdh = new ClusterTrainingDataHarvester(this.ensemble.length);
            Instances[] trainingInstances = new Instances[this.ensemble.length];
            for (int c = 0; c < inst.numClasses(); c++) {
                Instances[] insts = ctdh.getEnsembleTrainingData(ClassPool.getInstancesInClass(c), ClassPool.getNoOfClasses());
                for (int i = 0; i < this.ensemble.length; i++) {
                    instsInitSize = insts[i].size();
                    if (trainingInstances[i] == null) {
                        trainingInstances[i] = insts[i];
                    } else {
                        for (int j = 0; j < instsInitSize; j++) {
                            Instance instance = insts[i].get(j);
                            updateError(instance, i);
                            trainingInstances[i].add(instance);
                        }
                    }
                    if (i != (insts.length - 1)) {
                        instsSecondarySize = insts[i + 1].size();
                        for (int j = 0; j < instsSecondarySize; j++) {
                            Instance instance = insts[i + 1].get(j);
                            updateError(instance, i);
                            trainingInstances[i].add(instance);
                        }
                    } else {
                        int currentEnsemble = (int) (System.nanoTime() % (insts.length - 1));
                        instsSecondarySize = insts[currentEnsemble].size();
                        for (int j = 0; j < instsSecondarySize; j++) {
                            Instance instance = insts[currentEnsemble].get(j);
                            updateError(instance, i);
                            trainingInstances[i].add(instance);
                        }
                    }
                }
            }
            for (int i = 0; i < trainingInstances.length; i++) {
                Instances ensTrainInsts = trainingInstances[i];
                Collections.shuffle(ensTrainInsts);
                for (Instance tInst : ensTrainInsts) {
                    this.ensemble[i].trainOnInstance(tInst);
                }
            }
            ClassPool.clear();
        }
    }

    public void trainByWholePool(Instance inst, int windowSize) {
        int instsInitSize = 0;
        int instsSecondarySize = 0;

        if (Insts == null) {
            ArrayList attList = Collections.list(inst.enumerateAttributes());
            attList.add(inst.attribute(inst.classIndex()));
            Insts = new Instances("instancePool", attList, windowSize);
            Insts.setClassIndex(attList.size() - 1);
            Insts.add(inst);
        } else {
            Insts.add(inst);
        }
        if (Insts.size() >= windowSize && checkSize) {
            ClusterTrainingDataHarvester ctdh = new ClusterTrainingDataHarvester(this.ensemble.length);
            Instances[] insts = ctdh.getEnsembleTrainingData(Insts, Insts.numClasses());
            for (int i = 0; i < this.ensemble.length; i++) {
                instsInitSize = insts[i].size();
                //Train all of them with the current classifier's cluster
                for (int j = 0; j < instsInitSize; j++) {
                    updateError(insts[i].get(j), i);
                    this.ensemble[i].trainOnInstance(insts[i].get(j));
                }
                //If it is not the last cluster
                if (i != (insts.length - 1)) {
                    instsSecondarySize = insts[i + 1].size();
                    for (int j = 0; j < instsSecondarySize; j++) {
                        updateError(insts[i + 1].get(j), i);
                        this.ensemble[i].trainOnInstance(insts[i + 1].get(j));
                    }
                } else {
                    //If it is the last ensemble
                    int currentEnsemble = (int) (System.nanoTime() % (insts.length - 1));
                    instsSecondarySize = insts[currentEnsemble].size();
                    for (int j = 0; j < instsSecondarySize; j++) {
                        updateError(insts[currentEnsemble].get(j), i);
                        this.ensemble[i].trainOnInstance(insts[currentEnsemble].get(j));
                    }
                }
            }
            Insts.clear();
        }
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        int windowSize = windowSizeOption.getValue();
        if (PoolFlag) {
            trainByClassPool(inst, windowSize);
        } else {
            trainByWholePool(inst, windowSize);
        }

    }

    public double getActiveLearningRatio(double qbcEntropy, DoubleVector combinedVote) {
        int maxIndex = combinedVote.maxIndex();
        int ensembleLength = this.ensemble.length;
        double maxVote = combinedVote.getValue(maxIndex);
        double activeLearningRatio = Math.pow(qbcEntropy, 2) * (maxVote / ensembleLength);
        return activeLearningRatio;
    }

    /**
     * This is the main classification function that is used by the GUI
     */
    public double[] getVotesForInstance(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        DoubleVector confidenceVec = new DoubleVector();
//        double[] ensembleVotes = new double[inst.numClasses()];
        double[][] comitteeVotes = new double[this.ensemble.length][inst.numClasses()];
        double kullLeib = 0.0;
        int success = 0;
        double confidence = 0.0;

        for (int i = 0; i < this.ensemble.length; i++) {
            DoubleVector vote = new DoubleVector(this.ensemble[i]
                    .getVotesForInstance(inst));
            if (vote.sumOfValues() > 0.0) {
                vote.normalize();
                combinedVote.addValues(vote);
                if ((this.useWeightOption != null)
                        && this.useWeightOption.isSet()) {
                    vote.scaleValues(1.0 / (this.error[i] * this.error[i]));
                }
                //kullLeib += (1 / (this.ensemble.length)) * QBC.getKullbackLeiblerDiv(combinedVote.getArrayRef(), inst.numClasses(), this.ensemble.length);
                if (this.error[i] < errorRatio) {
                    //
                    // these are the votes of the ensembles for the classes
                    //
                    success++;
                    confidenceVec.addValues(vote);
//                    ensembleVotes[confidenceVec.maxIndex()] += vote.getValue(confidenceVec.maxIndex());
                    comitteeVotes[i] = vote.getArrayRef();
                }
            }
        }
        if (confidenceVec.numValues() > 0) {
            confidenceVec = (DoubleVector) combinedVote.copy();
            //confidenceVec.normalize();
            //System.out.println(confidenceVec);
            confidence = (confidenceVec.maxValue());
            Utils.logs2probs(confidenceVec.getArrayRef());
            confidence = confidenceVec.maxValue();
        }
        //Reconfigure the activeLearningRatio
        //For confidence measure add to the pool  and in order to fit the confidence value between 0 and 1 divide by success val

        if (confidence >= confidenceThreshold) {
            //kullLeib = QBC.queryByCommitee(ensembleVotes, inst.numClasses(), success, ensemble.length);
            kullLeib = QBC.getKullbackLeiblerDiv(comitteeVotes, confidenceVec, success);
            double activeLearningRatio = getActiveLearningRatio(kullLeib, combinedVote);
            inst.setClassValue(combinedVote.maxIndex()); //Set the class value of the instance
            instConfPool.addVotedInstance(inst, combinedVote
                    .getValue(combinedVote.maxIndex()), activeLearningRatio);
            instConfCount++;
        }
        return combinedVote.getArrayRef();
    }

    /**
     * This is the main classification function that is used by the GUI
     */
    public double[] getVotesForInstance_ori(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        DoubleVector confidenceVec = new DoubleVector();
        double[] ensembleVotes = new double[inst.numClasses()];
        double qbcEntropy = 0.0;
        int success = 0;
        double confidence = 0.0;

        for (int i = 0; i < this.ensemble.length; i++) {
            DoubleVector vote = new DoubleVector(this.ensemble[i]
                    .getVotesForInstance(inst));
            if (vote.sumOfValues() > 0.0) {
                vote.normalize();
                combinedVote.addValues(vote);
                if ((this.useWeightOption != null)
                        && this.useWeightOption.isSet()) {
                    vote.scaleValues(1.0 / (this.error[i] * this.error[i]));
                }
                if (this.error[i] < errorRatio && ((ASHoeffdingOptionTree) this.ensemble[i]).measureTreeDepth() > 2) {
                    //
                    // these are the votes of the ensembles for the classes
                    //
                    success++;
                    confidenceVec.addValues(vote);
                    ensembleVotes[confidenceVec.maxIndex()] += confidenceVec.getValue(confidenceVec.maxIndex());
                }
            }
        }
        if (confidenceVec.numValues() > 0) {
            confidenceVec = (DoubleVector) combinedVote.copy();
            confidenceVec.normalize();
            confidence = (confidenceVec.maxValue());
        }
        //Reconfigure the activeLearningRatio
        //For confidence measure add to the pool  and in order to fit the confidence value between 0 and 1 divide by success val
        if (confidence >= confidenceThreshold) {
            qbcEntropy = QBC.queryByCommitee(ensembleVotes, inst.numClasses(), success, ensemble.length);
            double activeLearningRatio = getActiveLearningRatio(qbcEntropy, combinedVote);
            inst.setClassValue(combinedVote.maxIndex()); //Set the class value of the instance
            instConfPool.addVotedInstance(inst, combinedVote
                    .getValue(combinedVote.maxIndex()), activeLearningRatio);
            instConfCount++;
        }
        return confidenceVec.getArrayRef();//combinedVote.getArrayRef();
    }

    public static VotedInstancePool getVotedInstancePool() {
        return instConfPool;
    }

    public void setCheckSize(boolean sizeControl) {
        checkSize = sizeControl;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        // TODO Auto-generated method stub
        super.getModelDescription(out, indent);
    }
}
