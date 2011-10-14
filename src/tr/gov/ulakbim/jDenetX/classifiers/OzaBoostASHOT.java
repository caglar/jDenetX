package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.core.DoubleVector;
import tr.gov.ulakbim.jDenetX.core.Measurement;
import tr.gov.ulakbim.jDenetX.core.MiscUtils;
import tr.gov.ulakbim.jDenetX.core.SizeOf;
import tr.gov.ulakbim.jDenetX.options.FlagOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import weka.core.Instance;
import weka.core.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 24, 2010
 * Time: 3:39:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class OzaBoostASHOT extends OzaBoost {
    private static final long serialVersionUID = 1L;

    public IntOption firstClassifierSizeOption = new IntOption("firstClassifierSize", 'f',
            "The size of first classifier in the bag.", 1, 1, Integer.MAX_VALUE);

    public FlagOption useWeightOption = new FlagOption("useWeight",
            'u', "Enable weight classifiers.");

    public FlagOption resetTreesOption = new FlagOption("resetTrees",
            'r', "Reset trees when size is higher than the max.");

    protected double[] error;

    protected double alpha = 0.01;

    protected double[] scms;
    protected double[] swms;


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
        this.error = new double[this.ensembleSizeOption.getValue()];
        Classifier baseLearner = (Classifier) getPreparedClassOption(this.baseLearnerOption);
        baseLearner.resetLearning();
        int pow = this.firstClassifierSizeOption.getValue(); //EXTENSION TO ASHT

        for (int i = 0; i < this.ensemble.length; i++) {
            this.ensemble[i] = baseLearner.copy();
            this.error[i] = 0.0;
            ((ASHoeffdingOptionTree) this.ensemble[i]).setMaxSize(pow); //EXTENSION TO ASHT
            if ((this.resetTreesOption != null)
                    && this.resetTreesOption.isSet()) {
                ((ASHoeffdingOptionTree) this.ensemble[i]).setResetTree();
            }
            pow *= 2; //EXTENSION TO ASHT
        }
        this.scms = new double[this.ensemble.length];
        this.swms = new double[this.ensemble.length];
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        double lambda_d = 1.0;
        int trueClass = (int) inst.classValue();
        for (int i = 0; i < this.ensemble.length; i++) {
            double k = this.pureBoostOption.isSet() ? lambda_d : MiscUtils
                    .poisson(lambda_d, this.classifierRandom);
            if (k > 0.0) {
                Instance weightedInst = (Instance) inst.copy();
                weightedInst.setWeight(inst.weight() * k);
                if (Utils.maxIndex(this.ensemble[i].getVotesForInstance(inst)) == trueClass) {
                    this.error[i] += alpha * (0.0 - this.error[i]); //EWMA
                } else {
                    this.error[i] += alpha * (1.0 - this.error[i]); //EWMA
                }
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

    public double[] getVotesForInstance(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        for (int i = 0; i < this.ensemble.length; i++) {
            double memberWeight = getEnsembleMemberWeight(i);
            if (memberWeight > 0.0) {
                DoubleVector vote = new DoubleVector(this.ensemble[i]
                        .getVotesForInstance(inst));
                if (vote.sumOfValues() > 0.0) {
                    vote.normalize();
                    if ((this.useWeightOption != null)
                            && this.useWeightOption.isSet()) {
                        vote.scaleValues(1.0 / (this.error[i] * this.error[i]));
                    }
                    vote.scaleValues(memberWeight);
                    combinedVote.addValues(vote);
                }
            } else {
                break;
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
