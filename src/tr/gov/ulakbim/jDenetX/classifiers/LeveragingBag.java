package tr.gov.ulakbim.jDenetX.classifiers;

/*
 *    LeveragingBag.java
 *    Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet@cs.waikato.ac.nz)
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

import tr.gov.ulakbim.jDenetX.core.DoubleVector;
import tr.gov.ulakbim.jDenetX.core.Measurement;
import tr.gov.ulakbim.jDenetX.core.MiscUtils;
import tr.gov.ulakbim.jDenetX.core.SizeOf;
import tr.gov.ulakbim.jDenetX.options.ClassOption;
import tr.gov.ulakbim.jDenetX.options.FlagOption;
import tr.gov.ulakbim.jDenetX.options.FloatOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import weka.core.Instance;

// Leveraging Bagging and Leveraging Bagging MC ( -o option)

public class LeveragingBag extends AbstractClassifier {

    private static final long serialVersionUID = 1L;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "HoeffdingTree");

    public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's',
            "The number of models in the bag.", 10, 1, Integer.MAX_VALUE);

    public FloatOption weightShrinkOption = new FloatOption("weightShrink", 'w',
            "The number to use to compute the weight of new instances.", 6, 0.0, Float.MAX_VALUE);

    public FloatOption deltaAdwinOption = new FloatOption("deltaAdwin", 'a',
            "Delta of Adwin change detection", 0.002, 0.0, 1.0);

    // Leveraging Bagging MC: uses this option to use Output Codes
    public FlagOption outputCodesOption = new FlagOption("outputCodes", 'o',
            "Use Output Codes to use binary classifiers.");


    protected Classifier[] ensemble;
    protected ADWIN[] ADError;
    protected int numberOfChangesDetected;
    protected int[][] matrixCodes;
    protected boolean initMatrixCodes = false;

    @Override
    public int measureByteSize() {
        int size = (int) SizeOf.sizeOf(this);
        for (Classifier classifier : this.ensemble) {
            size += classifier.measureByteSize();
        }
        for (ADWIN adwin : this.ADError) {
            size += adwin.measureByteSize();
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
        this.ADError = new ADWIN[this.ensemble.length];
        for (int i = 0; i < this.ensemble.length; i++) {
            this.ADError[i] = new ADWIN((double) this.deltaAdwinOption.getValue());
        }
        this.numberOfChangesDetected = 0;
        if (this.outputCodesOption.isSet()) {
            this.initMatrixCodes = true;
        }
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        int numClasses = inst.numClasses();
        //Output Codes
        if (this.initMatrixCodes == true) {
            this.matrixCodes = new int[this.ensemble.length][inst.numClasses()];
            for (int i = 0; i < this.ensemble.length; i++) {
                int numberOnes;
                int numberZeros;

                do { // until we have the same number of zeros and ones
                    numberOnes = 0;
                    numberZeros = 0;
                    for (int j = 0; j < numClasses; j++) {
                        int result = 0;
                        if (j == 1 && numClasses == 2) {
                            result = 1 - this.matrixCodes[i][0];
                        } else {
                            result = (this.classifierRandom.nextBoolean() ? 1 : 0);
                        }
                        this.matrixCodes[i][j] = result;
                        if (result == 1)
                            numberOnes++;
                        else
                            numberZeros++;
                    }
                } while ((numberOnes - numberZeros) * (numberOnes - numberZeros) > (this.ensemble.length % 2));

            }
            this.initMatrixCodes = false;
        }


        boolean Change = false;
        double w = 1.0;
        double mt = 0.0;
        Instance weightedInst = (Instance) inst.copy();
        /*for (int i = 0; i < this.ensemble.length; i++) {
              if (this.outputCodesOption.isSet()) {
                  weightedInst.setClassValue((double) this.matrixCodes[i][(int) inst.classValue()] );
              }
              if(!this.ensemble[i].correctlyClassifies(weightedInst)) {
                  mt++;
              }
          }*/
        //update w
        w = this.weightShrinkOption.getValue(); //1.0 +mt/2.0;
        //Train ensemble of classifiers
        for (int i = 0; i < this.ensemble.length; i++) {
            int k = MiscUtils.poisson(w, this.classifierRandom);
            if (k > 0) {
                if (this.outputCodesOption.isSet()) {
                    weightedInst.setClassValue((double) this.matrixCodes[i][(int) inst.classValue()]);
                }
                weightedInst.setWeight(inst.weight() * k);
                this.ensemble[i].trainOnInstance(weightedInst);
            }
            boolean correctlyClassifies = this.ensemble[i].correctlyClassifies(weightedInst);
            double ErrEstim = this.ADError[i].getEstimation();
            if (this.ADError[i].setInput(correctlyClassifies ? 0 : 1))
                if (this.ADError[i].getEstimation() > ErrEstim) Change = true;
        }
        if (Change) {
            numberOfChangesDetected++;
            double max = 0.0;
            int imax = -1;
            for (int i = 0; i < this.ensemble.length; i++) {
                if (max < this.ADError[i].getEstimation()) {
                    max = this.ADError[i].getEstimation();
                    imax = i;
                }
            }
            if (imax != -1) {
                this.ensemble[imax].resetLearning();
                //this.ensemble[imax].trainOnInstance(inst);
                this.ADError[imax] = new ADWIN((double) this.deltaAdwinOption.getValue());
            }
        }
    }

    public double[] getVotesForInstance(Instance inst) {
        if (this.outputCodesOption.isSet()) {
            return getVotesForInstanceBinary(inst);
        }
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

    public double[] getVotesForInstanceBinary(Instance inst) {
        double combinedVote[] = new double[(int) inst.numClasses()];
        Instance weightedInst = (Instance) inst.copy();
        if (this.initMatrixCodes == false) {
            for (int i = 0; i < this.ensemble.length; i++) {
                //Replace class by OC
                weightedInst.setClassValue((double) this.matrixCodes[i][(int) inst.classValue()]);

                double vote[];
                vote = this.ensemble[i]
                        .getVotesForInstance(weightedInst);
                //Binary Case
                int voteClass = 0;
                if (vote.length == 2) {
                    voteClass = (vote[1] > vote[0] ? 1 : 0);
                }
                //Update votes
                for (int j = 0; j < inst.numClasses(); j++) {
                    if (this.matrixCodes[i][j] == voteClass) {
                        combinedVote[j] += 1;
                    }
                }
            }
        }
        return combinedVote;
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
                this.ensemble != null ? this.ensemble.length : 0),
                new Measurement("change detections", this.numberOfChangesDetected)
        };
    }

    @Override
    public Classifier[] getSubClassifiers() {
        return this.ensemble.clone();
    }

}