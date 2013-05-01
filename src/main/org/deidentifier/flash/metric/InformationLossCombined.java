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

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements an abstract base class for information loss defined by
 * combination of different metrics
 * 
 * @author Prasser, Kohlmayer
 */
class InformationLossCombined extends InformationLossDefault {

    /**
     * 
     */
    private static final long                 serialVersionUID = 4571743764464958938L;
    /** The values for all metrics */
    protected Map<Metric<?>, InformationLoss> values           = new HashMap<Metric<?>, InformationLoss>();

    InformationLossCombined(final double value) {
        super(value);
    }

    /**
     * For cloning
     * 
     * @param value
     * @param values
     */
    private InformationLossCombined(final double value,
                                    final Map<Metric<?>, InformationLoss> values) {
        super(value);
        this.values = values;
    }

    @Override
    protected InformationLoss clone() {

        final Map<Metric<?>, InformationLoss> map = new HashMap<Metric<?>, InformationLoss>();
        for (final Metric<?> m : map.keySet()) {
            map.put(m, values.get(m).clone());
        }
        return new InformationLossCombined(getValue(), map);
    }

    /**
     * Returns the information loss value for one of the covered metrics
     * 
     * @param metric
     *            The according metric
     * @return The information loss
     */
    public InformationLoss getValue(final Metric<?> metric) {
        return values.get(metric);
    }

    @Override
    public void max(final InformationLoss other) {
        if (other == null) { return; }
        super.max(other);
        // Cascade
        final InformationLossCombined cother = (InformationLossCombined) other;
        for (final Metric<?> metric : values.keySet()) {
            values.get(metric).max(cother.getValue(metric));
        }
    }

    @Override
    public void min(final InformationLoss other) {
        if (other == null) { return; }
        super.min(other);
        // Cascade
        final InformationLossCombined cother = (InformationLossCombined) other;
        for (final Metric<?> metric : values.keySet()) {
            values.get(metric).min(cother.getValue(metric));
        }
    }

    /**
     * Set the value for a certain metric
     * 
     * @param values
     */
    protected void
            setValue(final Metric<?> metric, final InformationLoss value) {
        values.put(metric, value);
    }
}
