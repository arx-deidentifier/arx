package org.deidentifier.arx.criteria;

/**
 * The recursive-(c,l)-diversity criterion
 * @author Fabian Prasser
 */
public class RecursiveCLDiversity extends LDiversity{

    private static final long serialVersionUID = -5893481096346270328L;

    /** The parameter c */
    public final double c;
    
    /**
     * Creates a new instance
     * @param c
     * @param l
     */
    public RecursiveCLDiversity(double c, int l){
        super(l);
        this.c = c;
    }
}
