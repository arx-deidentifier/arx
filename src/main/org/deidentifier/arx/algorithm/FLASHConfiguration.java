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

package org.deidentifier.arx.algorithm;

import org.deidentifier.arx.framework.lattice.NodeAction;

/**
 * This class parameterizes a phase the interwoven two-phase Flash algorithm.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHConfiguration {

    /**
     * Creates a binary-phase only configuration.
     *
     * @param config
     * @param triggerSnapshotStore
     * @param triggerTagEvent
     * @param pruneDueToLowerBound
     * @return
     */
    public static FLASHConfiguration createBinaryPhaseConfiguration(FLASHPhaseConfiguration config,
                                                                    NodeAction triggerSnapshotStore,
                                                                    NodeAction triggerTagEvent,
                                                                    boolean pruneDueToLowerBound) {
        return new FLASHConfiguration(config, null, triggerSnapshotStore, triggerTagEvent, pruneDueToLowerBound);
    }

    /**
     * Creates a linear-phase only configuration.
     *
     * @param config
     * @param triggerSnapshotStore
     * @param triggerTagEvent
     * @param pruneDueToLowerBound
     * @return
     */
    public static FLASHConfiguration createLinearPhaseConfiguration(FLASHPhaseConfiguration config,
                                                                    NodeAction triggerSnapshotStore,
                                                                    NodeAction triggerTagEvent,
                                                                    boolean pruneDueToLowerBound) {
        return new FLASHConfiguration(null, config, triggerSnapshotStore, triggerTagEvent, pruneDueToLowerBound);
    }

    /**
     * Creates a two-phase configuration.
     *
     * @param binaryPhaseConfiguration
     * @param linearPhaseConfiguration
     * @param triggerSnapshotStore
     * @param triggerTagEvent
     * @param pruneDueToLowerBound
     * @return
     */
    public static FLASHConfiguration createTwoPhaseConfiguration(FLASHPhaseConfiguration binaryPhaseConfiguration,
                                                                 FLASHPhaseConfiguration linearPhaseConfiguration,
                                                                 NodeAction triggerSnapshotStore,
                                                                 NodeAction triggerTagEvent,
                                                                 boolean pruneDueToLowerBound) {
        return new FLASHConfiguration(binaryPhaseConfiguration,
                                      linearPhaseConfiguration,
                                      triggerSnapshotStore,
                                      triggerTagEvent,
                                      pruneDueToLowerBound);
    }

    /** A configuration for the binary phase. */
    private final FLASHPhaseConfiguration binaryPhaseConfiguration;

    /** A configuration for the linear phase. */
    private final FLASHPhaseConfiguration linearPhaseConfiguration;

    /** Prune based on lower bounds from monotonic shares of metrics for information loss. */
    private final boolean                 pruneInsufficientUtility;

    /** A trigger controlling which transformations are snapshotted. */
    private final NodeAction              triggerSnapshotStore;

    /** A trigger firing when a tag event should be triggered. */
    private final NodeAction              triggerTagEvent;

    /**
     * Creates a new configuration for the FLASH algorithm.
     *
     * @param binaryPhaseConfiguration
     * @param linearPhaseConfiguration
     * @param triggerSnapshotStore
     * @param triggerTagEvent
     * @param pruneDueToLowerBound
     */
    private FLASHConfiguration(FLASHPhaseConfiguration binaryPhaseConfiguration,
                               FLASHPhaseConfiguration linearPhaseConfiguration,
                               NodeAction triggerSnapshotStore,
                               NodeAction triggerTagEvent,
                               boolean pruneDueToLowerBound) {
        this.binaryPhaseConfiguration = binaryPhaseConfiguration;
        this.linearPhaseConfiguration = linearPhaseConfiguration;
        this.triggerSnapshotStore = triggerSnapshotStore;
        this.triggerTagEvent = triggerTagEvent;
        this.pruneInsufficientUtility = pruneDueToLowerBound;
    }

    /**
     * Getter.
     *
     * @return
     */
    public FLASHPhaseConfiguration getBinaryPhaseConfiguration() {
        return binaryPhaseConfiguration;
    }

    /**
     * Getter.
     *
     * @return
     */
    public FLASHPhaseConfiguration getLinearPhaseConfiguration() {
        return linearPhaseConfiguration;
    }

    /**
     * Getter: A trigger controlling which transformations are snapshotted.
     *
     * @return
     */
    public NodeAction getTriggerSnapshotStore() {
        return triggerSnapshotStore;
    }

    /**
     * Getter: A trigger firing when a tag event should be triggered on the lattice.
     *
     * @return
     */
    public NodeAction getTriggerTagEvent() {
        return triggerTagEvent;
    }

    /**
     * Is a binary phase required.
     *
     * @return
     */
    public boolean isBinaryPhaseRequired() {
        return binaryPhaseConfiguration != null;
    }

    /**
     * Is a linear phase required.
     *
     * @return
     */
    public boolean isLinearPhaseRequired() {
        return linearPhaseConfiguration != null;
    }

    /**
     * Prune based on lower bounds from monotonic shares of metrics for information loss?.
     *
     * @return
     */
    public boolean isPruneInsufficientUtility() {
        return pruneInsufficientUtility;
    }
}
