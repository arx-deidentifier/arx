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

package org.deidentifier.arx.gui.model;

import java.io.Serializable;

/**
 * This class models the current view configuration.
 *
 * @author Fabian Prasser
 */
public class ModelViewConfig implements Serializable {

    /**
     * Mode.
     */
    public static enum Mode {
        
        /**  TODO */
        SORTED_INPUT,
        
        /**  TODO */
        SORTED_OUTPUT,
        
        /**  TODO */
        GROUPED,
        
        /**  TODO */
        UNSORTED
    }

    /** SVUID. */
    private static final long serialVersionUID = 4770598345842536623L;

    /** Mode. */
    private Mode              mode             = Mode.UNSORTED;
    
    /** Attribute. */
    private String            attribute        = null;
    
    /** Subset. */
    private boolean           subset           = false;
    
    /** Sort order. */
    private boolean           sortOrder        = true;
    
    /** Changed flag. */
    private boolean           changed          = false;

    /**
     * Returns the attribute.
     *
     * @return
     */
    public String getAttribute() {
        return attribute;
    }
    
    /**
     * Returns the mode.
     *
     * @return
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the sort order.
     *
     * @return
     */
    public boolean getSortOrder(){
        return sortOrder;
    }

    /**
     * Returns whether the config has changed, and resets the flag to unmodified.
     *
     * @return
     */
    public boolean isChanged(){
        if (changed) {
            changed = false;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns whether we show the subset only.
     *
     * @return
     */
    public boolean isSubset() {
        return subset;
    }

    /**
     * Sets the attribute.
     *
     * @param attribute
     */
    public void setAttribute(String attribute) {
        if (attribute == null) return;
        if (!attribute.equals(this.attribute)) changed = true;
        this.attribute = attribute;
    }

    /**
     * Sets the mode.
     *
     * @param mode
     */
    public void setMode(Mode mode) {
        if (mode == null) return;
        if (mode != this.mode) changed = true;
        this.mode = mode;
    }

    /**
     * Sets the sort order.
     *
     * @param order
     */
    public void setSortOrder(boolean order){
        if (order != sortOrder) {
            changed = true;
            sortOrder = order;
        }
    }
    
    /**
     * Sets whether we show the subset only.
     *
     * @param subset
     */
    public void setSubset(boolean subset) {
        if (subset != this.subset) changed = true;
        this.subset = subset;
    }
}
