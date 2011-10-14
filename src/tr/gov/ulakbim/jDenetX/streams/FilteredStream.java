/*
 *    FilteredStream.java
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
package tr.gov.ulakbim.jDenetX.streams;

import tr.gov.ulakbim.jDenetX.core.InstancesHeader;
import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
import tr.gov.ulakbim.jDenetX.options.*;
import tr.gov.ulakbim.jDenetX.streams.filters.StreamFilter;
import tr.gov.ulakbim.jDenetX.tasks.TaskMonitor;
import weka.core.Instance;

public class FilteredStream extends AbstractOptionHandler implements
        InstanceStream {

    @Override
    public String getPurposeString() {
        return "A stream that is filtered.";
    }

    private static final long serialVersionUID = 1L;

    public ClassOption streamOption = new ClassOption("stream", 's',
            "Stream to filter.", InstanceStream.class,
            "generators.RandomTreeGenerator");

    public ListOption filtersOption = new ListOption("filters", 'f',
            "Filters to apply.", new ClassOption("filter", ' ',
                    "Stream filter.", StreamFilter.class, "AddNoiseFilter"),
            new Option[0], ',');

    protected InstanceStream filterChain;

    @Override
    public void prepareForUseImpl(TaskMonitor monitor,
                                  ObjectRepository repository) {
        Option[] filterOptions = this.filtersOption.getList();
        StreamFilter[] filters = new StreamFilter[filterOptions.length];
        for (int i = 0; i < filters.length; i++) {
            monitor.setCurrentActivity("Materializing filter " + (i + 1)
                    + "...", -1.0);
            filters[i] = (StreamFilter) ((ClassOption) filterOptions[i])
                    .materializeObject(monitor, repository);
            if (monitor.taskShouldAbort()) {
                return;
            }
            if (filters[i] instanceof OptionHandler) {
                monitor.setCurrentActivity("Preparing filter " + (i + 1)
                        + "...", -1.0);
                ((OptionHandler) filters[i]).prepareForUse(monitor, repository);
                if (monitor.taskShouldAbort()) {
                    return;
                }
            }
        }
        InstanceStream chain = (InstanceStream) getPreparedClassOption(this.streamOption);
        for (int i = filters.length - 1; i >= 0; i--) {
            filters[i].setInputStream(chain);
            chain = filters[i];
        }
        this.filterChain = chain;
    }

    public long estimatedRemainingInstances() {
        return this.filterChain.estimatedRemainingInstances();
    }

    public InstancesHeader getHeader() {
        return this.filterChain.getHeader();
    }

    public boolean hasMoreInstances() {
        return this.filterChain.hasMoreInstances();
    }

    public boolean isRestartable() {
        return this.filterChain.isRestartable();
    }

    public Instance nextInstance() {
        return this.filterChain.nextInstance();
    }

    public void restart() {
        this.filterChain.restart();
    }

    public void getDescription(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub

    }

}
