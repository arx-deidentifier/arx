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

package org.deidentifier.arx.framework.check;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.InformationLoss;
import org.deidentifier.arx.metric.Metric;

/**
 * This interface implements a generic interface for node checkers.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public interface INodeChecker {

    /**
     * The result of a check.
     */
    public static class Result {
        
        /** Overall anonymity. */
        public final boolean anonymous;
        
        /** k-Anonymity sub-criterion. */
        public final boolean kAnonymous;
        
        /** Information loss. */
        public final InformationLoss<?> informationLoss;
        
        /** Lower bound. */
        public final InformationLoss<?> lowerBound;
        
        /**
         * Creates a new instance.
         *
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
     * Checks the given node.
     *
     * @param node The node to check
     * @return Result
     */
    public abstract INodeChecker.Result check(final Node node);

    /**
     * Checks the given node.
     *
     * @param node The node to check
     * @param forceMeasureInfoLoss
     * @return Result
     */
    public INodeChecker.Result check(Node node, boolean forceMeasureInfoLoss);

    /**
     * Returns the buffer as a Data object.
     *
     * @return
     */
    public abstract Data getBuffer();

    /**
     * Returns the current config.
     *
     * @return
     */
    public abstract ARXConfigurationInternal getConfiguration();

    /**
     * Returns the data.
     *
     * @return
     */
    public abstract Data getData();

    /**
     * Returns the current hash groupify.
     *
     * @return
     */
    public abstract IHashGroupify getGroupify();


    /**
     * Returns the history, if there is any.
     *
     * @return
     */
    public abstract History getHistory();

    /**
     * 
     *
     * @param node
     * @return
     */
    @Deprecated
    public abstract double getInformationLoss(final Node node);

    /**
     * Returns the metric used by this checker.
     *
     * @return
     */
    public abstract Metric<?> getMetric();

    /**
     * Returns the number of groups from the previous check.
     *
     * @return
     */
    public abstract int getNumberOfGroups();

    /**
     * Applies the given transformation and sets its properties.
     *
     * @param transformation
     * @return
     */
    public TransformedData applyAndSetProperties(Node transformation);
}
