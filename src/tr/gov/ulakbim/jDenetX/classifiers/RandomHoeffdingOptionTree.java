package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.classifiers.attributes.AttributeClassObserver;
import weka.core.Instance;

public class RandomHoeffdingOptionTree extends HoeffdingOptionTree {

    private static final long serialVersionUID = 1L;

    public static class RandomLearningNode extends ActiveLearningNode {

        private static final long serialVersionUID = 1L;

        protected double weightSeenAtLastSplitEvaluation;

        protected int[] listAttributes;

        protected int numAttributes;

        public RandomLearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        @Override
        public void learnFromInstance(Instance inst, HoeffdingOptionTree ht) {
            this.observedClassDistribution.addToValue((int) inst.classValue(),
                    inst.weight());
            if (this.listAttributes == null) {
                this.numAttributes = (int) Math.floor(Math.sqrt(inst.numAttributes()));
                this.listAttributes = new int[this.numAttributes];
                for (int j = 0; j < this.numAttributes; j++) {
                    boolean isUnique = false;
                    while (isUnique == false) {
                        this.listAttributes[j] = ht.classifierRandom.nextInt(inst.numAttributes() - 1);
                        isUnique = true;
                        for (int i = 0; i < j; i++) {
                            if (this.listAttributes[j] == this.listAttributes[i]) {
                                isUnique = false;
                                break;
                            }
                        }
                    }

                }
            }
            for (int j = 0; j < this.numAttributes - 1; j++) {
                int i = this.listAttributes[j];
                int instAttIndex = modelAttIndexToInstanceAttIndex(i, inst);
                AttributeClassObserver obs = this.attributeObservers.get(i);
                if (obs == null) {
                    obs = inst.attribute(instAttIndex).isNominal() ? ht
                            .newNominalClassObserver() : ht
                            .newNumericClassObserver();
                    this.attributeObservers.set(i, obs);
                }
                obs.observeAttributeClass(inst.value(instAttIndex), (int) inst
                        .classValue(), inst.weight());
            }
        }

    }

    public RandomHoeffdingOptionTree() {
        this.removePoorAttsOption = null;
    }

    @Override
    protected LearningNode newLearningNode(double[] initialClassObservations) {
        return new RandomLearningNode(initialClassObservations);
    }

    @Override
    public boolean isRandomizable() {
        return true;
    }


}