package org.deidentifier.arx.criteria;

import org.deidentifier.arx.ARXConfiguration;

/**
 * An abstract base class for l-diversity criteria
 * @author Fabian Prasser
 */
public abstract class LDiversity extends PrivacyCriterion{

    private static final long serialVersionUID = 6429149925699964530L;
    
    /** The parameter l*/
    public final int l;

    /** 
     * Creates a new instance
     * @param l
     */
    public LDiversity(int l){
        super(false);
        this.l = l;    
    } 

    @Override
    public int getRequirements(){
        
        // Requires a distribution, but nothing else
        return ARXConfiguration.REQUIREMENT_DISTRIBUTION;
    }
}
