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
package org.deidentifier.arx.gui.model;

import java.io.Serializable;

/**
 * This class implements an entry for the audit trail. Actions that are logged should
 * extend this abstract base class.
 * 
 * @author Fabian Prasser
 */
public abstract class ModelAuditTrailEntry implements Serializable {

    /**
     * Find and replace entry
     * @author Fabian Prasser
     */
    public static class AuditTrailEntryFindReplace extends ModelAuditTrailEntry {
        
        /** SVUID*/
        private static final long serialVersionUID = -2321052598039892818L;
        
        /** The attribute*/
        private final String attribute;
        /** The search string*/
        private final String searchString;
        /** The replacement string*/
        private final String replacementString;
        
        /**
         * Constructor
         * @param attribute
         * @param searchString
         * @param replacementString
         */
        public AuditTrailEntryFindReplace(String attribute,
                                           String searchString,
                                           String replacementString) {
            this.attribute = attribute;
            this.searchString = searchString;
            this.replacementString = replacementString;
        }

        /**
         * @return the attribute
         */
        public String getAttribute() {
            return attribute;
        }

        /**
         * @return the replacementString
         */
        public String getReplacementString() {
            return replacementString;
        }

        /**
         * @return the searchString
         */
        public String getSearchString() {
            return searchString;
        }
    }

    /** SVUID*/
    private static final long serialVersionUID = -3945294611839543672L;
    
    /**
     * Returns an entry for the according action
     * @param attribute
     * @param searchString
     * @param replacementString
     * @return
     */
    public static ModelAuditTrailEntry createfindReplaceEntry(String attribute, String searchString, String replacementString) {
        return new AuditTrailEntryFindReplace(attribute, searchString, replacementString);
    }
}
