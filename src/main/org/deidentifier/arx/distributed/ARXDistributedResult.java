/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.distributed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsQuality;
import org.deidentifier.arx.distribution.util.IteratorIterator;

public class ARXDistributedResult {
    
    /** Quality metrics */
    private Map<String, List<Double>> qualityMetrics = new HashMap<>();
    /** Data */
    private Data                      data;
    /** Timing */
    private long                      timePrepare;
    /** Timing */
    private long                      timeAnonymize;
    /** Timing */
    private long                      timePostprocess;

    /**
     * Creates a new instance
     * 
     * @param handles
     * @param timeAnonymize 
     * @param timePrepare 
     */
    public ARXDistributedResult(List<DataHandle> handles, long timePrepare, long timeAnonymize) {
        
        this.timePrepare = timePrepare;
        this.timeAnonymize = timeAnonymize;
        
        // Collect iterators
        long timePostprocess = System.currentTimeMillis();
        List<Iterator<String[]>> iterators = new ArrayList<>();
        for (DataHandle handle : handles) {
            iterators.add(handle.iterator());
        }
        this.data = Data.create(new IteratorIterator<String[]>(iterators));
        
        // Collect statistics
        for (DataHandle handle : handles) {
            StatisticsQuality quality = handle.getStatistics().getQualityStatistics();
            store(qualityMetrics, "AverageClassSize", quality.getAverageClassSize().getValue());
            store(qualityMetrics, "GeneralizationIntensity", quality.getGeneralizationIntensity().getArithmeticMean());
            store(qualityMetrics, "Granularity", quality.getGranularity().getArithmeticMean());
        }
        
        // Done
        timePostprocess = System.currentTimeMillis() - timePostprocess;
    }
    
    /**
     * Returns a handle to the data obtained by applying the optimal transformation. This method will fork the buffer, 
     * allowing to obtain multiple handles to different representations of the data set. Note that only one instance can
     * be obtained for each transformation.
     * 
     * @return
     */
    public DataHandle getOutput() {
        return data.getHandle();
    }

    /**
     * Returns quality estimates
     * @return
     */
    public Map<String, List<Double>> getQuality() {
        return qualityMetrics;
    }
    
    
    /**
     * Returns the time needed for anonymization
     * @return the timeAnonymize
     */
    public long getTimeAnonymize() {
        return timeAnonymize;
    }

    /**
     * Returns the time needed for postprocessing
     * @return the timePostprocess
     */
    public long getTimePostprocess() {
        return timePostprocess;
    }

    /**
     * Returns the time needed for preparation
     * @return the timePrepare
     */
    public long getTimePrepare() {
        return timePrepare;
    }

    /**
     * Store metrics
     * @param map
     * @param label
     * @param value
     */
    private void store(Map<String, List<Double>> map, String label, double value) {
        if (!map.containsKey(label)) {
            map.put(label, new ArrayList<Double>());
        }
        map.get(label).add(value);
    }
}
