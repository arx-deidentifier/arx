package org.deidentifier.arx.distribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.aggregates.StatisticsQuality;

public class ARXDistributedResult {

    /** Quality metrics */
    private Map<String, List<Double>> qualityMetrics = new HashMap<>();
    /** Data */
    private Data                data;

    /**
     * Creates a new instance
     * 
     * @param handles
     */
    public ARXDistributedResult(List<DataHandle> handles) {
        
        // Collect iterators
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
