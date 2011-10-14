package tr.gov.ulakbim.jDenetX.classifiers;

import weka.core.Instance;

/**
 * Created by IntelliJ IDEA.
 * User: caglar
 * Date: May 12, 2010
 * Time: 3:39:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ASHoeffdingTree extends HoeffdingTreeNBAdaptive {

    private static final long serialVersionUID = 1L;

    protected int maxSize = 10000; //EXTENSION TO ASHT

    protected boolean resetTree = false;

    @Override
    public void resetLearningImpl() {
        this.treeRoot = null;
        this.decisionNodeCount = 0;
        this.activeLeafNodeCount = 0;
        this.inactiveLeafNodeCount = 0;
        this.inactiveLeafByteSizeEstimate = 0.0;
        this.activeLeafByteSizeEstimate = 0.0;
        this.byteSizeEstimateOverheadFraction = 1.0;
        this.growthAllowed = true;
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        if (this.treeRoot == null) {
            this.treeRoot = newLearningNode();
            this.activeLeafNodeCount = 1;
        }
        FoundNode foundNode = this.treeRoot
                .filterInstanceToLeaf(inst, null, -1);
        Node leafNode = foundNode.node;
        if (leafNode == null) {
            leafNode = newLearningNode();
            foundNode.parent.setChild(foundNode.parentBranch, leafNode);
            this.activeLeafNodeCount++;
        }
        if (leafNode instanceof LearningNode) {
            LearningNode learningNode = (LearningNode) leafNode;
            learningNode.learnFromInstance(inst, this);
            if (this.growthAllowed
                    && (learningNode instanceof ActiveLearningNode)) {
                ActiveLearningNode activeLearningNode = (ActiveLearningNode) learningNode;
                double weightSeen = activeLearningNode.getWeightSeen();
                if (weightSeen
                        - activeLearningNode
                        .getWeightSeenAtLastSplitEvaluation() >= this.gracePeriodOption
                        .getValue()) {
                    attemptToSplit(activeLearningNode, foundNode.parent,
                            foundNode.parentBranch);
                    //EXTENSION TO ASHT
                    // if size too big, resize tree ONLY Split Nodes
                    while (this.decisionNodeCount >= this.maxSize && this.treeRoot instanceof SplitNode) {
                        if (this.resetTree == false) {
                            resizeTree(this.treeRoot, ((SplitNode) this.treeRoot).instanceChildIndex(inst));
                            this.treeRoot = ((SplitNode) this.treeRoot).getChild(((SplitNode) this.treeRoot).instanceChildIndex(inst));
                        } else {
                            resetLearningImpl();
                        }
                    }
                    activeLearningNode
                            .setWeightSeenAtLastSplitEvaluation(weightSeen);
                }
            }
        }
        if (this.trainingWeightSeenByModel
                % this.memoryEstimatePeriodOption.getValue() == 0) {
            estimateModelByteSizes();
        }
    }

    //EXTENSION TO ASHT

    public void setMaxSize(int mSize) {
        this.maxSize = mSize;
    }

    public void setResetTree() {
        this.resetTree = true;
    }

    public void deleteNode(Node node, int childIndex) {
        Node child = ((SplitNode) node).getChild(childIndex);
        //if (child != null) {
        //}
        if (child instanceof SplitNode) {
            for (int branch = 0; branch < ((SplitNode) child).numChildren(); branch++) {
                deleteNode(child, branch);
            }
            this.decisionNodeCount--;
        } else if (child instanceof InactiveLearningNode) {
            this.inactiveLeafNodeCount--;
        } else if (child instanceof ActiveLearningNode) {
            this.activeLeafNodeCount--;
        }
        child = null;
    }

    public void resizeTree(Node node, int childIndex) {
        //Assume that this is root node
        if (node instanceof SplitNode) {
            for (int branch = 0; branch < ((SplitNode) node).numChildren(); branch++) {
                if (branch != childIndex) {
                    deleteNode(node, branch);
                }
            }
        }
    }
}