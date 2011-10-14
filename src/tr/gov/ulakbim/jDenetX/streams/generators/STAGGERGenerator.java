/*
 *    STAGGERGenerator.java
 *    Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *    @author Albert Bifet
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
import tr.gov.ulakbim.jDenetX.options.FlagOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import tr.gov.ulakbim.jDenetX.streams.InstanceStream;
import tr.gov.ulakbim.jDenetX.tasks.TaskMonitor;
import weka.core.*;

import java.util.Random;

// Generator described in paper:
//   Jeffrey C. Schlimmer and Richard H. Granger Jr.
//    "Incremental Learning from Noisy Data", 
//     Machine Learning 1: 317-354 1986.

//
// Notes:
// The built in functions are based on the paper (page 341)
//
public class STAGGERGenerator extends AbstractOptionHandler implements
        InstanceStream {

    @Override
    public String getPurposeString() {
        return "Generates STAGGER Concept functions.";
    }

    private static final long serialVersionUID = 1L;

    public IntOption instanceRandomSeedOption = new IntOption(
            "instanceRandomSeed", 'i',
            "Seed for random generation of instances.", 1);

    public IntOption functionOption = new IntOption("function", 'f',
            "Classification function used, as defined in the original paper.",
            1, 1, 3);

    public FlagOption balanceClassesOption = new FlagOption("balanceClasses",
            'b', "Balance the number of instances of each class.");

    protected interface ClassFunction {
        public int determineClass(int size, int color, int shape);
    }

    protected static ClassFunction[] classificationFunctions = {
            // function 1
            new ClassFunction() {
                public int determineClass(int size, int color, int shape) {
                    return (size == 0 && color == 0) ? 0 : 1; //size==small && color==red
                }
            },
            // function 2
            new ClassFunction() {
                public int determineClass(int size, int color, int shape) {
                    return (color == 2 || shape == 1) ? 0 : 1; //color==green || shape==circle
                }
            },
            // function 3
            new ClassFunction() {
                public int determineClass(int size, int color, int shape) {
                    return (size == 1 || size == 2) ? 0 : 1; // size==medium || size==large
                }
            }
    };

    protected InstancesHeader streamHeader;

    protected Random instanceRandom;

    protected boolean nextClassShouldBeZero;

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
                                     ObjectRepository repository) {
        // generate header
        FastVector attributes = new FastVector();

        FastVector sizeLabels = new FastVector();
        sizeLabels.addElement("small");
        sizeLabels.addElement("medium");
        sizeLabels.addElement("large");
        attributes.addElement(new Attribute("size", sizeLabels));

        FastVector colorLabels = new FastVector();
        colorLabels.addElement("red");
        colorLabels.addElement("blue");
        colorLabels.addElement("green");
        attributes.addElement(new Attribute("color", colorLabels));

        FastVector shapeLabels = new FastVector();
        shapeLabels.addElement("circle");
        shapeLabels.addElement("square");
        shapeLabels.addElement("triangle");
        attributes.addElement(new Attribute("shape", shapeLabels));

        FastVector classLabels = new FastVector();
        classLabels.addElement("false");
        classLabels.addElement("true");
        attributes.addElement(new Attribute("class", classLabels));
        this.streamHeader = new InstancesHeader(new Instances(
                getCLICreationString(InstanceStream.class), attributes, 0));
        this.streamHeader.setClassIndex(this.streamHeader.numAttributes() - 1);
        restart();
    }

    public long estimatedRemainingInstances() {
        return -1;
    }

    public InstancesHeader getHeader() {
        return this.streamHeader;
    }

    public boolean hasMoreInstances() {
        return true;
    }

    public boolean isRestartable() {
        return true;
    }

    public Instance nextInstance() {

        int size = 0, color = 0, shape = 0, group = 0;
        boolean desiredClassFound = false;
        while (!desiredClassFound) {
            // generate attributes
            size = this.instanceRandom.nextInt(3);
            color = this.instanceRandom.nextInt(3);
            shape = this.instanceRandom.nextInt(3);

            // determine class
            group = classificationFunctions[this.functionOption.getValue() - 1]
                    .determineClass(size, color, shape);
            if (!this.balanceClassesOption.isSet()) {
                desiredClassFound = true;
            } else {
                // balance the classes
                if ((this.nextClassShouldBeZero && (group == 0))
                        || (!this.nextClassShouldBeZero && (group == 1))) {
                    desiredClassFound = true;
                    this.nextClassShouldBeZero = !this.nextClassShouldBeZero;
                } // else keep searching
            }
        }

        // construct instance
        InstancesHeader header = getHeader();
        Instance inst = new DenseInstance(header.numAttributes());
        inst.setValue(0, size);
        inst.setValue(1, color);
        inst.setValue(2, shape);
        inst.setDataset(header);
        inst.setClassValue(group);
        return inst;
    }


    public void restart() {
        this.instanceRandom = new Random(this.instanceRandomSeedOption
                .getValue());
        this.nextClassShouldBeZero = false;
    }

    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub

    }

}
