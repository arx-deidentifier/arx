/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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
 * This class provides an implementation of the DM* metric (monotonic variant of
 * the Discernability Metric).
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class MetricDMStar extends MetricDefault {

    /** SVUID. */
    private static final long serialVersionUID = -3324788439890959974L;
    
    /** Number of tuples. */
    private double            rowCount         = 0;

    /**
     * Creates a new instance.
     */
    protected MetricDMStar() {
        super(true, true, false);
    }

    @Override
    public InformationLoss<?> createMaxInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount * rowCount);
        }
    }

    @Override
    public InformationLoss<?> createMinInformationLoss() {
        if (rowCount == 0) {
            throw new IllegalStateException("Metric must be initialized first");
        } else {
            return new InformationLossDefault(rowCount);
        }
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Discernibility");
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }

    @Override
    public String toString() {
        return "Monotonic Discernability";
    }

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Transformation node, final HashGroupify g) {

        if (node.getLowerBound() != null) {
            return new InformationLossWithBound<InformationLossDefault>((InformationLossDefault) node.getLowerBound(),
                                                                        (InformationLossDefault) node.getLowerBound());
        }

        double value = 0;
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            if (m.count > 0) {
                value += (double) m.count * (double) m.count;
            }
            m = m.nextOrdered;
        }
        return new InformationLossDefaultWithBound(value, value);
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
        return getInformationLossInternal(node, groupify).getInformationLoss();
    }

    /**
     * Returns the current row count.
     *
     * @return
     */
    protected double getRowCount() {
        return this.rowCount;
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
