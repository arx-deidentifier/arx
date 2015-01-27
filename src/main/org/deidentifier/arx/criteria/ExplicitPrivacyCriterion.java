/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
