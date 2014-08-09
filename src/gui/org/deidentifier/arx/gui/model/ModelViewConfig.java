/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.model;

import java.io.Serializable;

/**
 * This class models the current view configuration
 * @author Fabian Prasser
 *
 */
public class ModelViewConfig implements Serializable {

    /** Mode*/
    public static enum Mode {
        SORTED_INPUT,
        SORTED_OUTPUT,
        GROUPED,
        UNSORTED
    }

    /** SVUID*/
    private static final long serialVersionUID = 4770598345842536623L;

    /** Mode*/
    private Mode              mode             = Mode.UNSORTED;
    /** Attribute*/
    private String            attribute        = null;
    /** Subset*/
    private boolean           subset           = false;
    /** Sort order*/
    private boolean           sortOrder        = true;
    /** Changed flag*/
    private boolean           changed          = false;

    /**
     * Returns the attribute
     * @return
     */
    public String getAttribute() {
        return attribute;
    }
    
    /**
     * Returns the mode
     * @return
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Returns the sort order
     * @return
     */
    public boolean getSortOrder(){
        return sortOrder;
    }

    /**
     * Returns whether the config has changed, and resets the flag to unmodified
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
     * Returns whether we show the subset only
     * @return
     */
    public boolean isSubset() {
        return subset;
    }

    /**
     * Sets the attribute
     * @param attribute
     */
    public void setAttribute(String attribute) {
        if (attribute == null) return;
        if (!attribute.equals(this.attribute)) changed = true;
        this.attribute = attribute;
    }

    /**
     * Sets the mode
     * @param mode
     */
    public void setMode(Mode mode) {
        if (mode == null) return;
        if (mode != this.mode) changed = true;
        this.mode = mode;
    }

    /**
     * Sets the sort order
     * @param order
     */
    public void setSortOrder(boolean order){
        if (order != sortOrder) {
            changed = true;
            sortOrder = order;
        }
    }
    
    /**
     * Sets whether we show the subset only
     * @param subset
     */
    public void setSubset(boolean subset) {
        if (subset != this.subset) changed = true;
        this.subset = subset;
    }
}
