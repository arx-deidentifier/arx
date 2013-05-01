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

import java.util.Map;

import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * A metric that consists of several metrics that are evaluated and combined by
 * applying weights
 * 
 * @author Prasser, Kohlmayer
 */
public class MetricWeighted extends Metric<InformationLossCombined> {

    /**
     * 
     */
    private static final long serialVersionUID = 8922811727606112026L;

    /**
     * Determines whether the metric is independent
     * 
     * @param weights
     * @return
     */
    private static boolean isIndependent(final Map<Metric<?>, Double> weights) {
        boolean independent = true;
        for (final Metric<?> key : weights.keySet()) {
            independent &= key.isMonotonic();
        }
        return independent;
    }

    /**
     * Determines whether the combination is montonic
     * 
     * @param weights
     * @return
     */
    private static boolean isMonotonic(final Map<Metric<?>, Double> weights) {
        boolean monotonic = true;
        for (final Metric<?> key : weights.keySet()) {
            monotonic &= key.isMonotonic();
        }
        return monotonic;
    }

    /** The weights */
    private final Map<Metric<?>, Double> weights;

    /**
     * Creates a new weighted metric
     * 
     * @param main
     */
    public MetricWeighted(final Map<Metric<?>, Double> weights) {
        super(isMonotonic(weights), isIndependent(weights));
        this.weights = weights;
    }

    @Override
    protected InformationLossCombined
            evaluateInternal(final Node node, final IHashGroupify groupify) {

        double value = 0;
        for (final Metric<?> metric : weights.keySet()) {
            value += metric.evaluateInternal(node, groupify).getValue() *
                     weights.get(metric);
        }
        final InformationLossCombined result = new InformationLossCombined(value);
        for (final Metric<?> metric : weights.keySet()) {
            result.setValue(metric, metric.evaluateInternal(node, groupify));
        }
        return result;
    }

    @Override
    protected void
            initializeInternal(final Data input,
                               final GeneralizationHierarchy[] hierarchies) {
        for (final Metric<?> metric : weights.keySet()) {
            metric.initializeInternal(input, hierarchies);
        }
    }

    @Override
    protected InformationLoss maxInternal() {
        final InformationLossCombined max = new InformationLossCombined(InformationLossDefault.MAX.getValue());
        for (final Metric<?> metric : weights.keySet()) {
            max.setValue(metric, metric.max());
        }
        return max;
    }

    @Override
    protected InformationLoss minInternal() {

        final InformationLossCombined min = new InformationLossCombined(InformationLossDefault.MIN.getValue());
        for (final Metric<?> metric : weights.keySet()) {
            min.setValue(metric, metric.min());
        }
        return min;
    }
}
