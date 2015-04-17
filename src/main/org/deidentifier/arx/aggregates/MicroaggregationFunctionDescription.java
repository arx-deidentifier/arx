package org.deidentifier.arx.aggregates;

import java.io.Serializable;

import org.deidentifier.arx.DataType.ScaleOfMeasure;
import org.deidentifier.arx.aggregates.MicroaggregateFunction.HandlingOfNullValues;

/**
 * This class represents the description of a microaggregation function and some helper functions.
 * @author Florian Kohlmayer
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
