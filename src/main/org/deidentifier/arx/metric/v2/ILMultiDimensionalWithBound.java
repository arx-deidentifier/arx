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

package org.deidentifier.arx.metric.v2;

import org.deidentifier.arx.metric.InformationLossWithBound;

/**
 * Information loss with a potential lower bound.
 *
 * @author Fabian Prasser
 */
public class ILMultiDimensionalWithBound extends InformationLossWithBound<AbstractILMultiDimensional> {

    /**
     * Creates a new instance without a lower bound.
     *
     * @param informationLoss
     */
    public ILMultiDimensionalWithBound(AbstractILMultiDimensional informationLoss) {
        super(informationLoss);
    }

    /**
     * Creates a new instance.
     *
     * @param informationLoss
     * @param lowerBound
     */
    public ILMultiDimensionalWithBound(AbstractILMultiDimensional informationLoss,
                                       AbstractILMultiDimensional lowerBound) {
        super(informationLoss, lowerBound);
    }
}
