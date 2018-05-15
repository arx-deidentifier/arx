/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * A privacy criterion that is explicitly bound to a sensitive attribute.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class ExplicitPrivacyCriterion extends PrivacyCriterion {

    /** SVUID */
    private static final long serialVersionUID = -6467044039242481225L;
    
    /**  Attribute */
    protected final String attribute;
    
    /**  Attribute index */
    protected int index = -1;

    /**
     * Creates a new instance
     *
     * @param attribute
     * @param monotonicWithSuppression
     * @param monotonicWithGeneralization
     */
    public ExplicitPrivacyCriterion(String attribute, 
                                    boolean monotonicWithSuppression,
                                    boolean monotonicWithGeneralization) {
        super(monotonicWithSuppression, monotonicWithGeneralization);
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

    @Override
    public void initialize(DataManager manager, ARXConfiguration config) {
        String[] header = manager.getDataAnalyzed().getHeader();
        for (int i=0; i< header.length; i++){
            if (header[i].equals(attribute)) {
                index = i;
                break;
            }
        }
    }
}
