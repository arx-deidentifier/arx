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

package org.deidentifier.arx.criteria;

import java.io.Serializable;

import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.DataManager;

/**
 * An abstract base class for privacy criteria
 * @author Prasser, Kohlmayer
 */
public abstract class PrivacyCriterion implements Serializable{

    private static final long serialVersionUID = -8460571120677880409L;
    
    /** Is the criterion monotonic when allowing for tuple suppression*/
    private final boolean monotonic;
    
    /**
     * Instantiates a new criterion
     * @param snapshotLength
     * @param monotonic
     */
    public PrivacyCriterion(boolean monotonic){
        this.monotonic = monotonic;
    }
    
    /**
     * Override this to initialize the criterion
     * @param manager
     */
    public void initialize(DataManager manager){
        // Empty by design
    }

    /**
     * Returns the criterion's requirements
     * @return
     */
    public abstract int getRequirements();
    
    /**
     * Implement this, to enforce the criterion
     * @param entry
     * @return
     */
    public abstract boolean isAnonymous(HashGroupifyEntry entry);

    /**
     * Returns whether the criterion is monotonic with tuple suppression
     * @return
     */
    public boolean isMonotonic() {
        return this.monotonic;
    }
    
    /**
     * Returns a string representation
     */
    public abstract String toString();
}
