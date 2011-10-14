/*
 *    WaveformGeneratorDrift.java
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

public class WaveformGeneratorDrift extends WaveformGenerator {

    private static final long serialVersionUID = 1L;

    public IntOption numberAttributesDriftOption = new IntOption("numberAttributesDrift",
            'd', "Number of attributes with drift.", 0, 0, TOTAL_ATTRIBUTES_INCLUDING_NOISE);

    protected int[] numberAttribute;

    @Override
    public String getPurposeString() {
        return "Generates a problem of predicting one of three waveform types with drift.";
    }

    @Override
    protected void prepareForUseImpl(TaskMonitor monitor,
                                     ObjectRepository repository) {
        super.prepareForUseImpl(monitor, repository);
        int numAtts = this.addNoiseOption.isSet() ? TOTAL_ATTRIBUTES_INCLUDING_NOISE
                : NUM_BASE_ATTRIBUTES;
        this.numberAttribute = new int[numAtts];
        for (int i = 0; i < numAtts; i++) {
            this.numberAttribute[i] = i;
        }
        //Change atributes
        int randomInt = this.instanceRandom.nextInt(numAtts);
        int offset = this.instanceRandom.nextInt(numAtts);
        for (int i = 0; i < this.numberAttributesDriftOption.getValue(); i++) {
            this.numberAttribute[(i + randomInt) % numAtts] = (i + offset) % numAtts;
            this.numberAttribute[(i + offset) % numAtts] = (i + randomInt) % numAtts;
        }
    }

    @Override
    public Instance nextInstance() {
        InstancesHeader header = getHeader();
        Instance inst = new DenseInstance(header.numAttributes());
        inst.setDataset(header);
        int waveform = this.instanceRandom.nextInt(NUM_CLASSES);
        int choiceA = 0, choiceB = 0;
        switch (waveform) {
            case 0:
                choiceA = 0;
                choiceB = 1;
                break;
            case 1:
                choiceA = 0;
                choiceB = 2;
                break;
            case 2:
                choiceA = 1;
                choiceB = 2;
                break;

        }
        double multiplierA = this.instanceRandom.nextDouble();
        double multiplierB = 1.0 - multiplierA;
        for (int i = 0; i < NUM_BASE_ATTRIBUTES; i++) {
            inst.setValue(this.numberAttribute[i], (multiplierA * hFunctions[choiceA][i])
                    + (multiplierB * hFunctions[choiceB][i])
                    + this.instanceRandom.nextGaussian());
        }
        if (this.addNoiseOption.isSet()) {
            for (int i = NUM_BASE_ATTRIBUTES; i < TOTAL_ATTRIBUTES_INCLUDING_NOISE; i++) {
                inst.setValue(this.numberAttribute[i], this.instanceRandom.nextGaussian());
            }
        }
        inst.setClassValue(waveform);
        return inst;
    }

    @Override
    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub

    }

}
