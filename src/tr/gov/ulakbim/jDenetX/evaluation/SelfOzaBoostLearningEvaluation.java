package tr.gov.ulakbim.jDenetX.evaluation;

import tr.gov.ulakbim.jDenetX.classifiers.Classifier;
import tr.gov.ulakbim.jDenetX.core.Measurement;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: 10/19/11
 * Time: 4:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelfOzaBoostLearningEvaluation {

    private static final long serialVersionUID = 1L;

    protected Measurement[] measurements;

    public SelfOzaBoostLearningEvaluation(Measurement[] testingMeasurements,
                                          Measurement[] selfTestingMeasurements,
                                          HashMap<String, Integer> ClassesCountMap
                                          ) {
        this.measurements = measurements.clone();
    }

    public SelfOzaBoostLearningEvaluation(Measurement[] evaluationMeasurements,
                              ClassificationPerformanceEvaluator cpe, Classifier model) {
        List<Measurement> measurementList = new LinkedList<Measurement>();
        Collections.addAll(measurementList, evaluationMeasurements);
        Collections.addAll(measurementList, cpe.getPerformanceMeasurements());
        Collections.addAll(measurementList, model.getModelMeasurements());
        this.measurements = measurementList
                .toArray(new Measurement[measurementList.size()]);
    }

    public Measurement[] getMeasurements() {
        return this.measurements.clone();
    }

    public void getDescription(StringBuilder sb, int indent) {
        Measurement.getMeasurementsDescription(this.measurements, sb, indent);
    }
}
