/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

package org.deidentifier.arx.metric;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional;
import org.deidentifier.arx.metric.v2.AbstractMetricSingleDimensional;
import org.deidentifier.arx.metric.v2.MetricMDHeight;
import org.deidentifier.arx.metric.v2.MetricMDNMPrecision;
import org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed;
import org.deidentifier.arx.metric.v2.QualityMetadata;
import org.deidentifier.arx.metric.v2.__MetricV2;

/**
 * This class implements an abstract base class for information loss.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 * @param <T>
 */
public abstract class InformationLoss<T> implements Comparable<InformationLoss<?>>, Serializable {

    /** SVUID */
    private static final long        serialVersionUID = -5347658129539223333L;

    /**
     * Converter method, converting information loss from version 1 to information loss from version 2,
     * if necessary.
     *
     * @param loss
     * @param metric
     * @param minLevel
     * @param maxLevel
     * @return
     */
    public static InformationLoss<?> createInformationLoss(InformationLoss<?> loss, Metric<?> metric, int minLevel, int maxLevel) {
        
        Metric<?> _metric = Metric.createMetric(metric, minLevel, maxLevel);

        if (loss instanceof InformationLossDefault){
            if (_metric instanceof AbstractMetricSingleDimensional) {
                return __MetricV2.createILSingleDimensional(((InformationLossDefault)loss).getValue());
            } else if (_metric instanceof AbstractMetricMultiDimensional) {
                if (_metric instanceof MetricMDNUEntropyPrecomputed) {
                    return __MetricV2.createILMultiDimensionalSum(((InformationLossDefault)loss).getValue()); 
                } else if (_metric instanceof MetricMDHeight) {
                    return __MetricV2.createILMultiDimensionalSum(((InformationLossDefault)loss).getValue()); 
                } else if (_metric instanceof MetricMDNMPrecision) {
                    return __MetricV2.createILMultiDimensionalArithmeticMean(((InformationLossDefault)loss).getValue()); 
                } 
            } 
        }
        
        // Default
        return loss;
    }

    /** Metadata */
    private List<QualityMetadata<?>> metadata         = new ArrayList<QualityMetadata<?>>();
    
    /**
     * Creates a new instance
     */
    protected InformationLoss(){
        // Protected
    }
    
    /**
     * Adds new metadata
     * 
     * @param metadata
     * @return
     */
    protected void addMetadata(QualityMetadata<?> metadata) {
        
        // Backwards compatibility
        if (this.metadata == null) {
            this.metadata = new ArrayList<QualityMetadata<?>>();
        }
        this.metadata.add(metadata);
    }
    
    /**
     * Returns a clone of this object.
     *
     * @return
     */
    @Override
    public abstract InformationLoss<T> clone();

    /**
     * Compares the loss to the other.
     *
     * @param other
     * @return
     */
    public abstract int compareTo(InformationLoss<?> other);

    @Override
    public abstract boolean equals(Object obj);

    /**
     * Adds new metadata
     * 
     * @param metadata
     * @return
     */
    public List<QualityMetadata<?>> getMetadata() {
        
        // Backwards compatibility
        if (this.metadata == null) {
            return new ArrayList<QualityMetadata<?>>();
        } else {
            return new ArrayList<QualityMetadata<?>>(this.metadata);
        }
    }

    /**
     * Returns the value
     *
     * @return
     */
    public abstract T getValue();
    
    @Override
    public abstract int hashCode();

    /**
     * Retains the maximum of this and other.
     *
     * @param other
     */
    public abstract void max(InformationLoss<?> other);

    /**
     * Retains the minimum of this and other.
     *
     * @param other
     */
    public abstract void min(InformationLoss<?> other);

    /**
     * Returns the value relative to the other instance.
     *
     * @param min
     * @param max
     * @return
     */
    public abstract double relativeTo(InformationLoss<?> min, InformationLoss<?> max);

    /**
     * Returns a string representation.
     *
     * @return
     */
    public abstract String toString();
}
