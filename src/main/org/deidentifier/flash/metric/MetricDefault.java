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

import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;

/**
 * This class provides an abstract skeleton for the implementation of metrics.
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class MetricDefault extends Metric<InformationLossDefault> {

    /**
     * 
     */
    private static final long serialVersionUID = 2672819203235170632L;

    public MetricDefault(final boolean monotonic, final boolean independent) {
        super(monotonic, independent);
    }

    @Override
    public void initializeInternal(final Data input,
                                   final GeneralizationHierarchy[] hierarchies) {
        // Empty by design
    }

    @Override
    protected InformationLoss maxInternal() {
        return InformationLossDefault.MAX;
    }

    @Override
    protected InformationLoss minInternal() {
        return InformationLossDefault.MIN;
    }

}
