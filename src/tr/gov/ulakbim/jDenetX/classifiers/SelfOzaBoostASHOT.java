package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.core.DoubleVector;
import tr.gov.ulakbim.jDenetX.core.Measurement;
import tr.gov.ulakbim.jDenetX.core.MiscUtils;
import tr.gov.ulakbim.jDenetX.core.SizeOf;
import tr.gov.ulakbim.jDenetX.options.*;
import weka.core.Instance;
import weka.core.Utils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Oct 12, 2010
 * Time: 2:23:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelfOzaBoostASHOT extends SelfOzaBoost {

    private static final long serialVersionUID = 1L;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
            "Classifier to train.", Classifier.class, "HoeffdingTree");

    public IntOption ensembleSizeOption = new IntOption("ensembleSize", 's',
            "The number of models to boost.", 10, 1, Integer.MAX_VALUE);

    public FlagOption pureBoostOption = new FlagOption("pureBoost", 'p',
            "Boost with weights only; no poisson.");

    protected Classifier[] ensemble;

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
        Classifier baseLearner = (Classifier) getPreparedClassOption(this.baseLearnerOption);
        baseLearner.resetLearning();
        for (int i = 0; i < this.ensemble.length; i++) {
            this.ensemble[i] = baseLearner.copy();
        }
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

    int test = 0;

    public double[] getVotesForInstance(Instance inst) {
        DoubleVector combinedVote = new DoubleVector();
        double[] ensMemberWeights = new double[this.ensemble.length];
        boolean[] ensMemberFlags = new boolean[this.ensemble.length];

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
                }
            } else {
                break;
            }
        }
        if (combinedVote.getValue(combinedVote.maxIndex()) > 5.0) {
            test++;
            System.out.println(combinedVote.getValue(combinedVote.maxIndex()));
            System.out.println(test);
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

    /**
     * Active learning setting for evolving data streams.
     *
     * <p>Active learning focuses on learning an accurate model with as few labels
     * as possible. Streaming data poses additional challenges for active learning,
     * since the data distribution may change over time (concept drift) and classifiers
     * need to adapt. Conventional active learning strategies concentrate on querying
     * the most uncertain instances, which are typically concentrated around the
     * decision boundary. If changes do not occur close to the boundary, they will
     * be missed and classifiers will fail to adapt. This class contains four active
     * learning strategies for streaming data that explicitly handle concept drift.
     * They are based on randomization, fixed uncertainty, dynamic allocation of
     * labeling efforts over time and randomization of the search space [ZBPH].
     * It also contains the Selective Sampling strategy, which is adapted from [CGZ]
     * it uses a variable labeling threshold.
     *
     * </p>
     *
     * <p>[ZBPH] Indre Zliobaite, Albert Bifet, Bernhard Pfahringer, Geoff Holmes:
     * Active Learning with Evolving Streaming Data. ECML/PKDD (3) 2011: 597-612</p>

     * <p>[CGZ] N. Cesa-Bianchi, C. Gentile, and L. Zaniboni. Worst-case analysis of selective
     * sampling for linear classification. J. Mach. Learn. Res. (7) 2006: 1205-1230</p>.
     *
     * <p>Parameters:</p> <ul>
     * <li>-l : Classiﬁer to train</li>
     * <li>-d : Strategy to use: Random, FixedUncertainty, VarUncertainty, RandVarUncertainty, SelSampling</li> </ul>
     * <li>-b : Budget to use</li>
     * <li>-u : Fixed threshold</li>
     * <li>-s : Floating budget step</li>
     * <li>-n : Number of instances at beginning without active learning</li>
     *
     * @author Indre Zliobaite (zliobaite at gmail dot com)
     * @author Albert Bifet (abifet at cs dot waikato dot ac dot nz)
     * @version $Revision: 7 $
     */
    public static class ActiveClassifier extends AbstractClassifier {

        private static final long serialVersionUID = 1L;

        public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'l',
                "Classifier to train.", Classifier.class, "SingleClassifierDrift");

        public MultiChoiceOption activeLearningStrategyOption = new MultiChoiceOption(
                "activeLearningStrategy", 'd', "Active Learning Strategy to use.", new String[]{
                "Random", "FixedUncertainty", "VarUncertainty", "RandVarUncertainty", "SelSampling"}, new String[]{
                "Random strategy",
                "Fixed uncertainty strategy",
                "Uncertainty strategy with variable threshold",
                "Uncertainty strategy with randomized variable threshold",
                "Selective Sampling"}, 0);

        public FloatOption budgetOption = new FloatOption("budget",
                'b', "Budget to use.",
                0.1, 0.0, 1.0);

        public FloatOption fixedThresholdOption = new FloatOption("fixedThreshold",
                'u', "Fixed threshold.",
                0.9, 0.00, 1.00);

        public FloatOption stepOption = new FloatOption("step",
                's', "Floating budget step.",
                0.01, 0.00, 1.00);

        public FloatOption numInstancesInitOption = new FloatOption("numInstancesInit",
                'n', "Number of instances at beginning without active learning.",
                0.0, 0.00, Integer.MAX_VALUE);

        public Classifier classifier;

        public int costLabeling;

        public int costLabelingRandom;

        public int iterationControl;

        public double newThreshold;

        public double maxPosterior;

        public double accuracyBaseLearner;

        private double outPosterior;

        private double getMaxPosterior(double[] incomingPrediction) {
            if (incomingPrediction.length > 1) {
                DoubleVector vote = new DoubleVector(incomingPrediction);
                if (vote.sumOfValues() > 0.0) {
                    vote.normalize();
                }
                incomingPrediction = vote.getArrayRef();
                outPosterior = (incomingPrediction[Utils.maxIndex(incomingPrediction)]);
            } else {
                outPosterior = 0;
            }
            return outPosterior;
        }

        private void labelRandom(Instance inst) {
            if (this.classifierRandom.nextDouble() < this.budgetOption.getValue()) {
                this.classifier.trainOnInstance(inst);
                this.costLabeling++;
                this.costLabelingRandom++;
            }

        }

        private void labelFixed(double incomingPosterior, Instance inst) {
            if (incomingPosterior < this.fixedThresholdOption.getValue()) {
                this.classifier.trainOnInstance(inst);
                this.costLabeling++;
            }
        }

        private void labelVar(double incomingPosterior, Instance inst) {
            if (incomingPosterior < this.newThreshold) {
                this.classifier.trainOnInstance(inst);
                this.costLabeling++;
                this.newThreshold *= (1 - this.stepOption.getValue());
            } else {
                this.newThreshold *= (1 + this.stepOption.getValue());
            }
        }

        private void labelSelSampling(double incomingPosterior, Instance inst) {
            double p = Math.abs(incomingPosterior - 1.0 / (inst.numClasses()));
            double budget = this.budgetOption.getValue() / (this.budgetOption.getValue() + p);
            if (this.classifierRandom.nextDouble() < budget) {
                this.classifier.trainOnInstance(inst);
                this.costLabeling++;
            }
        }

        @Override
        public void resetLearningImpl() {
            this.classifier = ((Classifier) getPreparedClassOption(this.baseLearnerOption)).copy();
            this.classifier.resetLearning();
            this.costLabeling = 0;
            this.costLabelingRandom = 0;
            this.iterationControl = 0;
            this.newThreshold = 1.0;
            this.accuracyBaseLearner = 0;
        }

        @Override
        public void trainOnInstanceImpl(Instance inst) {

            this.iterationControl++;

            double costNow;

            if (this.iterationControl <= this.numInstancesInitOption.getValue()) {
                costNow = 0;
            } else {
                costNow = (this.costLabeling - this.numInstancesInitOption.getValue()) / ((double) this.iterationControl - this.numInstancesInitOption.getValue());
            }


            if (costNow < this.budgetOption.getValue()) { //allow to label
                switch (this.activeLearningStrategyOption.getChosenIndex()) {
                    case 0: //Random
                        labelRandom(inst);
                        break;
                    case 1: //fixed
                        maxPosterior = getMaxPosterior(this.classifier.getVotesForInstance(inst));
                        labelFixed(maxPosterior, inst);
                        break;
                    case 2: //variable
                        maxPosterior = getMaxPosterior(this.classifier.getVotesForInstance(inst));
                        labelVar(maxPosterior, inst);
                        break;
                    case 3: //randomized
                        maxPosterior = getMaxPosterior(this.classifier.getVotesForInstance(inst));
                        maxPosterior = maxPosterior / (this.classifierRandom.nextGaussian() + 1.0);
                        labelVar(maxPosterior, inst);
                        break;
                    case 4: //selective-sampling
                        maxPosterior = getMaxPosterior(this.classifier.getVotesForInstance(inst));
                        labelSelSampling(maxPosterior, inst);
                        break;
                }
            }
        }

        @Override
        public double[] getVotesForInstance(Instance inst) {
            return this.classifier.getVotesForInstance(inst);
        }

        @Override
        public boolean isRandomizable() {
            return true;
        }

        @Override
        public void getModelDescription(StringBuilder out, int indent) {
            ((AbstractClassifier) this.classifier).getModelDescription(out, indent);
        }

        @Override
        protected Measurement[] getModelMeasurementsImpl() {
            List<Measurement> measurementList = new LinkedList<Measurement>();
            measurementList.add(new Measurement("labeling cost", this.costLabeling));
            measurementList.add(new Measurement("newThreshold", this.newThreshold));
            measurementList.add(new Measurement("maxPosterior", this.maxPosterior));
            measurementList.add(new Measurement("accuracyBaseLearner (percent)", 100 * this.accuracyBaseLearner / this.costLabeling));
            Measurement[] modelMeasurements = ((AbstractClassifier) this.classifier).getModelMeasurementsImpl();
            if (modelMeasurements != null) {
                for (Measurement measurement : modelMeasurements) {
                    measurementList.add(measurement);
                }
            }
            return measurementList.toArray(new Measurement[measurementList.size()]);
        }
    }
}