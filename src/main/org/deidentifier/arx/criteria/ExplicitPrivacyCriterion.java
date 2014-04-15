package org.deidentifier.arx.criteria;

/**
 * A privacy criterion that is explicitly bound to a sensitive attribute
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class ExplicitPrivacyCriterion extends PrivacyCriterion {

    private static final long serialVersionUID = -6467044039242481225L;
    protected final String attribute;

    public ExplicitPrivacyCriterion(String attribute, boolean monotonic) {
        super(monotonic);
        this.attribute = attribute;
    }

    /**
     * Returns the associated sensitive attribute
     * @return
     */
    public String getAttribute() {
        return attribute;
    }
}
