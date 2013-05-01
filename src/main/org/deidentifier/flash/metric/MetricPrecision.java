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

import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * This class provides an implementation of the Precision metric.
 * 
 * @author Prasser, Kohlmayer
 */
public class MetricPrecision extends MetricDefault {

    /**
     * 
     */
    private static final long serialVersionUID = -7612335677779934529L;
    /** The maximum levels */
    private int[]             maxLevels;

    public MetricPrecision() {
        super(true, true);
    }

    @Override
    public InformationLossDefault evaluateInternal(final Node node,
                                                   final IHashGroupify g) {

        double value = 0;
        double divisor = 0;
        final int[] state = node.getTransformation();
        for (int i = 0; i < state.length; i++) {
            divisor++;
            value += (double) state[i] / (double) maxLevels[i];
        }
        return new InformationLossDefault(value / divisor);
    }

    @Override
    public void initializeInternal(final Data input,
                                   final GeneralizationHierarchy[] hierarchies) {

        // Initialize maximum levels
        maxLevels = new int[hierarchies.length];
        for (int j = 0; j < maxLevels.length; j++) {
            maxLevels[j] = hierarchies[j].getArray()[0].length;
        }
    }
}
