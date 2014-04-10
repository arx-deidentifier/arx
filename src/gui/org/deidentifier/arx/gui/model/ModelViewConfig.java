/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

public class ModelViewConfig implements Serializable {

    public static enum Mode {
        SORTED_INPUT,
        SORTED_OUTPUT,
        GROUPED,
        UNSORTED
    }

    private static final long serialVersionUID = 4770598345842536623L;

    private Mode              mode             = Mode.UNSORTED;
    private String            attribute        = null;
    private boolean           subset           = false;
    private boolean           inputSortOrder   = false;
    private boolean           outputSortOrder  = false;
    private boolean           changed          = false;

    public boolean getSortOrder(){
        
        if (mode == Mode.SORTED_INPUT) {
            inputSortOrder = !inputSortOrder;
            changed = true;
            return inputSortOrder;
        } else if (mode == Mode.SORTED_OUTPUT) {
            outputSortOrder = !outputSortOrder;
            changed = true;
            return outputSortOrder;
        } else {
            throw new IllegalStateException("Sort order not available");
        }
    }

    public String getAttribute() {
        return attribute;
    }

    public Mode getMode() {
        return mode;
    }

    public boolean isSubset() {
        return subset;
    }

    public void setAttribute(String attribute) {
        if (attribute == null) return;
        if (!attribute.equals(this.attribute)) changed = true;
        this.attribute = attribute;
    }

    public void setMode(Mode mode) {
        if (mode == null) return;
        if (mode != this.mode) changed = true;
        this.mode = mode;
    }

    public void setSubset(boolean subset) {
        if (subset != this.subset) changed = true;
        this.subset = subset;
    }
    
    /**
     * Returns whether the config has changed, and resets the flag to not-changed
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
}
