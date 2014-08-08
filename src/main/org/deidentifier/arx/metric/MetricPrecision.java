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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of a weighted precision metric.
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricPrecision extends MetricWeighted<InformationLossDefault> {

    /** SSUID*/
    private static final long serialVersionUID = -7612335677779934529L;

    /** The maximum levels */
    private int[]             maxLevels;

    protected MetricPrecision() {
        super(true, true);
    }

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        return new InformationLossDefault(Double.MAX_VALUE);
    }

    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new InformationLossDefault(Double.MIN_VALUE);
    }

    @Override
    public Metric<InformationLossDefault> createMonotonicSubMetric() {
        return null;
    }

    @Override
    public String toString() {
        return "Precision";
    }

    @Override
    protected InformationLossDefault evaluateInternal(final Node node, final IHashGroupify g) {

        double value = 0;
        double divisor = 0;
        final int[] state = node.getTransformation();
        for (int i = 0; i < state.length; i++) {
            divisor++;
            double weight = weights != null ? weights[i] : 1d;
            value += ((double) state[i] / (double) maxLevels[i]) * weight;
        }
        return new InformationLossDefault(value / divisor);
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);

        // Initialize maximum levels
        maxLevels = new int[hierarchies.length];
        for (int j = 0; j < maxLevels.length; j++) {
            maxLevels[j] = hierarchies[j].getArray()[0].length;
        }
    }
}
