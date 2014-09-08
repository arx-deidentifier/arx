package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;

public class Metric_V2 {

    /**
     * Creates an average equivalence class size
     * 
     * @return
     */
    public static Metric<?> createAECSMetric() {
        return new MetricSDAECS();
    }

    /**
     * Creates a DM* metric
     * 
     * @param k
     * @return
     */
    public static Metric<?> createDMMetric() {
        return new MetricSDDiscernability();
    }

    /**
     * Creates a DM* metric
     * 
     * @return
     */
    public static Metric<?> createDMStarMetric() {
        return new MetricSDNMDiscernability();
    }

    /**
     * Creates an entropy metric
     * 
     * @return
     */
    public static Metric<?> createEntropyMetric() {
        return new MetricMDNUEntropy();
    }

    /**
     * Creates a height metric
     * 
     * @return
     */
    public static Metric<?> createHeightMetric() {
        return new MetricMDHeight();
    }

    /**
     * Creates an instance of the NDS metric that treats suppression, generalization and
     * all attributes equally.
     * This metric will respect attribute weights defined in the configuration.
     * @return
     */
    public static Metric<?> createNDSMetric() {
        return new MetricMDNMNormalizedDomainShare();
    }
    

    /**
     * Creates an NDS metric with factors for weighting generalization and suppression.
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @param gsFactor A factor [0,1] weighting generalization and suppression. 
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor generalization, and a factor of 1 will favor
     *                 suppression. The values in between can be used for
     *                 balancing both methods. 
     */
    public static Metric<?> createNDSMetric(double gsFactor) {
        return new MetricMDNMNormalizedDomainShare(gsFactor, AggregateFunction.RANK);
    }
    
    /**
     * Creates an non-monotonic entropy metric
     * 
     * @return
     */
    public static Metric<?> createNMEntropyMetric() {
        return new MetricMDNUNMEntropy();
    }

    /**
     * Creates a non-monotonic precision metric. 
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<?> createNMPrecisionMetric() {
        return new MetricMDNMPrecision();
    }
    /**
     * Creates a precision metric. 
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<?> createPrecisionMetric() {
        return new MetricMDPrecision();
    }
    
    /**
     * Creates a precision metric with conservative estimation 
     * This metric will respect attribute weights defined in the configuration.
     * 
     * @return
     */
    public static Metric<?> createPrecisionRCEMetric() {
        return new MetricMDPrecision(AggregateFunction.RANK);
    }

}
