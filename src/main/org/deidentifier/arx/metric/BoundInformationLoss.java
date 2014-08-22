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

/**
 * Information loss with a potential lower bound
 * @author Fabian Prasser
 *
 */
public class BoundInformationLoss<T extends InformationLoss<?>> {

    /** Lower bound, if any*/
    private final T lowerBound;
    /** Actual information loss*/
    private final T informationLoss;
    
    /**
     * Creates a new instance
     * @param informationLoss
     * @param lowerBound
     */
    public BoundInformationLoss(T informationLoss, T lowerBound) {
        this.lowerBound = lowerBound;
        this.informationLoss = informationLoss;
    }

    /**
     * Creates a new instance without a lower bound
     * @param informationLoss
     */
    public BoundInformationLoss(T informationLoss) {
        this.lowerBound = null;
        this.informationLoss = informationLoss;
    }
    
    /**
     * Is a lower bound provided
     * @return
     */
    public boolean hasLowerBound(){
        return this.lowerBound != null;
    }

    /**
     * @return the lowerBound
     */
    public T getLowerBound() {
        return lowerBound;
    }

    /**
     * @return the informationLoss
     */
    public T getInformationLoss() {
        return informationLoss;
    }
}
