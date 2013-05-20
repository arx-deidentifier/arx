package org.deidentifier.arx.criteria;

import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;

/**
 * The distinct l-diversity privacy criterion
 * @author Fabian Prasser
 */
public class DistinctLDiversity extends LDiversity{

    private static final long serialVersionUID = -7973221140269608088L;
    
    /**
     * Creates a new instance
     * @param l
     */
    public DistinctLDiversity(int l){
        super(l);
    }

    @Override
    public boolean isAnonymous(HashGroupifyEntry entry) {
        return entry.distribution.size() >= l;
    }
}
