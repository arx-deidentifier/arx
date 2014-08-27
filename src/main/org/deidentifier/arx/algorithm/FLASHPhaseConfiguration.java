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
 * This class parameterizes a phase the interwoven two-phase Flash algorithm
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 *
 */
public class FLASHPhaseConfiguration {

    /** A trigger for tagging nodes in this phase */
    private final NodeAction triggerTag;

    /** A trigger for checking nodes in this phase */
    private final NodeAction triggerCheck;

    /** A trigger for evaluating nodes in this phase */
    private final NodeAction triggerEvaluate;

    /** A trigger for skipping nodes in this phase */
    private final NodeAction triggerSkip;

    /** The main anonymity property */
    private final int        anonymityProperty;

    /**
     * Creates a configuration for an active phase
     * @param anonymityProperty
     * @param triggerTag
     * @param triggerCheck
     * @param triggerEvaluate
     * @param triggerSkip
     * @param triggerSnapshotStore
     * @param triggerTagEvent
     */
    public FLASHPhaseConfiguration(int anonymityProperty,
                                   NodeAction triggerTag,
                                   NodeAction triggerCheck,
                                   NodeAction triggerEvaluate,
                                   NodeAction triggerSkip) {
        this.anonymityProperty = anonymityProperty;
        this.triggerTag = triggerTag;
        this.triggerCheck = triggerCheck;
        this.triggerEvaluate = triggerEvaluate;
        this.triggerSkip = triggerSkip;
    }

    /**
     * Getter: The main anonymity property
     * @return
     */
    public int getAnonymityProperty() {
        return anonymityProperty;
    }

    /**
     * Getter: A trigger for checking nodes in this phase
     * @return
     */
    public NodeAction getTriggerCheck() {
        return triggerCheck;
    }

    /**
     * Getter: A trigger for evaluating nodes in this phase
     * @return
     */
    public NodeAction getTriggerEvaluate() {
        return triggerEvaluate;
    }

    /**
     * Getter: A trigger for skipping nodes in this phase
     * @return
     */
    public NodeAction getTriggerSkip() {
        return triggerSkip;
    }

    /**
     * Getter: A trigger for tagging nodes in this phase
     * @return
     */
    public NodeAction getTriggerTag() {
        return triggerTag;
    }
}
