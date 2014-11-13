package org.deidentifier.arx.criteria;

/**
 * A privacy criterion that is implicitly bound to the quasi-identifiers.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class ImplicitPrivacyCriterion extends PrivacyCriterion {

    /**  TODO */
    private static final long serialVersionUID = -6467044039242481225L;

    /**
     * 
     *
     * @param monotonic
     */
    public ImplicitPrivacyCriterion(boolean monotonic) {
        super(monotonic);
    }

}
