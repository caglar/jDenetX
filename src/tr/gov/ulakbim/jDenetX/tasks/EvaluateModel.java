/*
 *    EvaluateModel.java
 *    Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 *    @author Richard Kirkby (rkirkby@cs.waikato.ac.nz)
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
package tr.gov.ulakbim.jDenetX.tasks;

import tr.gov.ulakbim.jDenetX.classifiers.Classifier;
import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
import tr.gov.ulakbim.jDenetX.evaluation.ClassificationPerformanceEvaluator;
import tr.gov.ulakbim.jDenetX.evaluation.LearningEvaluation;
import tr.gov.ulakbim.jDenetX.options.ClassOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import tr.gov.ulakbim.jDenetX.streams.InstanceStream;
import weka.core.Instance;

public class EvaluateModel extends MainTask {

    @Override
    public String getPurposeString() {
        return "Evaluates a static model on a stream.";
    }

    private static final long serialVersionUID = 1L;

    public ClassOption modelOption = new ClassOption("model", 'm',
            "Classifier to evaluate.", Classifier.class, "LearnModel");

    public ClassOption streamOption = new ClassOption("stream", 's',
            "Stream to evaluate on.", InstanceStream.class,
            "generators.RandomTreeGenerator");

    public ClassOption evaluatorOption = new ClassOption("evaluator", 'e',
            "Classification performance evaluation method.",
            ClassificationPerformanceEvaluator.class,
            "BasicClassificationPerformanceEvaluator");

    public IntOption maxInstancesOption = new IntOption("maxInstances", 'i',
            "Maximum number of instances to test.", 1000000, 0,
            Integer.MAX_VALUE);

    public EvaluateModel() {

    }

    public EvaluateModel(Classifier model, InstanceStream stream,
                         ClassificationPerformanceEvaluator evaluator, int maxInstances) {
        this.modelOption.setCurrentObject(model);
        this.streamOption.setCurrentObject(stream);
        this.evaluatorOption.setCurrentObject(evaluator);
        this.maxInstancesOption.setValue(maxInstances);
    }

    public Class<?> getTaskResultType() {
        return LearningEvaluation.class;
    }

    @Override
    public Object doMainTask(TaskMonitor monitor, ObjectRepository repository) {
        Classifier model = (Classifier) getPreparedClassOption(this.modelOption);
        InstanceStream stream = (InstanceStream) getPreparedClassOption(this.streamOption);
        ClassificationPerformanceEvaluator evaluator = (ClassificationPerformanceEvaluator) getPreparedClassOption(this.evaluatorOption);
        int maxInstances = this.maxInstancesOption.getValue();
        long instancesProcessed = 0;
        monitor.setCurrentActivity("Evaluating model...", -1.0);
        while (stream.hasMoreInstances()
                && ((maxInstances < 0) || (instancesProcessed < maxInstances))) {
            Instance testInst = (Instance) stream.nextInstance().copy();
            int trueClass = (int) testInst.classValue();
            testInst.setClassMissing();
            double[] prediction = model.getVotesForInstance(testInst);
            evaluator.addClassificationAttempt(trueClass, prediction, testInst
                    .weight());
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
                    monitor.setLatestResultPreview(new LearningEvaluation(
                            evaluator.getPerformanceMeasurements()));
                }
            }
        }
        return new LearningEvaluation(evaluator.getPerformanceMeasurements());
    }
}