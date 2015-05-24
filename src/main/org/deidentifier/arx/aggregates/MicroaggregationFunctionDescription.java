/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.aggregates;

import java.io.Serializable;

import org.deidentifier.arx.DataType.ScaleOfMeasure;
import org.deidentifier.arx.aggregates.MicroaggregateFunction.HandlingOfNullValues;

/**
 * This class represents the description of a microaggregation function and some helper functions.
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 *
 */
public abstract class MicroaggregationFunctionDescription implements Serializable {
    
    /** SVUID */
    private static final long serialVersionUID = -5815372742143860531L;
    
    /** The label */
    private String            label;
    
    /** The minimal required scale of measure. */
    private ScaleOfMeasure    requiredScaleOfMeasure;
    
    /**
     * Instantiates the decription.
     * 
     * @param label
     * @param requiredScaleOfMeasure
     */
    public MicroaggregationFunctionDescription(String label, ScaleOfMeasure requiredScaleOfMeasure) {
        this.label = label;
        this.requiredScaleOfMeasure = requiredScaleOfMeasure;
    }
    
    /**
     * Creates an microaggregationfunction instance for the given description.
     * 
     * @return
     */
    public MicroaggregateFunction createInstance() {
        return createInstance(HandlingOfNullValues.IGNORE);
    };
    
    /**
     * Creates an microaggregationfunction instance for the given description.
     * 
     * @return
     */
    public abstract MicroaggregateFunction createInstance(HandlingOfNullValues nullValueHandling);
    
    @Override
    public boolean equals(Object paramObject) {
        MicroaggregationFunctionDescription other = (MicroaggregationFunctionDescription) paramObject;
        if (other == null) {
            return false;
        }
        return other.label.equals(this.label) && other.requiredScaleOfMeasure == this.requiredScaleOfMeasure;
    }
    
    /**
     * Returns the label.
     * 
     * @return
     */
    public String getLabel() {
        return label;
    }
    
    /**
     * Returns the minimal required scale of measure.
     * @return
     */
    public ScaleOfMeasure getRequiredScaleOfMeasure() {
        return requiredScaleOfMeasure;
    }
    
    /**
     * Checks if the given function is an instance of this description.
     * 
     * @param function
     * @return
     */
    public abstract boolean isInstance(MicroaggregateFunction function);
    
}
