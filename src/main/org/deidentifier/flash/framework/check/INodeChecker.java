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

package org.deidentifier.flash.framework.check;

import org.deidentifier.flash.framework.Configuration;
import org.deidentifier.flash.framework.check.groupify.IHashGroupify;
import org.deidentifier.flash.framework.data.Data;
import org.deidentifier.flash.framework.lattice.Node;
import org.deidentifier.flash.metric.Metric;

/**
 * This class implements a generic interface for node checkers
 * 
 * @author Prasser, Kohlmayer
 */
public interface INodeChecker {

    /**
     * Returns the information loss for the given state. Negative infinity means
     * not k-anonymous.
     * 
     * @param node
     *            The node to check
     * @return Information loss, null if not k-anonymous
     */
    public abstract void check(final Node node);

    /**
     * Returns the buffer as a Data object
     * 
     * @return
     */
    public abstract Data getBuffer();

    /**
     * Returns the current config
     * 
     * @return
     */
    public abstract Configuration getConfiguration();

    /**
     * Returns the data
     * 
     * @return
     */
    public abstract Data getData();

    /**
     * Returns the number of groups from the previous check
     * 
     * @return
     */
    public abstract int getGroupCount();

    /**
     * Returns the current hash groupify
     * 
     * @return
     */
    public abstract IHashGroupify getGroupify();

    /**
     * Returns the number of outlying groups from the previous check
     * 
     * @return
     */
    public abstract int getGroupOutliersCount();

    @Deprecated
    public abstract double getInformationLoss(final Node node);

    /**
     * Returns the metric used by this checker
     * 
     * @return
     */
    public abstract Metric<?> getMetric();

    /**
     * Returns the number of outliers from the previous check
     * 
     * @return
     */
    public abstract int getTupleOutliersCount();

    /**
     * Returns the data for a given state. Only used for NUMA.
     * 
     * @param node
     *            the node
     * @return the transformed data
     */
    @Deprecated
    public abstract Data transform(final Node node);

    /**
     * Returns the data for a given state and marks outliers
     * 
     * @param node
     * @return
     */
    public Data transformAndMarkOutliers(Node node);
}
