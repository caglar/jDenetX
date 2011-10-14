package tr.gov.ulakbim.jDenetX.classifiers.ensemble;

import tr.gov.ulakbim.jDenetX.core.DoubleVector;
import weka.core.Utils;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: Oct 13, 2010
 * Time: 1:49:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class QBC {

    public QBC() {
    }

    public static double getEntropyForArray(double votes[]) {
        double entropy = 0.0;
        for (int i = 0; i < votes.length; i++) {
            votes[i] -= votes[i] * (Math.log(votes[i]) / Math.log(2));
            // By Default Java computes Math.log for base e, to compute base 2 we should divide by log(2)
        }
        return entropy;
    }

    public static double getQBCEntropy(double vote, int success) {
        double entropy = 0.0;
        entropy += (vote / success) * Utils.log2(vote / success);
        //(Math.log(vote) / Math.log(2));
        // Default Java log function computes
        // Math.log for base e, to compute base 2 we
        // should divide by log(2)
        return entropy;
    }

    public static double getKullbackLeiblerDiv(double[][] commiteeVotes, DoubleVector comitteeVote, int noOfSuccesses) {
        double klDiv = 0.0;
        if (noOfSuccesses != 0) {
            for (double[] votes : commiteeVotes) {
                for (int i = 0; i < votes.length; i++) {
                    if (comitteeVote.getValue(i) != 0) {
                        klDiv += votes[i] * Math.log(votes[i] / (double) comitteeVote.getValue(i));
                    }
                }
            }
        }
        klDiv /= noOfSuccesses;
        return klDiv;
    }

    public static double getKullbackLeiblerDiv_old(double[] ensembleVotes, int noOfClasses, int ensembleLength) {
        double klDiv = 0.0;
        if (noOfClasses != 0) {
            double commiteeProb = 0.0;
            for (double ensVote : ensembleVotes) {
                commiteeProb += commiteeProb;
            }
            commiteeProb /= ensembleLength;
            for (int j = 0; j < noOfClasses; j++) {
                if (ensembleVotes[j] != 0) {
                    klDiv += (double) ensembleVotes[j] * Math.log(ensembleVotes[j] / commiteeProb);
                }
            }
        }
        return klDiv;
    }

    /**
     * Query By Comittee algorithm
     * This measures the vote entropy.
     * xve = argmax -Sigma (V(yi)/C)*log(V(yi)/C) or xve = argmin Sigma (V(yi)/C)*log(V(yi)/C)
     * xve is the vote entropy.
     * C is the comittee size
     * V(yi) is the number of the votes that a label recieves among the comittee members' votes.
     */
    public static double queryByCommitee(double[] ensembleVotes, int noOfClasses, int success, int ensembleLength) {
        double entropyQBC = 0.0;
        double qbc = 0.0;
        if (noOfClasses != 0) {
            for (int j = 0; j < noOfClasses; j++) {
                if (ensembleVotes[j] != 0) {
                    qbc = (double) ensembleVotes[j] / ((double) ensembleLength);
                    entropyQBC -= getQBCEntropy(qbc, success);
                }
            }
        }
        return entropyQBC;
    }

    /**
     * Query By Comittee algorithm
     * This measures the vote entropy.
     * xve = argmax -Sigma (V(yi)/C)*log(V(yi)/C) or xve = argmin Sigma (V(yi)/C)*log(V(yi)/C)
     * xve is the vote entropy.
     * C is the comittee size
     * V(yi) is the number of the votes that a label recieves among the comittee members' votes.
     */
    public static double queryActiveLearnByCommitee(double[] ensembleVotes, int noOfClasses, int success, int ensembleLength) {
        double entropyQBC = 0.0;
        double qbc = 0.0;
        if (noOfClasses != 0) {
            for (int j = 0; j < noOfClasses; j++) {
                if (ensembleVotes[j] != 0) {
                    qbc = (double) ensembleVotes[j] / ((double) ensembleLength);
                    entropyQBC += getQBCEntropy(qbc, success);
                }
            }
        }
        return entropyQBC;
    }
}
