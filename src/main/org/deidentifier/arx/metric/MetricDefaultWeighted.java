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

package org.deidentifier.arx.metric;

import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * This class provides an abstract skeleton for the implementation of weighted metrics.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public abstract class MetricDefaultWeighted extends MetricDefault {

    private static final long         serialVersionUID = 6508220940790010968L;
    /** The user defined weight map, indexed by column name */
    private final Map<String, Double> definitionWeights;
    /** The weights */
    protected double[]                weights;

    public MetricDefaultWeighted(final boolean monotonic, final boolean independent, final Map<String, Double> definitionWeights) {
        super(monotonic, independent);
        this.definitionWeights = definitionWeights;
    }

    @Override
    protected void initializeInternal(final Data input, final GeneralizationHierarchy[] hierarchies, final ARXConfiguration config) {
        super.initializeInternal(input, hierarchies, config);

        // Initialize weights
        weights = new double[hierarchies.length];
        for (int i = 0; i < hierarchies.length; i++) {
            final String attribute = hierarchies[i].getName();

            if (!definitionWeights.containsKey(attribute)) {
                throw new RuntimeException("No weight defined for hierarchy [" + attribute + "]");
            }

            final double weight = definitionWeights.get(attribute);
            weights[i] = weight;
        }

    }

}
