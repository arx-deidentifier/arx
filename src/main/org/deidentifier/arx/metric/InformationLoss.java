/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.metric;

import java.io.Serializable;

import org.deidentifier.arx.metric.v2.AbstractMetricMultiDimensional;
import org.deidentifier.arx.metric.v2.AbstractMetricSingleDimensional;
import org.deidentifier.arx.metric.v2.MetricMDHeight;
import org.deidentifier.arx.metric.v2.MetricMDNMPrecision;
import org.deidentifier.arx.metric.v2.MetricMDNUEntropyPrecomputed;
import org.deidentifier.arx.metric.v2.__MetricV2;

/**
 * This class implements an abstract base class for information loss
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class InformationLoss<T> implements Comparable<InformationLoss<?>>, Serializable {

    private static final long serialVersionUID = -5347658129539223333L;
    
    protected InformationLoss(){
        // Protected
    }
    
    /**
     * Returns a clone of this object
     */
    @Override
    public abstract InformationLoss<T> clone();
    
    /**
     * Compares the loss to the other
     * @param other
     * @return
     */
    public abstract int compareTo(InformationLoss<?> other);
    
    @Override
    public abstract boolean equals(Object obj);

    public abstract T getValue();

    @Override
    public abstract int hashCode();

    /**
     * Retains the maximum of this and other
     * 
     * @param other
     */
    public abstract void max(InformationLoss<?> other);

    /**
     * Retains the minimum of this and other
     * 
     * @param other
     */
    public abstract void min(InformationLoss<?> other);
    
    /**
     * Returns the value relative to the other instance
     * @param other
     * @return
     */
    public abstract double relativeTo(InformationLoss<?> min, InformationLoss<?> max);

    /**
     * Returns a string representation
     * 
     * @return
     */
    public abstract String toString();

    /**
     * Converter method, converting information loss from version 1 to information loss from version 2,
     * if necessary
     * 
     * @param loss
     * @param metric
     * @return
     */
    public static InformationLoss<?> createInformationLoss(InformationLoss<?> loss, Metric<?> metric) {
        
        Metric<?> _metric = Metric.createMetric(metric);

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
}
