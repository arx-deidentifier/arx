/*
 * FLASH: Efficient, Stable and Optimal Data Anonymization
 * Copyright (C) 2012 - 2013 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.flash.metric;

import java.io.Serializable;

/**
 * This class implements an abstract base class for information loss
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class InformationLoss implements Comparable<InformationLoss>,
        Serializable {

    private static final long serialVersionUID = -5347658129539223333L;

    InformationLoss() {
        // Package visibility
    }

    /**
     * Returns a clone of this object
     */
    @Override
    protected abstract InformationLoss clone();

    /**
     * Returns the information loss
     * 
     * @return
     */
    public abstract double getValue();

    /**
     * Retains the maximum of this and other
     * 
     * @param other
     */
    public abstract void max(InformationLoss other);

    /**
     * Retains the minimum of this and other
     * 
     * @param other
     */
    public abstract void min(InformationLoss other);
}
