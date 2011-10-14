/*
 *    LEDGeneratorDrift.java
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
import tr.gov.ulakbim.jDenetX.options.IntOption;
import tr.gov.ulakbim.jDenetX.tasks.TaskMonitor;
import weka.core.DenseInstance;
import weka.core.Instance;

public class LEDGeneratorDrift extends LEDGenerator {

    @Override
    public String getPurposeString() {
        return "Generates a problem of predicting the digit displayed on a 7-segment LED display with drift.";
    }

    private static final long serialVersionUID = 1L;

    public IntOption numberAttributesDriftOption = new IntOption("numberAttributesDrift",
            'd', "Number of attributes with drift.", 1, 0, 7);

    protected int[] numberAttribute;

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
                                     ObjectRepository repository) {
        super.prepareForUseImpl(monitor, repository);
        this.numberAttribute = new int[7 + NUM_IRRELEVANT_ATTRIBUTES];
        for (int i = 0; i < 7 + NUM_IRRELEVANT_ATTRIBUTES; i++) {
            this.numberAttribute[i] = i;
        }
        //Change atributes
        if (!this.suppressIrrelevantAttributesOption.isSet() && this.numberAttributesDriftOption.getValue() > 0) {
            int randomInt = 0;//this.instanceRandom.nextInt(7);
            int offset = 0;//this.instanceRandom.nextInt(NUM_IRRELEVANT_ATTRIBUTES);
            for (int i = 0; i < this.numberAttributesDriftOption.getValue(); i++) {
                int value1 = (i + randomInt) % 7;
                int value2 = 7 + ((i + offset) % (NUM_IRRELEVANT_ATTRIBUTES));
                this.numberAttribute[value1] = value2;
                this.numberAttribute[value2] = value1;
            }
        }
    }

    @Override
    public Instance nextInstance() {
        InstancesHeader header = getHeader();
        Instance inst = new DenseInstance(header.numAttributes());
        inst.setDataset(header);
        int selected = this.instanceRandom.nextInt(10);
        for (int i = 0; i < 7; i++) {
            if ((1 + (this.instanceRandom.nextInt(100))) <= this.noisePercentageOption
                    .getValue()) {
                inst.setValue(this.numberAttribute[i], originalInstances[selected][i] == 0 ? 1 : 0);
            } else {
                inst.setValue(this.numberAttribute[i], originalInstances[selected][i]);
            }
        }
        if (!this.suppressIrrelevantAttributesOption.isSet()) {
            for (int i = 0; i < NUM_IRRELEVANT_ATTRIBUTES; i++) {
                inst.setValue(this.numberAttribute[i + 7], this.instanceRandom.nextInt(2));
            }
        }
        inst.setClassValue(selected);
        return inst;
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub

    }

}
