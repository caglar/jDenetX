package tr.gov.ulakbim.jDenetX.classifiers;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: 12/14/10
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
/*
 *    RandomHoeffdingTree.java
 *    Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet (abifet@cs.waikato.ac.nz)
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

import tr.gov.ulakbim.jDenetX.classifiers.attributes.AttributeClassObserver;
import weka.core.Instance;

public class RandomHoeffdingTree extends HoeffdingTree {

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
        public void learnFromInstance(Instance inst, HoeffdingTree ht) {
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

    public RandomHoeffdingTree() {
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