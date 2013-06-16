/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.metric;

import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides an implementation of the (normalized) average equivalence class size metric.
 * We dont normailze the metric as proposed in the original publications [1], as this would only be possible for k-anonymity.
 * [1] LeFevre K, DeWitt DJ, Ramakrishnan R. Mondrian Multidimensional K-Anonymity. IEEE; 2006:25–25.
 * 
 * @author Prasser, Kohlmayer
 */
public class MetricAECS extends MetricDefault {

    private static final long serialVersionUID = -532478849890959974L;
    private double            numRecordsTotal  = 0d;

    public MetricAECS() {
        super(true, false);
    }

    @Override
    public InformationLossDefault evaluateInternal(final Node node, final IHashGroupify g) {
        final double value = numRecordsTotal / (double) g.size();
        System.out.println(value);
        return new InformationLossDefault(value);
    }

    @Override
    public void initializeInternal(final Data input, final GeneralizationHierarchy[] ahierarchies) {
        numRecordsTotal = input.getDataLength();
    }
}
