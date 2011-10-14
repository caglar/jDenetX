package tr.gov.ulakbim.jDenetX.tasks;

import tr.gov.ulakbim.jDenetX.classifiers.ActiveClusterBaggingASHT;
import tr.gov.ulakbim.jDenetX.classifiers.Classifier;
import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
import tr.gov.ulakbim.jDenetX.streams.InstanceStream;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Sep 7, 2010
 * Time: 4:30:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class LearnClusterBaggingModel extends LearnModel {

//    public ClassOption learnerOption = new ClassOption("learner", 'l',
//            "Classifier to train.", Classifier.class, "ActiveClusterBaggingASHT");

    public LearnClusterBaggingModel() {
    }

    public LearnClusterBaggingModel(Classifier learner, InstanceStream stream,
                                    int maxInstances, int numPasses) {
        super(learner, stream, maxInstances, numPasses);
    }

    @Override
    public Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {
        System.out.println(learnerOption.getName());
        Classifier learner = (Classifier) getPreparedClassOption(learnerOption);
        InstanceStream stream = (InstanceStream) getPreparedClassOption(this.streamOption);
        learner.setModelContext(stream.getHeader());
        int numPasses = this.numPassesOption.getValue();
        int maxInstances = this.maxInstancesOption.getValue();
        for (int pass = 0; pass < numPasses; pass++) {
            long instancesProcessed = 0;
            monitor.setCurrentActivity("Training learner"
                    + (numPasses > 1 ? (" (pass " + (pass + 1) + "/"
                    + numPasses + ")") : "") + "...", -1.0);
            if (pass > 0) {
                stream.restart();
            }
            while (stream.hasMoreInstances()
                    && ((maxInstances < 0) || (instancesProcessed < maxInstances))) {
                if (instancesProcessed == maxInstances - 1) {
                    ((ActiveClusterBaggingASHT) learner).setCheckSize(false);
                }
                // System.out.println(stream.nextInstance());
                learner.trainOnInstance(stream.nextInstance());
                instancesProcessed++;
                if (instancesProcessed % INSTANCES_BETWEEN_MONITOR_UPDATES == 0) {
                    if (monitor.taskShouldAbort()) {
                        return null;
                    }
                    long estimatedRemainingInstances = stream
                            .estimatedRemainingInstances();
                    if (maxInstances > 0) {
                        long maxRemaining = maxInstances - instancesProcessed;
                        if ((estimatedRemainingInstances < 0)
                                || (maxRemaining < estimatedRemainingInstances)) {
                            estimatedRemainingInstances = maxRemaining;
                        }
                    }
                    monitor
                            .setCurrentActivityFractionComplete(estimatedRemainingInstances < 0 ? -1.0
                                    : (double) instancesProcessed
                                    / (double) (instancesProcessed + estimatedRemainingInstances));
                    if (monitor.resultPreviewRequested()) {
                        monitor.setLatestResultPreview(learner.copy());
                    }
                }
            }
        }
        learner.setModelContext(stream.getHeader());
        return learner;
    }

}
