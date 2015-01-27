/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    /** SVUID. */
    private static final long serialVersionUID = 5911337622032778562L;

    /** The minimum height. */
    private int               minHeight        = -1;

    /** The maximum height. */
    private int               maxHeight        = -1;

    /**
     * Creates a new instance.
     */
    protected MetricHeight() {
        super(true, true);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#createMaxInformationLoss()
     */
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (maxHeight == -1) {
            throw new IllegalStateException("Metric must be intialized first");
        } else {
            return new InformationLossDefault(maxHeight);
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#createMinInformationLoss()
     */
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (minHeight == -1) {
            throw new IllegalStateException("Metric must be intialized first");
        } else {
            return new InformationLossDefault(minHeight);
        }
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Height";
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final IHashGroupify g) {
        int level = node.getLevel();
        return new InformationLossDefaultWithBound(level, level);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node) {
        return new InformationLossDefault(node.getLevel());
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
        return new InformationLossDefault(node.getLevel());
    }

    /**
     * @return the maxHeight
     */
    protected int getMaxHeight() {
        return maxHeight;
    }

    /**
     * @return the minHeight
     */
    protected int getMinHeight() {
        return minHeight;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
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
