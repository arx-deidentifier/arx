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

package org.deidentifier.arx.metric;

import java.io.Serializable;

/**
 * This class implements an abstract base class for information loss
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class InformationLoss<T> implements Comparable<InformationLoss<?>>, Serializable {

    private static final long serialVersionUID = -5347658129539223333L;
    
    protected InformationLoss(){
        // Protected
    }
    
    public abstract T getValue();
    
    /**
     * Returns the value relative to the other instance
     * @param other
     * @return
     */
    public abstract double relativeTo(InformationLoss<?> min, InformationLoss<?> max);
    
    /**
     * Compares the loss to the other
     * @param other
     * @return
     */
    public abstract int compareTo(InformationLoss<?> other);

    /**
     * Returns a string representation
     * 
     * @return
     */
    public abstract String toString();

    /**
     * Retains the maximum of this and other
     * 
     * @param other
     */
    public abstract void max(InformationLoss<?> other);

    /**
     * Retains the minimum of this and other
     * 
     * @param other
     */
    public abstract void min(InformationLoss<?> other);

    /**
     * Returns a clone of this object
     */
    @Override
    public abstract InformationLoss<T> clone();
    
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
