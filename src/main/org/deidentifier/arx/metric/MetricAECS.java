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
import org.deidentifier.arx.criteria.DPresence;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of the (normalized) average equivalence class size metric.
 * We dont normailze the metric as proposed in the original publication [1], as this would only be possible for k-anonymity.
 * [1] LeFevre K, DeWitt DJ, Ramakrishnan R. Mondrian Multidimensional K-Anonymity. IEEE; 2006:25-25.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricAECS extends MetricDefault {

    /**  TODO */
    private static final long serialVersionUID = -532478849890959974L;

    /** Number of tuples. */
    private double rowCount = 0;
    
    /**
     * 
     */
    protected MetricAECS() {
        super(false, false);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#createMaxInformationLoss()
     */
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount);
        }
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#createMinInformationLoss()
     */
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new InformationLossDefault(1);
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#toString()
     */
    @Override
    public String toString() {
        return "Average Equivalence Class Size";
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.Metric#getInformationLossInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Node node, final IHashGroupify g) {

        // The total number of groups with suppression
        int groupsWithSuppression = 0;
        // The total number of groups without suppression
        int groupsWithoutSuppression = 0;
        // The total number of tuples
        int tuples = 0;
        // Are there suppressed tuples
        boolean suppressed = false;
        
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                tuples += m.count;
                groupsWithSuppression += m.isNotOutlier ? 1 : 0;
                groupsWithoutSuppression++;
                suppressed |= !m.isNotOutlier;
            }
            m = m.nextOrdered;
        }
        
        // If there are suppressed tuples, they form one additional group
        groupsWithSuppression += suppressed ? 1 : 0;
        
        // Compute AECS
        return new InformationLossDefaultWithBound((double)tuples / (double)groupsWithSuppression,
                                               (double)tuples / (double)groupsWithoutSuppression);
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#getLowerBoundInternal(org.deidentifier.arx.framework.lattice.Node, org.deidentifier.arx.framework.check.groupify.IHashGroupify)
     */
    @Override
    protected InformationLossDefault getLowerBoundInternal(Node node,
                                                           IHashGroupify groupify) {
        // The total number of tuples
        int tuples = 0;
        int groups = 0;
        HashGroupifyEntry m = groupify.getFirstEntry();
        while (m != null) {
            if (m.count > 0) {
                tuples += m.count;
                groups++;
            }
            m = m.nextOrdered;
        }
        // Compute AECS
        return new InformationLossDefault((double)tuples / (double)groups);
    }

    /**
     * Returns the row count.
     *
     * @return
     */
    protected double getRowCount() {
        return rowCount;
    }
    
    /* (non-Javadoc)
     * @see org.deidentifier.arx.metric.MetricDefault#initializeInternal(org.deidentifier.arx.DataDefinition, org.deidentifier.arx.framework.data.Data, org.deidentifier.arx.framework.data.GeneralizationHierarchy[], org.deidentifier.arx.ARXConfiguration)
     */
    @Override
    protected void initializeInternal(DataDefinition definition, Data input, GeneralizationHierarchy[] hierarchies, ARXConfiguration config) {
        super.initializeInternal(definition, input, hierarchies, config);
        if (config.containsCriterion(DPresence.class)) {
            Set<DPresence> crits = config.getCriteria(DPresence.class);
            if (crits.size() > 1) { throw new IllegalArgumentException("Only one d-presence criterion supported!"); }
            for (DPresence dPresence : crits) {
                rowCount = dPresence.getSubset().getArray().length;
            }
        } else {
            rowCount = input.getDataLength();
        }
    }
}
