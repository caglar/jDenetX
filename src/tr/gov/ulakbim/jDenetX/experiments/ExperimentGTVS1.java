package tr.gov.ulakbim.jDenetX.experiments;

import tr.gov.ulakbim.jDenetX.classifiers.*;
import tr.gov.ulakbim.jDenetX.classifiers.splits.InfoGainSplitCriterion;
import tr.gov.ulakbim.jDenetX.evaluation.BasicClassificationPerformanceEvaluator;
import tr.gov.ulakbim.jDenetX.evaluation.ClassificationPerformanceEvaluator;

import tr.gov.ulakbim.jDenetX.evaluation.LearningEvaluation;
import tr.gov.ulakbim.jDenetX.experiments.wrappers.EvalModel;
import tr.gov.ulakbim.jDenetX.options.ClassOption;
import tr.gov.ulakbim.jDenetX.streams.ArffFileStream;
import tr.gov.ulakbim.jDenetX.streams.InstanceStream;

public class ExperimentGTVS1 {
    private static final String trainFile = "/home/caglar/workspace/FASC/datasets/gtvs/Day1.TCP.arff";
    private static final String valFile = "/home/caglar/workspace/FASC/datasets/gtvs/Day2.TCP.arff";
    private static final String testFile = "/home/caglar/workspace/FASC/datasets/gtvs/SiteB.TCP.arff";
    private InstanceStream trainStream = null;
    private InstanceStream valStream = null;
    private InstanceStream testStream = null;
    ClassificationPerformanceEvaluator evaluator;

    public void reInit() {
        if (trainStream == null) {
            trainStream = new ArffFileStream(trainFile, -1);
        } else {
            trainStream.restart();
        }

        if (valStream == null) {
            valStream = new ArffFileStream(trainFile, -1);
        } else {
            valStream.restart();
        }

        if (testStream == null) {
            testStream = new ArffFileStream(trainFile, -1);
        } else {
            testStream.restart();
        }
        evaluator = new BasicClassificationPerformanceEvaluator();
    }

    public void testHoeffdingOptionTree() {
        HoeffdingOptionTree hot = new HoeffdingOptionTree();
        hot.binarySplitsOption.setValue(true);
        hot.splitCriterionOption.setValueViaCLIString("InfoGainSplitCriterion");
        hot.splitConfidenceOption.setValue(0);
        hot.binarySplitsOption.setValue(false);
        InstanceStream trainStream = new ArffFileStream(trainFile, -1);
        InstanceStream testStream = new ArffFileStream(testFile, -1);
        EvalModel evalModel = new EvalModel();
        LearningEvaluation lEval = evalModel.evalModel(trainStream, testStream, hot);
        System.out.println(lEval.getMeasurements()[0]);
        System.out.println(lEval.getMeasurements()[1]);
        System.out.println(lEval.getMeasurements()[2]);
        System.out.println(lEval.getMeasurements()[3]);
        System.out.println(lEval.getMeasurements()[4]);
    }

    public void testHoeffdingOptionTreeNB() {
        HoeffdingOptionTreeNB hot = new HoeffdingOptionTreeNB();
        ClassOption splitCriterionOption = new ClassOption("splitCriterion",
                's', "Split criterion to use.", InfoGainSplitCriterion.class, "InfoGainSplitCriterion");
        hot.splitCriterionOption.setValueViaCLIString("InfoGainSplitCriterion");
        hot.splitCriterionOption = splitCriterionOption;
        hot.binarySplitsOption.setValue(true);
        InstanceStream trainStream = new ArffFileStream(trainFile, -1);
        InstanceStream testStream = new ArffFileStream(testFile, -1);
        EvalModel evalModel = new EvalModel();
        LearningEvaluation lEval = evalModel.evalModel(trainStream, testStream, hot);
        System.out.println(lEval.getMeasurements()[0]);
        System.out.println(lEval.getMeasurements()[1]);
        System.out.println(lEval.getMeasurements()[2]);
        System.out.println(lEval.getMeasurements()[3]);
        System.out.println(lEval.getMeasurements()[4]);
    }

    public void testHoeffdingOptionTreeNBAda() {
        HoeffdingOptionTreeNBAdaptive hot = new HoeffdingOptionTreeNBAdaptive();
        ClassOption splitCriterionOption = new ClassOption("splitCriterion",
                's', "Split criterion to use.", InfoGainSplitCriterion.class, "InfoGainSplitCriterion");
        hot.splitCriterionOption.setValueViaCLIString("InfoGainSplitCriterion");
        hot.splitCriterionOption = splitCriterionOption;
        hot.binarySplitsOption.setValue(true);
        InstanceStream trainStream = new ArffFileStream(trainFile, -1);
        InstanceStream testStream = new ArffFileStream(testFile, -1);
        EvalModel evalModel = new EvalModel();
        LearningEvaluation lEval = evalModel.evalModel(trainStream, testStream, hot);
        System.out.println(lEval.getMeasurements()[0]);
        System.out.println(lEval.getMeasurements()[1]);
        System.out.println(lEval.getMeasurements()[2]);
        System.out.println(lEval.getMeasurements()[3]);
        System.out.println(lEval.getMeasurements()[4]);
    }

    public void testOzaBagASHT() {
        OzaBagASHT ozaBag= new OzaBagASHT();
        ClassOption splitCriterionOption = new ClassOption("splitCriterion",
                's', "Split criterion to use.", InfoGainSplitCriterion.class, "InfoGainSplitCriterion");
        ASHoeffdingOptionTree asHoeffOpt = new ASHoeffdingOptionTree();
        asHoeffOpt.splitCriterionOption.setValueViaCLIString("InfoGainSplitCriterion");
        asHoeffOpt.splitCriterionOption = splitCriterionOption;
        asHoeffOpt.binarySplitsOption.setValue(true);
        InstanceStream trainStream = new ArffFileStream(trainFile, -1);
        InstanceStream testStream = new ArffFileStream(testFile, -1);
        EvalModel evalModel = new EvalModel();
        LearningEvaluation lEval = evalModel.evalModel(trainStream, testStream, ozaBag);
        System.out.println(lEval.getMeasurements()[0]);
        System.out.println(lEval.getMeasurements()[1]);
        System.out.println(lEval.getMeasurements()[2]);
        System.out.println(lEval.getMeasurements()[3]);
        System.out.println(lEval.getMeasurements()[4]);
    }

    public void testOzaBoostASHT() {

    }

    public void testOzaBag() {

    }

    public void testRandomHoeffdingTree() {

    }

    public void testRandomHoeffdingTreeNB() {

    }

    public void testActiveClusterBaggingASHT() {

    }

    public void testOzaBagASHOT() {

    }

    public void testOzaBoostASHOT() {

    }

    public void testSelfOzaBoostID() {

    }

    public void testSelfOzaBoostASHOT() {

    }

    public void testRandomHoeffdingTreeNBAda() {

    }

    public static void main(String[] args) {
        ExperimentGTVS1 expGTVS1 = new ExperimentGTVS1();
        expGTVS1.reInit();
        System.out.println("Hoeffding Option Tree Test has been started");
        expGTVS1.testHoeffdingOptionTree();
        expGTVS1.reInit();
        System.out.println("Hoeffding Option NB Tree Test has been started");
        expGTVS1.testHoeffdingOptionTreeNB();
        expGTVS1.reInit();
        System.out.println("Hoeffding Option NB Tree Adaptive Test has been started");
        expGTVS1.testHoeffdingOptionTreeNBAda();
    }
}