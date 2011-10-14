package tr.gov.ulakbim.jDenetX.evaluation;

//import weka.core.Utils;

public class Evaluation {
    private static double[] makeDistribution (int predictionClass, int numClasses){
        double dist[] = new double[numClasses];
        dist[predictionClass] = 1.0;
        return dist;
    }

    public static double getSqError (int trueClass, double[] classVotes,
                                         double weight){
        double err = 0.0;
        int numClasses = classVotes.length;
        double diff = 0.0;
        double sqDiff = 0.0;
        if (weight > 0.0 && numClasses > trueClass) {
            double trueDist[] = makeDistribution(trueClass, numClasses);
           for (int i = 0; i < numClasses; i++) {
               if (!Double.isInfinite(classVotes[i]) && !Double.isNaN(classVotes[i])) {
                    diff = classVotes[i] - trueDist[i];
                    sqDiff += diff * diff;
               }
            }
            err = weight * sqDiff;
        }
        return err;
    }
}