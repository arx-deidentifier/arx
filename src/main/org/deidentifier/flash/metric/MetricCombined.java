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

import java.util.Set;

import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.data.GeneralizationHierarchy;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * A metric that has a default main metric and several auxiliary metrics that
 * are also evaluated
 * 
 * @author Prasser, Kohlmayer
 */
public class MetricCombined extends Metric<InformationLossCombined> {

    /**
     * 
     */
    private static final long serialVersionUID = 4827641906575335994L;

    /**
     * Determines whether the metric is independent
     * 
     * @param weights
     * @return
     */
    private static boolean isIndependent(final Metric main,
                                         final Set<Metric<?>> metrics) {
        boolean independent = true;
        independent &= main.isIndependent();
        for (final Metric<?> key : metrics) {
            independent &= key.isMonotonic();
        }
        return independent;
    }

    /** The metrics */
    private final Set<Metric<?>> metrics = null;

    /** The main */
    private Metric<?>            main;

    /**
     * Creates a new combined metric
     * 
     * @param main
     */
    public MetricCombined(final Metric<?> main, final Set<Metric<?>> metrics) {
        super(main.isMonotonic(), isIndependent(main, metrics));
    }

    @Override
    protected InformationLossCombined
            evaluateInternal(final Node node, final IHashGroupify groupify) {

        final double value = main.evaluateInternal(node, groupify).getValue();
        final InformationLossCombined result = new InformationLossCombined(value);
        for (final Metric<?> metric : metrics) {
            result.setValue(metric, metric.evaluateInternal(node, groupify));
        }
        return result;
    }

    @Override
    protected void
            initializeInternal(final Data input,
                               final GeneralizationHierarchy[] hierarchies) {
        main.initializeInternal(input, hierarchies);
        for (final Metric<?> metric : metrics) {
            metric.initializeInternal(input, hierarchies);
        }
    }

    @Override
    protected InformationLoss maxInternal() {
        final InformationLossCombined max = new InformationLossCombined(main.max()
                                                                            .getValue());
        for (final Metric<?> metric : metrics) {
            max.setValue(metric, metric.max());
        }
        return max;
    }

    @Override
    protected InformationLoss minInternal() {
        final InformationLossCombined min = new InformationLossCombined(main.min()
                                                                            .getValue());
        for (final Metric<?> metric : metrics) {
            min.setValue(metric, metric.min());
        }
        return min;
    }
}
