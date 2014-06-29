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

import java.util.List;
import java.util.Map;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of a static metric in
 * which information loss is user-defined per generalization level.
 * This metric will respect attribute weights defined in the configuration.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricStatic extends MetricWeighted<InformationLossDefault> {

    private static final long               serialVersionUID = 3778891174824606177L;

    /** The user defined information loss per level, indexed by column name */
    private final Map<String, List<Double>> _infoloss;

    /** The pre-calculated information loss */
    private double[][]                      infoloss;

    /**
     * Constructor
     * @param infoloss
     */
    protected MetricStatic(final Map<String, List<Double>> infoloss) {
        super(true, true);
        _infoloss = infoloss;
    }
    
    @Override
    protected InformationLossDefault evaluateInternal(final Node node, final IHashGroupify g) {

        double value = 0;
        final int[] transformation = node.getTransformation();
        for (int i = 0; i < transformation.length; i++) {
            value += infoloss[i][transformation[i]];
        }
        return new InformationLossDefault(value);
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        
        super.initializeInternal(definition, input, hierarchies, config);

        // Initialize
        infoloss = new double[hierarchies.length][];
        for (int i = 0; i < hierarchies.length; i++) {
            final String attribute = hierarchies[i].getName();

            final List<Double> basicInfoloss = _infoloss.get(attribute);
            if (basicInfoloss == null) {
                throw new RuntimeException("No information loss defined for attribute [" + attribute + "]");
            }
            if (basicInfoloss.size() != hierarchies[i].getHeight()) {
                throw new RuntimeException("Information loss for attribute [" + attribute + "] is not defined on all levels.");
            }

            for (int j = 1; j < basicInfoloss.size(); j++) {
                if (basicInfoloss.get(j) < basicInfoloss.get(j - 1)) {
                    throw new RuntimeException("Information loss is not monotonic for attribute [" + attribute + "]");
                }
            }

            infoloss[i] = new double[basicInfoloss.size()];
            for (int j = 0; j < infoloss[i].length; j++) {
                double weight = weights != null ? weights[i] : 1d;
                infoloss[i][j] = basicInfoloss.get(j) * weight;
            }
        }
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
    public String getName() {
        return "Static";
    }
}
