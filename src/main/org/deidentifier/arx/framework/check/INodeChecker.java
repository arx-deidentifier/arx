/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
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

package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

/**
 * This interface implements a generic interface for node checkers
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public interface INodeChecker {

    /** The result of a check*/
    public static class Result {
        
        public final InformationLoss<?> lowerBound;
        /** Overall anonymity*/
        public final boolean anonymous;
        /** k-Anonymity sub-criterion*/
        public final boolean kAnonymous;
        /** Information loss*/
        public final InformationLoss<?> informationLoss;
        
        /**
         * Creates a new instance
         * @param anonymous
         * @param kAnonymous
         * @param infoLoss
         * @param lowerBound
         */
        Result(boolean anonymous, boolean kAnonymous, InformationLoss<?> infoLoss, InformationLoss<?> lowerBound) {
            this.anonymous = anonymous;
            this.kAnonymous = kAnonymous;
            this.informationLoss = infoLoss;
            this.lowerBound = lowerBound;
        }
    }

    /**
     * Checks the given node
     * 
     * @param node The node to check
     * @return Result
     */
    public abstract INodeChecker.Result check(final Node node);

    /**
     * Checks the given node
     * 
     * @param node The node to check
     * @param forceMeasureInfoLoss
     * @return Result
     */
    public INodeChecker.Result check(Node node, boolean forceMeasureInfoLoss);

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
    public abstract ARXConfiguration getConfiguration();

    /**
     * Returns the data
     * 
     * @return
     */
    public abstract Data getData();

    /**
     * Returns the current hash groupify
     * 
     * @return
     */
    public abstract IHashGroupify getGroupify();


    /**
     * Returns the history, if there is any
     * @return
     */
    public abstract History getHistory();

    @Deprecated
    public abstract double getInformationLoss(final Node node);

    /**
     * Returns the metric used by this checker
     * 
     * @return
     */
    public abstract Metric<?> getMetric();

    /**
     * Returns the number of groups from the previous check
     * 
     * @return
     */
    public abstract int getNumberOfGroups();

    /**
     * Applies the given transformation and sets its properties
     * 
     * @param transformation
     * @return
     */
    public TransformedData applyAndSetProperties(Node transformation);
}
