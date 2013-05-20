package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;

/**
 * The k-anonymity criterion
 * @author Fabian Prasser
 */
public class KAnonymity extends PrivacyCriterion{

    private static final long serialVersionUID = -8370928677928140572L;
    
    /** The parameter k*/
    public final int k;
    
    /**
     * Creates a new instance
     * @param k
     */
    public KAnonymity(int k){
        super(true);
        this.k = k;
    }

    @Override
    public int getRequirements(){
        // Requires only one counter
        return ARXConfiguration.REQUIREMENT_COUNTER;
    }
}