package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;

/**
 * An abstract base class for t-closeness criteria
 * @author Fabian Prasser
 */
public abstract class TCloseness extends PrivacyCriterion {

    private static final long serialVersionUID = -139670758266526116L;
    
    /** The param t*/
    public final double       t;
    
    /**
     * Creates a new instance
     * @param t
     */
    public TCloseness(double t) {
        super(false);
        this.t = t;
    }

    @Override
    public int getRequirements(){
        // Requires a distribution
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }
}
