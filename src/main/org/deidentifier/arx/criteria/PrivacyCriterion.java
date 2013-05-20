package org.deidentifier.arx.criteria;

import java.io.Serializable;

import org.deidentifier.arx.framework.data.DataManager;

/**
 * An abstract base class for privacy criteria
 * @author Fabian Prasser
 */
public abstract class PrivacyCriterion implements Serializable{

    private static final long serialVersionUID = -8460571120677880409L;
    
    /** Is the criterion monotonic when allowing for tuple suppression*/
    public final boolean monotonic;
    
    /**
     * Instantiates a new criterion
     * @param snapshotLength
     * @param monotonic
     */
    public PrivacyCriterion(boolean monotonic){
        this.monotonic = monotonic;
    }
    
    /**
     * Override this to initialize the criterion
     * @param manager
     */
    public void initialize(DataManager manager){
        // Empty by design
    }

    /**
     * Returns the criterion's requirements
     * @return
     */
    public abstract int getRequirements();
}
