package tr.gov.ulakbim.jDenetX.classifiers.ensemble;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Oct 13, 2010
 * Time: 2:32:27 PM
 * To change this template use File | Settings | File Templates.
 */

public class EnsembleEntropy {
    public static double getEntropyForArray(double votes[]) {
        double entropy = 0.0;
        for (int i = 0; i < votes.length; i++) {
            votes[i] -= votes[i] * (Math.log(votes[i]) / Math.log(2));
            // By Default Java computes Math.log for base e, to compute base 2 we should divide by log(2)
        }
        return entropy;
    }
}
