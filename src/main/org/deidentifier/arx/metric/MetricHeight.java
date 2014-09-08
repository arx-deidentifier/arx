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

import java.util.Set;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of the Height metric.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricHeight extends MetricDefault {

    /** SVUID */
    private static final long serialVersionUID = 5911337622032778562L;

    /** The minimum height */
    private int               minHeight        = -1;

    /** The maximum height */
    private int               maxHeight        = -1;

    /**
     * Creates a new instance
     */
    protected MetricHeight() {
        super(true, true);
    }

    @Override
    public String toString() {
        return "Height";
    }

    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (minHeight == -1) {
            throw new IllegalStateException("Metric must be intialized first");
        } else {
            return new InformationLossDefault(minHeight);
        }
    }

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (maxHeight == -1) {
            throw new IllegalStateException("Metric must be intialized first");
        } else {
            return new InformationLossDefault(maxHeight);
        }
    }

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final IHashGroupify g) {
        int level = node.getLevel();
        return new InformationLossDefaultWithBound(level, level);
    }

    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node) {
        return new InformationLossDefault(node.getLevel());
    }

    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
        return new InformationLossDefault(node.getLevel());
    }

    @Override
    protected void initializeInternal(final DataDefinition definition,
                                      final Data input,
                                      final GeneralizationHierarchy[] hierarchies,
                                      final ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);

        minHeight = 0;
        maxHeight = 0;
        Set<String> qis = definition.getQuasiIdentifyingAttributes();
        for (String qi : qis) {
            minHeight += definition.getMinimumGeneralization(qi);
            maxHeight += definition.getMaximumGeneralization(qi);
        }
    }
}
