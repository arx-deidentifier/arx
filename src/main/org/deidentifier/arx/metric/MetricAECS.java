/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.certificate.elements.ElementData;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Transformation;

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
     * Creates a new instance
     */
    protected MetricAECS() {
        super(true, false, false);
    }
    
    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount);
        }
    }
    
    @Override
    public InformationLoss<?> createMinInformationLoss() {
        return new InformationLossDefault(1);
    }
    
    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Average equivalence class size");
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }

    @Override
    public String toString() {
        return "Average Equivalence Class Size";
    }

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Transformation node, final HashGroupify g) {

        // The total number of groups with suppression
        int groupsWithSuppression = 0;
        // The total number of groups without suppression
        int groupsWithoutSuppression = 0;
        // The total number of tuples
        int tuples = 0;
        // Are there suppressed tuples
        boolean suppressed = false;
        
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
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

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        return new InformationLossDefaultWithBound(entry.count, entry.count);
    }

    @Override
    protected InformationLossDefault getLowerBoundInternal(Transformation node) {
        return null;
    }

    @Override
    protected InformationLossDefault getLowerBoundInternal(Transformation node,
                                                           HashGroupify groupify) {
        // The total number of tuples
        int tuples = 0;
        int groups = 0;
        HashGroupifyEntry m = groupify.getFirstEquivalenceClass();
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

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        super.initializeInternal(manager, definition, input, hierarchies, config);
        rowCount = (double)super.getNumRecords(config, input);
    }
}
