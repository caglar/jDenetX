/*
 *    CacheShuffledStream.java
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
package tr.gov.ulakbim.jDenetX.tasks;

import tr.gov.ulakbim.jDenetX.core.ObjectRepository;
import tr.gov.ulakbim.jDenetX.options.ClassOption;
import tr.gov.ulakbim.jDenetX.options.IntOption;
import tr.gov.ulakbim.jDenetX.streams.CachedInstancesStream;
import tr.gov.ulakbim.jDenetX.streams.InstanceStream;
import weka.core.Instances;

import java.util.Random;

public class CacheShuffledStream extends AbstractTask {

    @Override
    public String getPurposeString() {
        return "Stores and shuffles examples in memory.";
    }

    private static final long serialVersionUID = 1L;

    public ClassOption streamOption = new ClassOption("stream", 's',
            "Stream to cache and shuffle.", InstanceStream.class,
            "generators.RandomTreeGenerator");

    public IntOption maximumCacheSizeOption = new IntOption("maximumCacheSize",
            'm', "Maximum number of instances to cache.", 1000000, 1,
            Integer.MAX_VALUE);

    public IntOption shuffleRandomSeedOption = new IntOption(
            "shuffleRandomSeed", 'r',
            "Seed for random shuffling of instances.", 1);

    @Override
    protected Object doTaskImpl(TaskMonitor monitor, ObjectRepository repository) {
        InstanceStream stream = (InstanceStream) getPreparedClassOption(this.streamOption);
        Instances cache = new Instances(stream.getHeader(), 0);
        monitor.setCurrentActivity("Caching instances...", -1.0);
        while ((cache.numInstances() < this.maximumCacheSizeOption.getValue())
                && stream.hasMoreInstances()) {
            cache.add(stream.nextInstance());
            if (cache.numInstances()
                    % MainTask.INSTANCES_BETWEEN_MONITOR_UPDATES == 0) {
                if (monitor.taskShouldAbort()) {
                    return null;
                }
                long estimatedRemainingInstances = stream
                        .estimatedRemainingInstances();
                long maxRemaining = this.maximumCacheSizeOption.getValue()
                        - cache.numInstances();
                if ((estimatedRemainingInstances < 0)
                        || (maxRemaining < estimatedRemainingInstances)) {
                    estimatedRemainingInstances = maxRemaining;
                }
                monitor
                        .setCurrentActivityFractionComplete(estimatedRemainingInstances < 0 ? -1.0
                                : (double) cache.numInstances()
                                / (double) (cache.numInstances() + estimatedRemainingInstances));
            }
        }
        monitor.setCurrentActivity("Shuffling instances...", -1.0);
        cache.randomize(new Random(this.shuffleRandomSeedOption.getValue()));
        return new CachedInstancesStream(cache);
    }

    public Class<?> getTaskResultType() {
        return CachedInstancesStream.class;
    }

}
