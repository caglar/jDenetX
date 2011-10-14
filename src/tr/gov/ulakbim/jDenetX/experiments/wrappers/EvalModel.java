package tr.gov.ulakbim.jDenetX.experiments.wrappers;

import tr.gov.ulakbim.jDenetX.classifiers.AbstractClassifier;
import tr.gov.ulakbim.jDenetX.evaluation.BasicClassificationPerformanceEvaluator;
import tr.gov.ulakbim.jDenetX.evaluation.ClassificationPerformanceEvaluator;
import tr.gov.ulakbim.jDenetX.evaluation.LearningEvaluation;
import tr.gov.ulakbim.jDenetX.streams.InstanceStream;
import weka.core.Instance;

public class EvalModel {

    private int MaxInstances = 1000000;
    private static final int INSTANCES_BETWEEN_MONITOR_UPDATES = 10;

    public int getMaxInstances() {
        return MaxInstances;
    }

    public void setMaxInstances(int maxInstances) {
        MaxInstances = maxInstances;
    }

    protected void trainModel(InstanceStream trainStream, AbstractClassifier model) {
        ClassificationPerformanceEvaluator evaluator = new BasicClassificationPerformanceEvaluator();
        evaluator.reset();
        long instancesProcessed = 0;
        System.out.println("Started learning the model...");
        while (trainStream.hasMoreInstances()
                && ((MaxInstances < 0) || (instancesProcessed < MaxInstances))) {
            model.trainOnInstance(trainStream.nextInstance());
            instancesProcessed++;
            if (instancesProcessed % INSTANCES_BETWEEN_MONITOR_UPDATES == 0) {
                long estimatedRemainingInstances = trainStream
                        .estimatedRemainingInstances();
                if (MaxInstances > 0) {
                    long maxRemaining = MaxInstances - instancesProcessed;
                    if ((estimatedRemainingInstances < 0)
                            || (maxRemaining < estimatedRemainingInstances)) {
                        estimatedRemainingInstances = maxRemaining;
                    }
                }
            }
        }
    }

    public LearningEvaluation evalModel (InstanceStream trainStream, InstanceStream testStream, AbstractClassifier model) {
        ClassificationPerformanceEvaluator evaluator = new BasicClassificationPerformanceEvaluator();
        evaluator.reset();
        long instancesProcessed = 0;
        System.out.println("Evaluating model...");
        trainModel(trainStream, model);
        while (testStream.hasMoreInstances()
                && ((MaxInstances < 0) || (instancesProcessed < MaxInstances))) {
            Instance testInst = (Instance) testStream.nextInstance().copy();
            int trueClass = (int) testInst.classValue();
            testInst.setClassMissing();
            double[] prediction = model.getVotesForInstance(testInst);
            evaluator.addClassificationAttempt(trueClass, prediction, testInst
                    .weight());
            instancesProcessed++;
            if (instancesProcessed % INSTANCES_BETWEEN_MONITOR_UPDATES == 0) {
                long estimatedRemainingInstances = testStream
                        .estimatedRemainingInstances();
                if (MaxInstances > 0) {
                    long maxRemaining = MaxInstances - instancesProcessed;
                    if ((estimatedRemainingInstances < 0)
                            || (maxRemaining < estimatedRemainingInstances)) {
                        estimatedRemainingInstances = maxRemaining;
                    }
                }
            }
        }
        return new LearningEvaluation(evaluator.getPerformanceMeasurements());
    }
}
