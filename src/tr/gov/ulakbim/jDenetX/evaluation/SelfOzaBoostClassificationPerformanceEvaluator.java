package tr.gov.ulakbim.jDenetX.evaluation;

import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.lang3.ArrayUtils;
import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import tr.gov.ulakbim.jDenetX.core.Measurement;
import weka.core.Utils;

import java.io.ObjectInputStream;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: 10/19/11
 * Time: 3:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class SelfOzaBoostClassificationPerformanceEvaluator extends AbstractMOAObject
        implements ClassificationPerformanceEvaluator {

    private static final long serialVersionUID = 1L;

    protected double weightObserved;

    protected double weightCorrect;

    protected double[] columnKappa;

    protected double[] rowKappa;

    protected int[] instanceClassesMap;

    protected HashMap<String, Integer> ClassesCountMap;

    protected int numClasses;

    private double SE = 0.0;

    private int NoOfProcessedInstances = 0;
    //PrintWriter out = null;

    public void reset() {
        reset(this.numClasses);
    }

    public void reset(int numClasses) {
        this.numClasses = numClasses;
        this.rowKappa = new double[numClasses];
        this.columnKappa = new double[numClasses];
        this.instanceClassesMap = new int[numClasses];
        this.ClassesCountMap = new HashMap<String, Integer>();
        for (int i = 0; i < this.numClasses; i++) {
            this.rowKappa[i] = 0;
            this.columnKappa[i] = 0;
            this.instanceClassesMap[i] = 0;
        }
        this.SE = 0.0;
        NoOfProcessedInstances = 0;
        this.weightObserved = 0.0;
        this.weightCorrect = 0.0;
    }

    public void addClassificationAttempt(int trueClass,
                                         double[] classVotes,
                                         double weight) {
        if (weight > 0.0) {
            NoOfProcessedInstances++;
            if (this.weightObserved == 0) {
                reset(classVotes.length > 1 ? classVotes.length : 2);
            }
            this.weightObserved += weight;
            int predictedClass = Utils.maxIndex(classVotes);
            if (predictedClass == trueClass) {
                this.weightCorrect += weight;
            }
            this.SE += Evaluation.getSqError(trueClass, classVotes, weight);
            this.rowKappa[predictedClass] += weight;
            this.columnKappa[trueClass] += weight;
            this.instanceClassesMap[trueClass]++;
        }
    }

    public void addClassificationAttempt (int trueClass,
                                         String className,
                                         double[] classVotes,
                                         double weight) {
        if (weight > 0.0) {
            NoOfProcessedInstances++;

            if (this.weightObserved == 0) {
                reset(classVotes.length > 1 ? classVotes.length : 2);
            }
            this.weightObserved += weight;
            int predictedClass = Utils.maxIndex(classVotes);
            if (predictedClass == trueClass) {
                this.weightCorrect += weight;
            }
            this.SE += Evaluation.getSqError(trueClass, classVotes, weight);
            this.rowKappa[predictedClass] += weight;
            this.columnKappa[trueClass] += weight;
            instanceClassesMap[trueClass]++;
            ClassesCountMap.put(className, (Integer)(instanceClassesMap[trueClass]));
        }
    }

    public String getClassesRatioMap(){
        String message = "";

        for (String key : ClassesCountMap.keySet()) {
            double ratio = ((double)ClassesCountMap.get(key) / (double) NoOfProcessedInstances) * 100;
            message += key + ": " + ratio + "% \n";
        }
        return message;
    }

    public Measurement[] getClassesRatioMeasurements(){
        Measurement []measurements = new Measurement[ClassesCountMap.size()];
        int i = 0;
        for (String key : ClassesCountMap.keySet()) {
            double ratio = ((double)ClassesCountMap.get(key) / (double) NoOfProcessedInstances) * 100;
            measurements[i] = new Measurement(key, ratio);
            i++;
        }
        return measurements;
    }

    public Measurement[] getPerformanceMeasurements() {
        Measurement basicMeasurements[] = new Measurement[]{
                new Measurement("classified instances",
                        getTotalWeightObserved()),
                new Measurement("classifications correct (percent)",
                        getFractionCorrectlyClassified() * 100.0),
                new Measurement("Kappa Statistic (percent)",
                        getKappaStatistic() * 100.0),
                new Measurement("Mean Square Error ",
                        getMSE()),
                new Measurement("Root Mean Square Error ",
                        getRMSE())
        };
        Measurement classRatios[] = getClassesRatioMeasurements();
        Measurement aggregatedMeasurements[] = (Measurement []) ArrayUtils.addAll(basicMeasurements, classRatios);
        return aggregatedMeasurements;
    }

    public double getTotalWeightObserved() {
        return this.weightObserved;
    }

    public double getMSE() {
        return (SE / (double) NoOfProcessedInstances);
    }

    public double getRMSE() {
        return Math.sqrt(SE / (double) NoOfProcessedInstances);
    }

    public double getFractionCorrectlyClassified() {
        return this.weightObserved > 0.0 ? this.weightCorrect
                / this.weightObserved : 0.0;
    }

    public int getNoOfProcessedInstances() {
        return NoOfProcessedInstances;
    }

    public HashMap<String, Integer> getClassesCountMap() {
        return ClassesCountMap;
    }

    public double getFractionIncorrectlyClassified() {
        return 1.0 - getFractionCorrectlyClassified();
    }

    public double getKappaStatistic() {
        if (this.weightObserved > 0.0) {
            double p0 = getFractionCorrectlyClassified();
            double pc = 0.0;
            for (int i = 0; i < this.numClasses; i++) {
                pc += (this.rowKappa[i] / this.weightObserved) *
                        (this.columnKappa[i] / this.weightObserved);

            }
            return (p0 - pc) / (1.0 - pc);
        } else {
            return 0;
        }
    }

    public void getDescription(StringBuilder sb, int indent) {
        Measurement.getMeasurementsDescription(getPerformanceMeasurements(),
                sb, indent);
    }
}
