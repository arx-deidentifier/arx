package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.metric.InformationLoss;

/**
 * The result of a check of a transformation.
 */
public class TransformationResult {
    
    /** Overall anonymity. */
    public final Boolean privacyModelFulfilled;
    
    /** k-Anonymity sub-criterion. */
    public final Boolean minimalClassSizeFulfilled;
    
    /** Information loss. */
    public final InformationLoss<?> informationLoss;
    
    /** Lower bound. */
    public final InformationLoss<?> lowerBound;

    /**
     * Creates a new instance.
     * 
     * @param privacyModelFulfilled
     * @param minimalClassSizeFulfilled
     * @param infoLoss
     * @param lowerBound
     */
    TransformationResult(Boolean privacyModelFulfilled,
           Boolean minimalClassSizeFulfilled,
           InformationLoss<?> infoLoss,
           InformationLoss<?> lowerBound) {
        
        this.privacyModelFulfilled = privacyModelFulfilled;
        this.minimalClassSizeFulfilled = minimalClassSizeFulfilled;
        this.informationLoss = infoLoss;
        this.lowerBound = lowerBound;
    }
}