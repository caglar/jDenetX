/*
 *    RandomTreeGenerator.java
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
package tr.gov.ulakbim.jDenetX.streams.generators;

import tr.gov.ulakbim.jDenetX.core.InstancesHeader;
import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
import tr.gov.ulakbim.jDenetX.options.AbstractOptionHandler;
import tr.gov.ulakbim.jDenetX.options.FloatOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import tr.gov.ulakbim.jDenetX.streams.InstanceStream;
import tr.gov.ulakbim.jDenetX.tasks.TaskMonitor;
import weka.core.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class RandomTreeGenerator extends AbstractOptionHandler implements
        InstanceStream {

    @Override
    public String getPurposeString() {
        return "Generates a stream based on a randomly generated tree.";
    }

    private static final long serialVersionUID = 1L;

    public IntOption treeRandomSeedOption = new IntOption("treeRandomSeed",
            'r', "Seed for random generation of tree.", 1);

    public IntOption instanceRandomSeedOption = new IntOption(
            "instanceRandomSeed", 'i',
            "Seed for random generation of instances.", 1);

    public IntOption numClassesOption = new IntOption("numClasses", 'c',
            "The number of classes to generate.", 8, 2, Integer.MAX_VALUE);

    public IntOption numNominalsOption = new IntOption("numNominals", 'o',
            "The number of nominal attributes to generate.", 0, 0,
            Integer.MAX_VALUE);

    public IntOption numNumericsOption = new IntOption("numNumerics", 'u',
            "The number of numeric attributes to generate.", 6, 0,
            Integer.MAX_VALUE);

    public IntOption numValsPerNominalOption = new IntOption(
            "numValsPerNominal", 'v',
            "The number of values to generate per nominal attribute.", 5, 2,
            Integer.MAX_VALUE);

    public IntOption maxTreeDepthOption = new IntOption("maxTreeDepth", 'd',
            "The maximum depth of the tree concept.", 5, 0, Integer.MAX_VALUE);

    public IntOption firstLeafLevelOption = new IntOption(
            "firstLeafLevel",
            'l',
            "The first level of the tree above maxTreeDepth that can have leaves.",
            3, 0, Integer.MAX_VALUE);

    public FloatOption leafFractionOption = new FloatOption("leafFraction",
            'f',
            "The fraction of leaves per level from firstLeafLevel onwards.",
            0.15, 0.0, 1.0);

    protected static class Node implements Serializable {

        private static final long serialVersionUID = 1L;

        public int classLabel;

        public int splitAttIndex;

        public double splitAttValue;

        public Node[] children;

    }

    protected Node treeRoot;

    protected InstancesHeader streamHeader;

    protected Random instanceRandom;

    @Override
    public void prepareForUseImpl(TaskMonitor monitor,
                                  ObjectRepository repository) {
        monitor.setCurrentActivity("Preparing random tree...", -1.0);
        generateHeader();
        generateRandomTree();
        restart();
    }

    public long estimatedRemainingInstances() {
        return -1;
    }

    public boolean isRestartable() {
        return true;
    }

    public void restart() {
        this.instanceRandom = new Random(this.instanceRandomSeedOption
                .getValue());
    }

    public InstancesHeader getHeader() {
        return this.streamHeader;
    }

    public boolean hasMoreInstances() {
        return true;
    }

    public Instance nextInstance() {
        double[] attVals = new double[this.numNominalsOption.getValue()
                + this.numNumericsOption.getValue()];
        InstancesHeader header = getHeader();
        Instance inst = new DenseInstance(header.numAttributes());
        for (int i = 0; i < attVals.length; i++) {
            attVals[i] = i < this.numNominalsOption.getValue() ? this.instanceRandom
                    .nextInt(this.numValsPerNominalOption.getValue())
                    : this.instanceRandom.nextDouble();
            inst.setValue(i, attVals[i]);
        }
        inst.setDataset(header);
        inst.setClassValue(classifyInstance(this.treeRoot, attVals));
        return inst;
    }

    protected int classifyInstance(Node node, double[] attVals) {
        if (node.children == null) {
            return node.classLabel;
        }
        if (node.splitAttIndex < this.numNominalsOption.getValue()) {
            return classifyInstance(
                    node.children[(int) attVals[node.splitAttIndex]], attVals);
        }
        return classifyInstance(
                node.children[attVals[node.splitAttIndex] < node.splitAttValue ? 0
                        : 1], attVals);
    }

    protected void generateHeader() {
        FastVector attributes = new FastVector();
        FastVector nominalAttVals = new FastVector();
        for (int i = 0; i < this.numValsPerNominalOption.getValue(); i++) {
            nominalAttVals.addElement("value" + (i + 1));
        }
        for (int i = 0; i < this.numNominalsOption.getValue(); i++) {
            attributes.addElement(new Attribute("nominal" + (i + 1),
                    nominalAttVals));
        }
        for (int i = 0; i < this.numNumericsOption.getValue(); i++) {
            attributes.addElement(new Attribute("numeric" + (i + 1)));
        }
        FastVector classLabels = new FastVector();
        for (int i = 0; i < this.numClassesOption.getValue(); i++) {
            classLabels.addElement("class" + (i + 1));
        }
        attributes.addElement(new Attribute("class", classLabels));
        this.streamHeader = new InstancesHeader(new Instances(
                getCLICreationString(InstanceStream.class), attributes, 0));
        this.streamHeader.setClassIndex(this.streamHeader.numAttributes() - 1);
    }

    protected void generateRandomTree() {
        Random treeRand = new Random(this.treeRandomSeedOption.getValue());
        ArrayList<Integer> nominalAttCandidates = new ArrayList<Integer>(
                this.numNominalsOption.getValue());
        for (int i = 0; i < this.numNominalsOption.getValue(); i++) {
            nominalAttCandidates.add(i);
        }
        double[] minNumericVals = new double[this.numNumericsOption.getValue()];
        double[] maxNumericVals = new double[this.numNumericsOption.getValue()];
        for (int i = 0; i < this.numNumericsOption.getValue(); i++) {
            minNumericVals[i] = 0.0;
            maxNumericVals[i] = 1.0;
        }
        this.treeRoot = generateRandomTreeNode(0, nominalAttCandidates,
                minNumericVals, maxNumericVals, treeRand);
    }

    protected Node generateRandomTreeNode(int currentDepth,
                                          ArrayList<Integer> nominalAttCandidates, double[] minNumericVals,
                                          double[] maxNumericVals, Random treeRand) {
        if ((currentDepth >= this.maxTreeDepthOption.getValue())
                || ((currentDepth >= this.firstLeafLevelOption.getValue()) && (this.leafFractionOption
                .getValue() >= (1.0 - treeRand.nextDouble())))) {
            Node leaf = new Node();
            leaf.classLabel = treeRand
                    .nextInt(this.numClassesOption.getValue());
            return leaf;
        }
        Node node = new Node();
        int chosenAtt = treeRand.nextInt(nominalAttCandidates.size()
                + this.numNumericsOption.getValue());
        if (chosenAtt < nominalAttCandidates.size()) {
            node.splitAttIndex = nominalAttCandidates.get(chosenAtt);
            node.children = new Node[this.numValsPerNominalOption.getValue()];
            ArrayList<Integer> newNominalCandidates = new ArrayList<Integer>(
                    nominalAttCandidates);
            newNominalCandidates.remove(Integer.valueOf(node.splitAttIndex));
            newNominalCandidates.trimToSize();
            for (int i = 0; i < node.children.length; i++) {
                node.children[i] = generateRandomTreeNode(currentDepth + 1,
                        newNominalCandidates, minNumericVals, maxNumericVals,
                        treeRand);
            }
        } else {
            int numericIndex = chosenAtt - nominalAttCandidates.size();
            node.splitAttIndex = this.numNominalsOption.getValue()
                    + numericIndex;
            double minVal = minNumericVals[numericIndex];
            double maxVal = maxNumericVals[numericIndex];
            node.splitAttValue = ((maxVal - minVal) * treeRand.nextDouble())
                    + minVal;
            node.children = new Node[2];
            double[] newMaxVals = maxNumericVals.clone();
            newMaxVals[numericIndex] = node.splitAttValue;
            node.children[0] = generateRandomTreeNode(currentDepth + 1,
                    nominalAttCandidates, minNumericVals, newMaxVals, treeRand);
            double[] newMinVals = minNumericVals.clone();
            newMinVals[numericIndex] = node.splitAttValue;
            node.children[1] = generateRandomTreeNode(currentDepth + 1,
                    nominalAttCandidates, newMinVals, maxNumericVals, treeRand);
        }
        return node;
    }

    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub

    }

}
