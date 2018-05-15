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

import java.util.Set;

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
        super(true, true, true);
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
    public InformationLoss<?> createMinInformationLoss() {
        if (minHeight == -1) {
            throw new IllegalStateException("Metric must be intialized first");
        } else {
            return new InformationLossDefault(minHeight);
        }
    }

    @Override
    public ElementData render(ARXConfiguration config) {
        ElementData result = new ElementData("Height");
        result.addProperty("Monotonic", this.isMonotonic(config.getSuppressionLimit()));
        return result;
    }

    @Override
    public String toString() {
        return "Height";
    }

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(final Transformation node, final HashGroupify g) {
        int level = node.getLevel();
        return new InformationLossDefaultWithBound(level, level);
    }

    @Override
    protected InformationLossWithBound<InformationLossDefault> getInformationLossInternal(Transformation node, HashGroupifyEntry entry) {
        return new InformationLossDefaultWithBound(entry.count, entry.count);
    }

    @Override
    protected InformationLossDefault getLowerBoundInternal(Transformation node) {
        return new InformationLossDefault(node.getLevel());
    }

    @Override
    protected InformationLossDefault getLowerBoundInternal(Transformation node,
                                                           HashGroupify groupify) {
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

    @Override
    protected void initializeInternal(final DataManager manager,
                                      final DataDefinition definition, 
                                      final Data input, 
                                      final GeneralizationHierarchy[] hierarchies, 
                                      final ARXConfiguration config) {
        super.initializeInternal(manager, definition, input, hierarchies, config);

        minHeight = 0;
        maxHeight = 0;
        Set<String> genQis = definition.getQuasiIdentifiersWithGeneralization();
        for (String genQi : genQis) {
            minHeight += definition.getMinimumGeneralization(genQi);
            maxHeight += definition.getMaximumGeneralization(genQi);
        }
    }

}
