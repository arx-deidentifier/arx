package org.deidentifier.arx.criteria;

import org.deidentifier.arx.framework.data.DataManager;

/**
 * A privacy criterion that is explicitly bound to a sensitive attribute.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class ExplicitPrivacyCriterion extends PrivacyCriterion {

    /**  TODO */
    private static final long serialVersionUID = -6467044039242481225L;
    
    /**  TODO */
    protected final String attribute;
    
    /**  TODO */
    protected int index = -1;

    /**
     * 
     *
     * @param attribute
     * @param monotonic
     */
    public ExplicitPrivacyCriterion(String attribute, boolean monotonic) {
        super(monotonic);
        this.attribute = attribute;
    }

    /**
     * Returns the associated sensitive attribute.
     *
     * @return
     */
    public String getAttribute() {
        return attribute;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.criteria.PrivacyCriterion#initialize(org.deidentifier.arx.framework.data.DataManager)
     */
    @Override
    public void initialize(DataManager manager) {
        String[] header = manager.getDataSE().getHeader();
        for (int i=0; i< header.length; i++){
            if (header[i].equals(attribute)) {
                index = i;
                break;
            }
        }
    }
}
