/*
 *    HoeffdingOptionTree.java
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

package tr.gov.ulakbim.jDenetX.classifiers;

import tr.gov.ulakbim.jDenetX.AbstractMOAObject;
import tr.gov.ulakbim.jDenetX.classifiers.attributes.*;
import tr.gov.ulakbim.jDenetX.classifiers.conditionals.InstanceConditionalTest;
import tr.gov.ulakbim.jDenetX.classifiers.splits.SplitCriterion;
import tr.gov.ulakbim.jDenetX.core.*;
import tr.gov.ulakbim.jDenetX.options.*;
import weka.core.Instance;
import weka.core.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class HoeffdingOptionTree extends AbstractClassifier  {

    private static final long serialVersionUID = 1L;
    public IntOption maxOptionPathsOption = new IntOption("maxOptionPaths",
            'o', "Maximum number of option paths per node.", 5, 1,
            Integer.MAX_VALUE);

    public IntOption maxByteSizeOption = new IntOption("maxByteSize", 'm',
            "Maximum memory consumed by the tree.", 33554432, 0,
            Integer.MAX_VALUE);

    public MultiChoiceOption numericEstimatorOption = new MultiChoiceOption(
            "numericEstimator", 'n', "Numeric estimator to use.", new String[]{
                    "GAUSS10", "GAUSS100", "GK10", "GK100", "GK1000", "VFML10",
                    "VFML100", "VFML1000", "BINTREE"}, new String[]{
                    "Gaussian approximation evaluating 10 splitpoints",
                    "Gaussian approximation evaluating 100 splitpoints",
                    "Greenwald-Khanna quantile summary with 10 tuples",
                    "Greenwald-Khanna quantile summary with 100 tuples",
                    "Greenwald-Khanna quantile summary with 1000 tuples",
                    "VFML method with 10 bins", "VFML method with 100 bins",
                    "VFML method with 1000 bins", "Exhaustive binary tree"}, 0);

    public IntOption memoryEstimatePeriodOption = new IntOption(
            "memoryEstimatePeriod", 'e',
            "How many instances between memory consumption checks.", 1000000,
            0, Integer.MAX_VALUE);

    public IntOption gracePeriodOption = new IntOption(
            "gracePeriod",
            'g',
            "The number of instances a leaf should observe between split attempts.",
            50, 0, Integer.MAX_VALUE);

    public ClassOption splitCriterionOption = new ClassOption("splitCriterion",
            's', "Split criterion to use.", SplitCriterion.class,
            "InfoGainSplitCriterion");

    public FloatOption splitConfidenceOption = new FloatOption(
            "splitConfidence",
            'c',
            "The allowable error in split decision, values closer to 0 will take longer to decide.",
            0.0000001, 0.0, 1.0);

    public FloatOption secondarySplitConfidenceOption = new FloatOption(
            "secondarySplitConfidence",
            'w',
            "The allowable error in secondary split decisions, values closer to 0 will take longer to decide.",
            0.1, 0.0, 1.0);

    public FloatOption tieThresholdOption = new FloatOption("tieThreshold",
            't', "Threshold below which a split will be forced to break ties.",
            0.05, 0.0, 1.0);

    public FlagOption binarySplitsOption = new FlagOption("binarySplits", 'b',
            "Only allow binary splits.");

    public FlagOption removePoorAttsOption = new FlagOption("removePoorAtts",
            'r', "Disable poor attributes.");

    public FlagOption noPrePruneOption = new FlagOption("noPrePrune", 'p',
            "Disable pre-pruning.");

    public FileOption dumpFileOption = new FileOption("dumpFile", 'd',
            "File to append option table to.", null, "csv", true);

    public IntOption memoryStrategyOption = new IntOption("memStrategy", 'z',
            "Memory strategy to use.", 2);

    /**
     * The node that is found as a result of tree traversal(or filtering).
     * It has a parent as a splitnode.
     * Track the
     */
    public static class FoundNode {

        public Node node = null;
        public SplitNode parent = null;
        public int parentBranch = -1; // set to -999 for option leaves

        public FoundNode(Node node, SplitNode parent, int parentBranch) {
            this.node = node;
            this.parent = parent;
            this.parentBranch = parentBranch;
        }
    }

    /**
     * The basic static node class.
     * This is more than a node, it is barely a subtree.
     */
    public static class Node extends AbstractMOAObject {

        private static final long serialVersionUID = 1L;

        /**
         * Investigates the class distribution and uses this distribution while calculating the split point and entropy.
         */
        protected DoubleVector observedClassDistribution;

        public Node(double[] classObservations) {
            this.observedClassDistribution = new DoubleVector(classObservations);
        }

        public int calcByteSize() {
            return (int) (SizeOf.sizeOf(this) + SizeOf
                    .sizeOf(this.observedClassDistribution));
        }

        public int calcByteSizeIncludingSubtree() {
            return calcByteSize();
        }

        public boolean isLeaf() {
            return true;
        }

        /**
         *Traverse the tree until you find the node with the specified properties
         * @param inst Instance
         * @param parent Splitnode
         * @param parentBranch ParentBranch number
         * @param updateSplitterCounts
         * @return
         */
        public FoundNode[] filterInstanceToLeaves(Instance inst,
                                                  SplitNode parent, int parentBranch, boolean updateSplitterCounts) {
            List<FoundNode> nodes = new LinkedList<FoundNode>();
            filterInstanceToLeaves(inst, parent, parentBranch, nodes,
                    updateSplitterCounts);
            return nodes.toArray(new FoundNode[nodes.size()]);
        }

        /**
         * Add the Found Nodes to the local storage.
         * @param inst
         * @param splitparent
         * @param parentBranch
         * @param foundNodes
         * @param updateSplitterCounts
         */
        public void filterInstanceToLeaves(Instance inst,
                                           SplitNode splitparent, int parentBranch,
                                           List<FoundNode> foundNodes, boolean updateSplitterCounts) {
            foundNodes.add(new FoundNode(this, splitparent, parentBranch));
        }

        /**
         * @return Observed class dist array.
         */
        public double[] getObservedClassDistribution() {
            return this.observedClassDistribution.getArrayCopy();
        }

        /**
         * Get the classification result done by the current node
         * @param inst Instance
         * @return dist distribution of the probabilities.
         */
        public double[] getClassVotes(Instance inst) {
            if (inst.numClasses() != (this.observedClassDistribution.maxIndex() + 1)) {
                this.observedClassDistribution.setArrayLength(inst.numClasses());
            }
            double[] dist = this.observedClassDistribution.getArrayCopy();
            double distSum = Utils.sum(dist);
            if (distSum > 0.0) {
                Utils.normalize(dist, distSum);
            }
            return dist;
        }

        /**
         * @return true if the number of non-zero classes is less than 2
         */
        public boolean observedClassDistributionIsPure() {
            return this.observedClassDistribution.numNonZeroEntries() < 2;
        }

        public int subtreeDepth() {
            return 0;
        }

        public double calculatePromise() {
            double totalSeen = this.observedClassDistribution.sumOfValues();
            return totalSeen > 0.0 ? (totalSeen - this.observedClassDistribution
                    .getValue(this.observedClassDistribution.maxIndex()))
                    : 0.0;
        }

        public void describeSubtree(HoeffdingOptionTree ht, StringBuilder out,
                                    int indent) {
            StringUtils.appendIndented(out, indent, "Leaf ");
            out.append(ht.getClassNameString());
            out.append(" = ");
            out.append(ht.getClassLabelString(this.observedClassDistribution
                    .maxIndex()));
            out.append(" weights: ");
            this.observedClassDistribution.getSingleLineDescription(out,
                    ht.treeRoot.observedClassDistribution.numValues());
            StringUtils.appendNewline(out);
        }

        public void getDescription(StringBuilder sb, int indent) {
            describeSubtree(null, sb, indent);
        }
    }

    /**
     * @brief The class for split nodes of trees.
     * Used for splitting attributes and traversing the tree.
     */
    public static class SplitNode extends Node {

        private static final long serialVersionUID = 1L;

        /**
         * The conditional split test for an attribute. An attribute can be nominal
         * or numerical. But its split can be multiway or binary. If the attribute is
         * nominal. If attribute is numerical, gaussian NIP will split into binary splits.
         */
        protected InstanceConditionalTest splitTest;

        /**
         * The parent node of the current splitnode
         */
        protected SplitNode parent;

        /**
         *
         */
        protected Node nextOption;

        protected int optionCount; // set to -999 for optional splits

        /**
         * Children nodes.
         */
        protected AutoExpandVector<Node> children = new AutoExpandVector<Node>();

        @Override
        public int calcByteSize() {
            return super.calcByteSize()
                    + (int) (SizeOf.sizeOf(this.children) + SizeOf
                    .sizeOf(this.splitTest));
        }

        @Override
        public int calcByteSizeIncludingSubtree() {
            int byteSize = calcByteSize();
            for (Node child : this.children) {
                if (child != null) {
                    byteSize += child.calcByteSizeIncludingSubtree();
                }
            }
            if (this.nextOption != null) {
                byteSize += this.nextOption.calcByteSizeIncludingSubtree();
            }
            return byteSize;
        }

        public SplitNode(InstanceConditionalTest splitTest,
                         double[] classObservations) {
            super(classObservations);
            this.splitTest = splitTest;
        }

        public int numChildren() {
            return this.children.size();
        }

        public void setChild(int index, Node child) {
            if ((this.splitTest.maxBranches() >= 0)
                    && (index >= this.splitTest.maxBranches())) {
                throw new IndexOutOfBoundsException();
            }
            this.children.set(index, child);
        }

        public Node getChild(int index) {
            return this.children.get(index);
        }

        public int instanceChildIndex(Instance inst) {
            return this.splitTest.branchForInstance(inst);
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        /**
         * Traverse the tree until you find the wanted properties.
         * @param inst
         * @param myparent
         * @param parentBranch
         * @param foundNodes
         * @param updateSplitterCounts
         */
        @Override
        public void filterInstanceToLeaves(Instance inst, SplitNode myparent,
                                           int parentBranch, List<FoundNode> foundNodes,
                                           boolean updateSplitterCounts) {
            if (updateSplitterCounts) {
                this.observedClassDistribution.addToValue((int) inst
                        .classValue(), inst.weight());
            }
            int childIndex = instanceChildIndex(inst);
            if (childIndex >= 0) {
                Node child = getChild(childIndex);
                if (child != null) {
                    child.filterInstanceToLeaves(inst, this, childIndex,
                            foundNodes, updateSplitterCounts);
                } else {
                    foundNodes.add(new FoundNode(null, this, childIndex));
                }
            }
            if (this.nextOption != null) {
                this.nextOption.filterInstanceToLeaves(inst, this, -999,
                        foundNodes, updateSplitterCounts);
            }
        }

        @Override
        public void describeSubtree(HoeffdingOptionTree ht, StringBuilder out,
                                    int indent) {
            for (int branch = 0; branch < numChildren(); branch++) {
                Node child = getChild(branch);
                if (child != null) {
                    StringUtils.appendIndented(out, indent, "if ");
                    out.append(this.splitTest.describeConditionForBranch(branch,
                            ht.getModelContext()));
                    out.append(": ");
                    out.append("** option count = " + this.optionCount);
                    StringUtils.appendNewline(out);
                    child.describeSubtree(ht, out, indent + 2);
                }
            }
        }

        @Override
        public int subtreeDepth() {
            int maxChildDepth = 0;
            for (Node child : this.children) {
                if (child != null) {
                    int depth = child.subtreeDepth();
                    if (depth > maxChildDepth) {
                        maxChildDepth = depth;
                    }
                }
            }
            return maxChildDepth + 1;
        }

        /**
         * Compute the merit of existing split using a splitting criterion, e.g.: Information Gain.
         * @param splitCriterion
         * @param preDist
         * @return
         */
        public double computeMeritOfExistingSplit(
                SplitCriterion splitCriterion, double[] preDist) {
            double[][] postDists = new double[this.children.size()][];
            /*
                Post dists are the distribution of the children nodes.
             */
            for (int i = 0; i < this.children.size(); i++) {
                postDists[i] = this.children.get(i)
                        .getObservedClassDistribution();
            }
            return splitCriterion.getMeritOfSplit(preDist, postDists);
        }

        /**
         * This function updates the number of options from the specified split node.
         * @param source
         * @param hot
         */
        public void updateOptionCount(SplitNode source, HoeffdingOptionTree hot) {
            if (this.optionCount == -999) {
                this.parent.updateOptionCount(source, hot);
            } else {
                int maxChildCount = -999;
                SplitNode curr = this;
                while (curr != null) {
                    for (Node child : curr.children) {
                        if (child instanceof SplitNode) {
                            SplitNode splitChild = (SplitNode) child;
                            if (splitChild.optionCount > maxChildCount) {
                                maxChildCount = splitChild.optionCount;
                            }
                        }
                    }
                    if ((curr.nextOption != null)
                            && (curr.nextOption instanceof SplitNode)) {
                        curr = (SplitNode) curr.nextOption;
                    } else {
                        curr = null;
                    }
                }
                if (maxChildCount > this.optionCount) {
                    // currently only works
                    // one
                    // way - adding, not
                    // removing
                    int delta = maxChildCount - this.optionCount;
                    this.optionCount = maxChildCount;
                    if (this.optionCount >= hot.maxOptionPathsOption.getValue()) {
                        killOptionLeaf(hot);
                    }
                    curr = this;
                    while (curr != null) {
                        for (Node child : curr.children) {
                            if (child instanceof SplitNode) {
                                SplitNode splitChild = (SplitNode) child;
                                if (splitChild != source) {
                                    splitChild.updateOptionCountBelow(delta,
                                            hot);
                                }
                            }
                        }
                        if ((curr.nextOption != null)
                                && (curr.nextOption instanceof SplitNode)) {
                            curr = (SplitNode) curr.nextOption;
                        } else {
                            curr = null;
                        }
                    }
                    if (this.parent != null) {
                        this.parent.updateOptionCount(this, hot);
                    }
                }
            }
        }

        /**
         *
         * @param delta
         * @param hot
         */
        public void updateOptionCountBelow(int delta, HoeffdingOptionTree hot) {
            if (this.optionCount != -999) {
                this.optionCount += delta;
                if (this.optionCount >= hot.maxOptionPathsOption.getValue()) {
                    killOptionLeaf(hot);
                }
            }
            for (Node child : this.children) {
                if (child instanceof SplitNode) {
                    SplitNode splitChild = (SplitNode) child;
                    splitChild.updateOptionCountBelow(delta, hot);
                }
            }
            if (this.nextOption instanceof SplitNode) {
                ((SplitNode) this.nextOption)
                        .updateOptionCountBelow(delta, hot);
            }
        }

        /**
         *
         * @param hot
         */
        private void killOptionLeaf(HoeffdingOptionTree hot) {
            if (this.nextOption instanceof SplitNode) {
                ((SplitNode) this.nextOption).killOptionLeaf(hot);
            } else if (this.nextOption instanceof ActiveLearningNode) {
                this.nextOption = null;
                hot.activeLeafNodeCount--;
            } else if (this.nextOption instanceof InactiveLearningNode) {
                this.nextOption = null;
                hot.inactiveLeafNodeCount--;
            }
        }

        public int getHeadOptionCount() {
            SplitNode sn = this;
            while (sn.optionCount == -999) {
                sn = sn.parent;
            }
            return sn.optionCount;
        }
    }

    /**@brief Learning Node abstract class.
     * This is the learning node abstract class. InActiveLearningNode and ActiveLearningNode classes extends that.
     */
    public static abstract class LearningNode extends Node {

        /**
         * The constructor of learning node only taking the initial class observations.
         * @param initialClassObservations
         */
        public LearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        /**
         * abstract learning function.
         * This is empty and will be filled with in the children classes.
         * @param inst
         * @param ht
         */
        public abstract void learnFromInstance(Instance inst,
                                               HoeffdingOptionTree ht);

    }

    /**
     * @brief Learning Node abstract class.
     */
    public static class InactiveLearningNode extends LearningNode {

        /**
         * Constructor.
         * @param initialClassObservations
         */
        public InactiveLearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
        }

        /**
         * Learns from the instance function. It only addes the class of instance to the observed
         * class distribution.
         * @param inst
         * @param ht
         */
        @Override
        public void learnFromInstance(Instance inst, HoeffdingOptionTree ht) {
            this.observedClassDistribution.addToValue((int) inst.classValue(),
                    inst.weight());
        }
    }

    public static class ActiveLearningNode extends LearningNode {

        private static final long serialVersionUID = 1L;
        protected double weightSeenAtLastSplitEvaluation;

        //To observe the the attributes and split numerical attributes for obtaining a nominal attribute
        protected AutoExpandVector<AttributeClassObserver> attributeObservers = new AutoExpandVector<AttributeClassObserver>();

        /**
         * Constructor. Initializes the weights.
         * @param initialClassObservations
         */
        public ActiveLearningNode(double[] initialClassObservations) {
            super(initialClassObservations);
            this.weightSeenAtLastSplitEvaluation = getWeightSeen();
        }

        @Override
        public int calcByteSize() {
            return super.calcByteSize()
                    + (int) (SizeOf.sizeOf(this.attributeObservers));
        }

        /**
         * Learning function. Observes the statistics of attributes of an instance.
         * @param inst
         * @param ht
         */
        @Override
        public void learnFromInstance(Instance inst, HoeffdingOptionTree ht) {
            this.observedClassDistribution.addToValue((int) inst.classValue(),
                    inst.weight());
            for (int i = 0; i < inst.numAttributes() - 1; i++) {
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

        public double getWeightSeen() {
            return this.observedClassDistribution.sumOfValues();
        }

        public double getWeightSeenAtLastSplitEvaluation() {
            return this.weightSeenAtLastSplitEvaluation;
        }

        public void setWeightSeenAtLastSplitEvaluation(double weight) {
            this.weightSeenAtLastSplitEvaluation = weight;
        }

        /**
         * Get the best split suggestions. Using the split criterion on the HT.
         * @param criterion
         * @param ht
         * @return
         */
        public AttributeSplitSuggestion[] getBestSplitSuggestions(
                SplitCriterion criterion, HoeffdingOptionTree ht) {
            List<AttributeSplitSuggestion> bestSuggestions = new LinkedList<AttributeSplitSuggestion>();
            double[] preSplitDist = this.observedClassDistribution
                    .getArrayCopy();
            if (!ht.noPrePruneOption.isSet()) {
                // add null split as an option
                bestSuggestions
                        .add(new AttributeSplitSuggestion(null,
                                new double[0][], criterion.getMeritOfSplit(
                                preSplitDist,
                                new double[][]{preSplitDist})));
            }
            for (int i = 0; i < this.attributeObservers.size(); i++) {
                AttributeClassObserver obs = this.attributeObservers.get(i);
                if (obs != null) {
                    AttributeSplitSuggestion bestSuggestion = obs
                            .getBestEvaluatedSplitSuggestion(criterion,
                                    preSplitDist, i, ht.binarySplitsOption
                                    .isSet());
                    if (bestSuggestion != null) {
                        bestSuggestions.add(bestSuggestion);
                    }
                }
            }
            return bestSuggestions
                    .toArray(new AttributeSplitSuggestion[bestSuggestions
                            .size()]);
        }

        /**
         * disable the functioning of the attribute.
         * @param attIndex
         */
        public void disableAttribute(int attIndex) {
            this.attributeObservers.set(attIndex,
                    new NullAttributeClassObserver());
        }
    }

    protected Node treeRoot;
    protected int decisionNodeCount;
    protected int activeLeafNodeCount;
    protected int inactiveLeafNodeCount;
    protected double inactiveLeafByteSizeEstimate;
    protected double activeLeafByteSizeEstimate;
    protected double byteSizeEstimateOverheadFraction;
    protected int maxPredictionPaths;
    protected boolean growthAllowed;

    @Override
    public void resetLearningImpl() {
        this.treeRoot = null;
        this.decisionNodeCount = 0;
        this.activeLeafNodeCount = 0;
        this.inactiveLeafNodeCount = 0;
        this.inactiveLeafByteSizeEstimate = 0.0;
        this.activeLeafByteSizeEstimate = 0.0;
        this.byteSizeEstimateOverheadFraction = 1.0;
        this.maxPredictionPaths = 0;
        this.growthAllowed = true;
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        if (this.treeRoot == null) {
            this.treeRoot = newLearningNode();
            this.activeLeafNodeCount = 1;
        }
        FoundNode[] foundNodes = this.treeRoot.filterInstanceToLeaves(inst,
                null, -1, true);

        for (FoundNode foundNode : foundNodes) {
            // option leaves will have a parentBranch of -999
            // option splits will have an option count of -999
            //System.out.println( foundNode.parentBranch );
            Node leafNode = foundNode.node;
            if (leafNode == null) {
                leafNode = newLearningNode();
                foundNode.parent.setChild(foundNode.parentBranch, leafNode);
                this.activeLeafNodeCount++;
            }
            if (leafNode instanceof LearningNode) {
                LearningNode learningNode = (LearningNode) leafNode;
                learningNode.learnFromInstance(inst, this);
                if (learningNode instanceof ActiveLearningNode) {
                    ActiveLearningNode activeLearningNode = (ActiveLearningNode) learningNode;
                    double weightSeen = activeLearningNode.getWeightSeen();
                    if (weightSeen
                            - activeLearningNode
                            .getWeightSeenAtLastSplitEvaluation() >= this.gracePeriodOption
                            .getValue()) {
                        attemptToSplit(activeLearningNode, foundNode.parent,
                                foundNode.parentBranch);
                        activeLearningNode
                                .setWeightSeenAtLastSplitEvaluation(weightSeen);
                    }
                }
            }
        }
        if (this.trainingWeightSeenByModel
                % this.memoryEstimatePeriodOption.getValue() == 0) {
            estimateModelByteSizes();
        }
    }

    /**
     * Return the Votes for instance.
     * @param inst
     * @return
     */
    public double[] getVotesForInstance(Instance inst) {
        if (this.treeRoot != null) {
            FoundNode[] foundNodes = this.treeRoot.filterInstanceToLeaves(inst,
                    null, -1, false);
            DoubleVector result = new DoubleVector();
            int predictionPaths = 0;
            for (FoundNode foundNode : foundNodes) {
                if (foundNode.parentBranch != -999) {
                    Node leafNode = foundNode.node;
                    if (leafNode == null) {
                        leafNode = foundNode.parent;
                    }
                    double[] dist = leafNode.getClassVotes(inst);
                    result.addValues(dist);
                    predictionPaths++;
                }
            }
            if (predictionPaths > this.maxPredictionPaths) {
                this.maxPredictionPaths++;
            }
            result.normalize();
            return result.getArrayRef();
        }
        return new double[0];
    }

    /**
     *
     * @param out
     * @param indent
     */
    @Override
    public void getModelDescription(StringBuilder out, int indent) {
        this.treeRoot.describeSubtree(this, out, indent);
    }

    public boolean isRandomizable() {
        return false;
    }

    /**
     * Compute the Hoeffding bound, range and confidence are determined by the user input.
     * @param range
     * @param confidence
     * @param n
     * @return
     */
    public static double computeHoeffdingBound(double range, double confidence,
                                               double n) {
        return Math.sqrt(((range * range) * Math.log(1.0 / confidence))
                / (2.0 * n));
    }

    protected LearningNode newLearningNode() {
        return newLearningNode(new double[0]);
    }

    /**
     * Create a new learning node and return the distribution of the classes.
     * @param initialClassObservations
     * @return
     */
    protected LearningNode newLearningNode(double[] initialClassObservations) {
        return new ActiveLearningNode(initialClassObservations);
    }

    protected AttributeClassObserver newNominalClassObserver() {
        return new NominalAttributeClassObserver();
    }

    protected AttributeClassObserver newNumericClassObserver() {
        switch (this.numericEstimatorOption.getChosenIndex()) {
            case 0:
                return new GaussianNumericAttributeClassObserver(10);
            case 1:
                return new GaussianNumericAttributeClassObserver(100);
            case 2:
                return new GreenwaldKhannaNumericAttributeClassObserver(10);
            case 3:
                return new GreenwaldKhannaNumericAttributeClassObserver(100);
            case 4:
                return new GreenwaldKhannaNumericAttributeClassObserver(1000);
            case 5:
                return new VFMLNumericAttributeClassObserver(10);
            case 6:
                return new VFMLNumericAttributeClassObserver(100);
            case 7:
                return new VFMLNumericAttributeClassObserver(1000);
            case 8:
                return new BinaryTreeNumericAttributeClassObserver();
        }
        return new GaussianNumericAttributeClassObserver();
    }

    /**
     * One of the most important split function. spltis the numger of nodes.
     * @param node
     * @param parent
     * @param parentIndex
     */
    protected void attemptToSplit(ActiveLearningNode node, SplitNode parent,
                                  int parentIndex) {
        if (!node.observedClassDistributionIsPure()) {
            SplitCriterion splitCriterion = (SplitCriterion) getPreparedClassOption(this.splitCriterionOption);
            //SplitCriterion splitCriterion = new SplitCriterion();
            AttributeSplitSuggestion[] bestSplitSuggestions = node
                    .getBestSplitSuggestions(splitCriterion, this);
            Arrays.sort(bestSplitSuggestions);
            boolean shouldSplit = false;
            if (parentIndex != -999) {
                if (bestSplitSuggestions.length < 2) {
                    shouldSplit = bestSplitSuggestions.length > 0;
                } else {
                    double hoeffdingBound = computeHoeffdingBound(
                            splitCriterion.getRangeOfMerit(node
                                    .getObservedClassDistribution()),
                            this.splitConfidenceOption.getValue(), node
                            .getWeightSeen());
                    AttributeSplitSuggestion bestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 1];
                    AttributeSplitSuggestion secondBestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 2];
                    if ((bestSuggestion.merit - secondBestSuggestion.merit > hoeffdingBound)
                            || (hoeffdingBound < this.tieThresholdOption
                            .getValue())) {
                        shouldSplit = true;
                    }
                    if ((this.removePoorAttsOption != null)
                            && this.removePoorAttsOption.isSet()) {
                        Set<Integer> poorAtts = new HashSet<Integer>();
                        // scan 1 - add any poor to set
                        for (int i = 0; i < bestSplitSuggestions.length; i++) {
                            if (bestSplitSuggestions[i].splitTest != null) {
                                int[] splitAtts = bestSplitSuggestions[i].splitTest
                                        .getAttsTestDependsOn();
                                if (splitAtts.length == 1) {
                                    if (bestSuggestion.merit
                                            - bestSplitSuggestions[i].merit > hoeffdingBound) {
                                        poorAtts.add(Integer.valueOf(splitAtts[0]));
                                    }
                                }
                            }
                        }
                        // scan 2 - remove good ones from set
                        for (int i = 0; i < bestSplitSuggestions.length; i++) {
                            if (bestSplitSuggestions[i].splitTest != null) {
                                int[] splitAtts = bestSplitSuggestions[i].splitTest
                                        .getAttsTestDependsOn();
                                if (splitAtts.length == 1) {
                                    if (bestSuggestion.merit
                                            - bestSplitSuggestions[i].merit < hoeffdingBound) {
                                        poorAtts.remove(Integer.valueOf(
                                                splitAtts[0]));
                                    }
                                }
                            }
                        }
                        for (int poorAtt : poorAtts) {
                            node.disableAttribute(poorAtt);
                        }
                    }
                }
            } else if (bestSplitSuggestions.length > 0) {
                double hoeffdingBound = computeHoeffdingBound(splitCriterion
                        .getRangeOfMerit(node.getObservedClassDistribution()),
                        this.secondarySplitConfidenceOption.getValue(), node
                        .getWeightSeen());
                AttributeSplitSuggestion bestSuggestion = bestSplitSuggestions[bestSplitSuggestions.length - 1];

                // in option case, scan back through existing options to find best
                SplitNode current = parent;
                double bestPreviousMerit = Double.NEGATIVE_INFINITY;
                double[] preDist = node.getObservedClassDistribution();
                while (true) {
                    double merit = current.computeMeritOfExistingSplit(
                            splitCriterion, preDist);
                    if (merit > bestPreviousMerit) {
                        bestPreviousMerit = merit;
                    }
                    if (current.optionCount != -999) {
                        break;
                    }
                    current = current.parent;
                }

                if (bestSuggestion.merit - bestPreviousMerit > hoeffdingBound) {
                    shouldSplit = true;
                }
            }
            if (shouldSplit) {
                AttributeSplitSuggestion splitDecision = bestSplitSuggestions[bestSplitSuggestions.length - 1];
                if (splitDecision.splitTest == null) {
                    // preprune - null wins
                    if (parentIndex != -999) {
                        deactivateLearningNode(node, parent, parentIndex);
                    }
                } else {
                    SplitNode newSplit = new SplitNode(splitDecision.splitTest,
                            node.getObservedClassDistribution());
                    newSplit.parent = parent;
                    // add option procedure
                    SplitNode optionHead = parent;
                    if (parent != null) {
                        while (optionHead.optionCount == -999) {
                            optionHead = optionHead.parent;
                        }
                    }
                    if ((parentIndex == -999) && (parent != null)) {
                        // adding a new option
                        newSplit.optionCount = -999;
                        optionHead.updateOptionCountBelow(1, this);
                        if (optionHead.parent != null) {
                            optionHead.parent.updateOptionCount(optionHead,
                                    this);
                        }
                        addToOptionTable(splitDecision, optionHead.parent);
                    } else {
                        // adding a regular leaf
                        if (optionHead == null) {
                            newSplit.optionCount = 1;
                        } else {
                            newSplit.optionCount = optionHead.optionCount;
                        }
                    }
                    int numOptions = 1;
                    if (optionHead != null) {
                        numOptions = optionHead.optionCount;
                    }
                    if (numOptions < this.maxOptionPathsOption.getValue()) {
                        newSplit.nextOption = node;
                        // preserve leaf
                        // disable attribute just used
                        int[] splitAtts = splitDecision.splitTest
                                .getAttsTestDependsOn();
                        for (int i : splitAtts) {
                            node.disableAttribute(i);
                        }
                    } else {
                        this.activeLeafNodeCount--;
                    }
                    for (int i = 0; i < splitDecision.numSplits(); i++) {
                        Node newChild = newLearningNode(splitDecision
                                .resultingClassDistributionFromSplit(i));
                        newSplit.setChild(i, newChild);
                    }
                    this.decisionNodeCount++;
                    this.activeLeafNodeCount += splitDecision.numSplits();
                    if (parent == null) {
                        this.treeRoot = newSplit;
                    } else {
                        if (parentIndex != -999) {
                            parent.setChild(parentIndex, newSplit);
                        } else {
                            parent.nextOption = newSplit;
                        }
                    }
                }
                // manage memory
                enforceTrackerLimit();
            }
        }
    }

    /**
     *
     * @param bestSuggestion
     * @param parent
     */
    private void addToOptionTable(AttributeSplitSuggestion bestSuggestion,
                                  SplitNode parent) {
        File dumpFile = this.dumpFileOption.getFile();
        PrintStream immediateResultStream = null;
        if (dumpFile != null) {
            try {
                if (dumpFile.exists()) {
                    immediateResultStream = new PrintStream(
                            new FileOutputStream(dumpFile, true), true);
                } else {
                    immediateResultStream = new PrintStream(
                            new FileOutputStream(dumpFile), true);
                }
            } catch (Exception ex) {
                throw new RuntimeException("Unable to open dump file: "
                        + dumpFile, ex);
            }
            int splitAtt = bestSuggestion.splitTest.getAttsTestDependsOn()[0];
            double splitVal = -1.0;
            if (bestSuggestion.splitTest instanceof NumericAttributeBinaryTest) {
                NumericAttributeBinaryTest test = (NumericAttributeBinaryTest) bestSuggestion.splitTest;
                splitVal = test.getSplitValue();
            }
            int treeDepth = 0;
            while (parent != null) {
                parent = parent.parent;
                treeDepth++;
            }
            immediateResultStream.println(this.trainingWeightSeenByModel + ","
                    + treeDepth + "," + splitAtt + "," + splitVal);
            immediateResultStream.flush();
            immediateResultStream.close();
        }
    }

    /**
     * Manage memory
     */
    public void enforceTrackerLimit() {
        if ((this.inactiveLeafNodeCount > 0)
                || ((this.activeLeafNodeCount * this.activeLeafByteSizeEstimate + this.inactiveLeafNodeCount
                * this.inactiveLeafByteSizeEstimate)
                * this.byteSizeEstimateOverheadFraction > this.maxByteSizeOption
                .getValue())) {
            FoundNode[] learningNodes = findLearningNodes();
            Arrays.sort(learningNodes, new Comparator<FoundNode>() {
                public int compare(FoundNode fn1, FoundNode fn2) {
                    if (HoeffdingOptionTree.this.memoryStrategyOption
                            .getValue() == 0) {
                        // strategy 1 - every leaf treated equal
                        return Double.compare(fn1.node.calculatePromise(),
                                fn2.node.calculatePromise());
                    } else if (HoeffdingOptionTree.this.memoryStrategyOption
                            .getValue() == 1) {
                        // strategy 2 - internal leaves penalised
                        double p1 = fn1.node.calculatePromise();
                        if (fn1.parentBranch == -999) {
                            p1 /= fn1.parent.getHeadOptionCount();
                        }
                        double p2 = fn2.node.calculatePromise();
                        if (fn2.parentBranch == -999) {
                            p1 /= fn2.parent.getHeadOptionCount();
                        }
                        return Double.compare(p1, p2);
                    } else {
                        // strategy 3 - all true leaves beat internal leaves
                        if (fn1.parentBranch == -999) {
                            if (fn2.parentBranch == -999) {
                                return Double.compare(fn1.node
                                        .calculatePromise(), fn2.node
                                        .calculatePromise());
                            }
                            return -1; // fn1 < fn2
                        }
                        if (fn2.parentBranch == -999) {
                            return 1; // fn1 > fn2
                        }
                        return Double.compare(fn1.node.calculatePromise(),
                                fn2.node.calculatePromise());
                    }
                }
            });
            int maxActive = 0;
            while (maxActive < learningNodes.length) {
                maxActive++;
                if ((maxActive * this.activeLeafByteSizeEstimate + (learningNodes.length - maxActive)
                        * this.inactiveLeafByteSizeEstimate)
                        * this.byteSizeEstimateOverheadFraction > this.maxByteSizeOption
                        .getValue()) {
                    maxActive--;
                    break;
                }
            }
            int cutoff = learningNodes.length - maxActive;
            for (int i = 0; i < cutoff; i++) {
                if (learningNodes[i].node instanceof ActiveLearningNode) {
                    deactivateLearningNode(
                            (ActiveLearningNode) learningNodes[i].node,
                            learningNodes[i].parent,
                            learningNodes[i].parentBranch);
                }
            }
            for (int i = cutoff; i < learningNodes.length; i++) {
                if (learningNodes[i].node instanceof InactiveLearningNode) {
                    activateLearningNode(
                            (InactiveLearningNode) learningNodes[i].node,
                            learningNodes[i].parent,
                            learningNodes[i].parentBranch);
                }
            }
        }
    }

    public void estimateModelByteSizes() {
        FoundNode[] learningNodes = findLearningNodes();
        long totalActiveSize = 0;
        long totalInactiveSize = 0;
        for (FoundNode foundNode : learningNodes) {
            if (foundNode.node instanceof ActiveLearningNode) {
                totalActiveSize += SizeOf.sizeOf(foundNode.node);
            } else {
                totalInactiveSize += SizeOf.sizeOf(foundNode.node);
            }
        }
        if (totalActiveSize > 0) {
            this.activeLeafByteSizeEstimate = (double) totalActiveSize
                    / this.activeLeafNodeCount;
        }
        if (totalInactiveSize > 0) {
            this.inactiveLeafByteSizeEstimate = (double) totalInactiveSize
                    / this.inactiveLeafNodeCount;
        }
        int actualModelSize = this.measureByteSize();
        double estimatedModelSize = (this.activeLeafNodeCount
                * this.activeLeafByteSizeEstimate + this.inactiveLeafNodeCount
                * this.inactiveLeafByteSizeEstimate);
        this.byteSizeEstimateOverheadFraction = actualModelSize
                / estimatedModelSize;
        if (actualModelSize > this.maxByteSizeOption.getValue()) {
            enforceTrackerLimit();
        }
    }

    /**
     *  Delete node that satisfies the criterion.
     * @param toDeactivate
     * @param parent
     * @param parentBranch
     */
    protected void deactivateLearningNode(ActiveLearningNode toDeactivate,
                                          SplitNode parent, int parentBranch) {
        Node newLeaf = new InactiveLearningNode(toDeactivate
                .getObservedClassDistribution());
        if (parent == null) {
            this.treeRoot = newLeaf;
        } else {
            if (parentBranch != -999) {
                parent.setChild(parentBranch, newLeaf);
            } else {
                parent.nextOption = newLeaf;
            }
        }
        this.activeLeafNodeCount--;
        this.inactiveLeafNodeCount++;
    }

    /**
     *
     * @param toActivate
     * @param parent
     * @param parentBranch
     */
    protected void activateLearningNode(InactiveLearningNode toActivate,
                                        SplitNode parent, int parentBranch) {
        Node newLeaf = newLearningNode(toActivate
                .getObservedClassDistribution());
        if (parent == null) {
            this.treeRoot = newLeaf;
        } else {
            if (parentBranch != -999) {
                parent.setChild(parentBranch, newLeaf);
            } else {
                parent.nextOption = newLeaf;
            }
        }
        this.activeLeafNodeCount++;
        this.inactiveLeafNodeCount--;
    }

    protected FoundNode[] findLearningNodes() {
        List<FoundNode> foundList = new LinkedList<FoundNode>();
        findLearningNodes(this.treeRoot, null, -1, foundList);
        return foundList.toArray(new FoundNode[foundList.size()]);
    }

    /**
     *
     * @param node
     * @param parent
     * @param parentBranch
     * @param found
     */
    protected void findLearningNodes(Node node, SplitNode parent,
                                     int parentBranch, List<FoundNode> found) {
        if (node != null) {
            if (node instanceof LearningNode) {
                found.add(new FoundNode(node, parent, parentBranch));
            }
            if (node instanceof SplitNode) {
                SplitNode splitNode = (SplitNode) node;
                for (int i = 0; i < splitNode.numChildren(); i++) {
                    findLearningNodes(splitNode.getChild(i), splitNode, i,
                            found);
                }
                findLearningNodes(splitNode.nextOption, splitNode, -999, found);
            }
        }
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return new Measurement[]{
                new Measurement("tree size (nodes)", this.decisionNodeCount
                        + this.activeLeafNodeCount + this.inactiveLeafNodeCount),
                new Measurement("tree size (leaves)", this.activeLeafNodeCount
                        + this.inactiveLeafNodeCount),
                new Measurement("active learning leaves",
                        this.activeLeafNodeCount),
                new Measurement("tree depth", measureTreeDepth()),
                new Measurement("active leaf byte size estimate",
                        this.activeLeafByteSizeEstimate),
                new Measurement("inactiveleaf byte size estimate",
                        this.inactiveLeafByteSizeEstimate),
                new Measurement("byte sizeestimate overhead",
                        this.byteSizeEstimateOverheadFraction),
                new Measurement("maximum prediction paths used",
                        this.maxPredictionPaths)};
    }

    public int measureTreeDepth() {
        if (this.treeRoot != null) {
            return this.treeRoot.subtreeDepth();
        }
        return 0;
    }

    public int calcByteSize() {
        int size = (int) SizeOf.sizeOf(this);
        if (this.treeRoot != null) {
            size += this.treeRoot.calcByteSizeIncludingSubtree();
        }
        return size;
    }

    @Override
    public int measureByteSize() {
        return calcByteSize();
    }
}