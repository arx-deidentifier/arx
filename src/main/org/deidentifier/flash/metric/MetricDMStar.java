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

import org.deidentifier.flash.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * This class provides an implementation of the DM* metric (monotonic variant of
 * the Discernability Metric).
 * 
 * @author Prasser, Kohlmayer
 */
public class MetricDMStar extends MetricDefault {

    /**
     * 
     */
    private static final long serialVersionUID = -3324788439890959974L;

    public MetricDMStar() {
        super(true, false);
    }

    @Override
    public InformationLossDefault evaluateInternal(final Node node,
                                                   final IHashGroupify g) {
        double value = 0;
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            value += (double) m.count * (double) m.count;
            m = m.nextOrdered;
        }
        return new InformationLossDefault(value);
    }
}
