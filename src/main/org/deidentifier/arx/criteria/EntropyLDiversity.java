package org.deidentifier.arx.criteria;

/**
 * The entropy l-diversity criterion
 * @author Fabian Prasser
 */
public class EntropyLDiversity extends LDiversity{

    private static final long serialVersionUID = -354688551915634000L;

    /**
     * Creates a new instance
     * @param l
     */
    public EntropyLDiversity(int l){
        super(l);
    } 
}
