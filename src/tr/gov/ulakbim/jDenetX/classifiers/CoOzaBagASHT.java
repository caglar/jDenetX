/*
 *    OzaBagASHT.java
 *    Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *    @author Caglar
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.core.DoubleVector;
import tr.gov.ulakbim.jDenetX.core.MiscUtils;
import tr.gov.ulakbim.jDenetX.core.VotedInstancePool;
import tr.gov.ulakbim.jDenetX.options.FlagOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import weka.core.Instance;
import weka.core.Utils;

import java.util.ArrayList;

public class CoOzaBagASHT extends OzaBag {

    private static final long serialVersionUID = 1L;

    public IntOption firstClassifierSizeOption = new IntOption(
            "firstClassifierSize", 'f',
            "The size of first classifier in the bag.", 1, 1, Integer.MAX_VALUE);

    public FlagOption useWeightOption = new FlagOption("useWeight", 'u',
            "Enable weight classifiers.");

    public FlagOption resetTreesOption = new FlagOption("resetTrees", 'r',
            "Reset trees when size is higher than the max.");

    protected double[] error;

    protected ArrayList<Instance> centroids;

    protected double alpha = 0.01;

    private static VotedInstancePool instConfPool = new VotedInstancePool();

    public static int instConfCount = 0;

    private final static double confidenceThreshold = 9.7;

    @Override
    public void resetLearningImpl() {
        this.ensemble = new Classifier[this.ensembleSizeOption.getValue()];
        this.error = new double[this.ensembleSizeOption.getValue()];
        instConfPool = new VotedInstancePool();
        instConfCount = 0;
        Classifier baseLearner = (Classifier) getPreparedClassOption(this.baseLearnerOption);
        baseLearner.resetLearning();
        int pow = this.firstClassifierSizeOption.getValue(); // EXTENSION TO ASHT
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

    public double getEntropyForArray(double votes[]) {
        double entropy = 0.0;
        for (int i = 0; i < votes.length; i++) {
            votes[i] -= votes[i] * (Math.log(votes[i]) / Math.log(2));
            // By Default Java computes Math.log for base e, to compute base 2 we should divide by log(2)
        }
        return entropy;
    }

    public double getQBCEntropy(double vote, int success) {
        double entropy = 0.0;
        entropy -= (vote / success) * Utils.log2(vote / success);
        //(Math.log(vote) / Math.log(2));
        // Default Java log function computes
        // Math.log for base e, to compute base 2 we
        // should divide by log(2)
        return entropy;
    }

    /**
     * Query By Comittee algorithm
     * This measures the vote entropy.
     * xve = argmax-Sigma (V(yi)/C)*log(V(yi)/C)
     * xve is the vote entropy.
     * C is the comittee size
     * V(yi) is the number of the votes that a label recieves among the comittee members' votes.
     */
    public double queryByCommitee(double[] ensembleVotes, int noOfClasses, int success) {
        double entropyQBC = 0.0;
        double qbc = 0.0;
        if (noOfClasses != 0) {
            for (int j = 0; j < noOfClasses; j++) {
                if (ensembleVotes[j] != 0) {
                    qbc = (double) ensembleVotes[j] / ((double) ensemble.length);
                    //System.out.println("qbc is : " + qbc);
                    entropyQBC -= getQBCEntropy(qbc, success);
                }
            }
        }
        return entropyQBC;
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        int trueClass = (int) inst.classValue();
        for (int i = 0; i < this.ensemble.length; i++) {
            int k = MiscUtils.poisson(1.0, this.classifierRandom);
            if (k > 0) {
                Instance weightedInst = (Instance) inst.copy();
                weightedInst.setWeight(inst.weight() * k);
                if (Utils.maxIndex(this.ensemble[i].getVotesForInstance(inst)) == trueClass) { // Here we used the getVotesForInstanceFunction of HoeffdingTree
                    this.error[i] += alpha * (0.0 - this.error[i]); // EWMA
                } else {
                    this.error[i] += alpha * (1.0 - this.error[i]); // EWMA
                }
                this.ensemble[i].trainOnInstance(weightedInst);
            }
        }
    }

    /**
     * This is the main classification function that is used by the GUI
     */
    public double[] getVotesForInstance(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        DoubleVector confidenceVec = new DoubleVector();
        double[] ensembleVotes = new double[inst.numClasses()];
        double qbcEntropy = 0.0;
        int success = 0;
        int alpha1 = 1;
        int alpha2 = 1;
        for (int i = 0; i < this.ensemble.length; i++) {
            DoubleVector vote = new DoubleVector(this.ensemble[i]
                    .getVotesForInstance(inst));
            if (vote.sumOfValues() > 0.0) {
                vote.normalize();
                confidenceVec.addValues(vote);
                if ((this.useWeightOption != null)
                        && this.useWeightOption.isSet()) {
                    vote.scaleValues(1.0 / (this.error[i] * this.error[i]));
                    //System.out.println("Ensemble : " + i + " Error: " + this.error[i]);
                }
                combinedVote.addValues(vote);
            }
            //
            //Ignore the classifiers which have high error ratio
            //
            if (this.error[i] < 0.23) {
                //
                // this is the votes of the ensembles for the classes
                //
                success++;
                ensembleVotes[combinedVote.maxIndex()] += combinedVote.getValue(combinedVote.maxIndex());
            }
        }
        //For confidence measure add to the pool  and in order to fit the confidence value between 0 and 1 divide by success val
        //System.out.println("Confidence " + combinedVote.getValue(combinedVote.maxIndex()));
        if ((confidenceVec.getValue(combinedVote.maxIndex())) >= confidenceThreshold) {
            qbcEntropy = queryByCommitee(ensembleVotes, inst.numClasses(), success);
            double activeLearningRatio = (qbcEntropy) * (combinedVote.getValue(combinedVote.maxIndex()) / this.ensemble.length);
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
    public double[] getVotesForInstanceOrig(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        double[] ensembleVotes = new double[inst.numClasses()];
        double qbcEntropy = 0.0;
        int success = 0;

        for (int i = 0; i < this.ensemble.length; i++) {
            DoubleVector vote = new DoubleVector(this.ensemble[i]
                    .getVotesForInstance(inst));
            // This will call the HoeffdingTree's getVotesForInstance Function
            if (vote.sumOfValues() > 0.0) {
                vote.normalize();
                if ((this.useWeightOption != null)
                        && this.useWeightOption.isSet()) {
                    vote.scaleValues(1.0 / (this.error[i] * this.error[i]));
                    System.out.println("Ensemble : " + i + " Error: " + this.error[i]);
                }
                //
                //Ignore the ensembles which have high error ratio
                //
                if (this.error[i] < 0.3) {
                    combinedVote.addValues(vote);
                }
            }
            //
            // this is the votes of the ensembles for the classes
            //
            if (this.error[i] < 0.3) {
                success++;
                ensembleVotes[combinedVote.maxIndex()] += combinedVote.getValue(combinedVote.maxIndex());
            }
        }
        // For confidence measure add to the pool  and in order to fit the confidence value between 0 and 1 divide by success val

        if ((combinedVote.getValue(combinedVote.maxIndex()) / success) >= confidenceThreshold) {
            qbcEntropy = queryByCommitee(ensembleVotes, inst.numClasses(), 0);
            System.out.println("QBC Entropy: " + qbcEntropy);
            double activeLearningRatio = (qbcEntropy) + (combinedVote.getValue(combinedVote.maxIndex()) / this.ensemble.length);
            inst.setClassValue(combinedVote.maxIndex());
            instConfPool.addVotedInstance(inst, combinedVote
                    .getValue(combinedVote.maxIndex()), activeLearningRatio);
        }
        return combinedVote.getArrayRef();
    }

    public static VotedInstancePool getVotedInstancePool() {
        return instConfPool;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        // TODO Auto-generated method stub
        super.getModelDescription(out, indent);
    }
}