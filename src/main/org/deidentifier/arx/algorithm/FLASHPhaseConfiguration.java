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

package org.deidentifier.arx.algorithm;

import org.deidentifier.arx.framework.lattice.NodeAction;

/**
 * This class parameterizes a phase the interwoven two-phase Flash algorithm.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHPhaseConfiguration {

    /** A trigger for tagging nodes in this phase. */
    private final NodeAction triggerTag;

    /** A trigger for checking nodes in this phase. */
    private final NodeAction triggerCheck;

    /** A trigger for evaluating nodes in this phase. */
    private final NodeAction triggerEvaluate;

    /** A trigger for skipping nodes in this phase. */
    private final NodeAction triggerSkip;

    /** The main anonymity property. */
    private final int        anonymityProperty;

    /**
     * Creates a configuration for an active phase.
     *
     * @param anonymityProperty
     * @param triggerTag
     * @param triggerCheck
     * @param triggerEvaluate
     * @param triggerSkip
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
     * Getter: The main anonymity property.
     *
     * @return
     */
    public int getAnonymityProperty() {
        return anonymityProperty;
    }

    /**
     * Getter: A trigger for checking nodes in this phase.
     *
     * @return
     */
    public NodeAction getTriggerCheck() {
        return triggerCheck;
    }

    /**
     * Getter: A trigger for evaluating nodes in this phase.
     *
     * @return
     */
    public NodeAction getTriggerEvaluate() {
        return triggerEvaluate;
    }

    /**
     * Getter: A trigger for skipping nodes in this phase.
     *
     * @return
     */
    public NodeAction getTriggerSkip() {
        return triggerSkip;
    }

    /**
     * Getter: A trigger for tagging nodes in this phase.
     *
     * @return
     */
    public NodeAction getTriggerTag() {
        return triggerTag;
    }
}
