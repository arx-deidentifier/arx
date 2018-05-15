/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2018 Fabian Prasser and contributors
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

import org.deidentifier.arx.framework.lattice.DependentAction;

/**
 * This class parameterizes a phase the interwoven two-phase Flash algorithm.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHPhaseConfiguration {

    /** The main anonymity property. */
    public static enum PhaseAnonymityProperty {
        K_ANONYMITY,
        ANONYMITY
    }

    /** A trigger for tagging nodes in this phase. */
    private final DependentAction             triggerTag;

    /** A trigger for checking nodes in this phase. */
    private final DependentAction             triggerCheck;

    /** A trigger for evaluating nodes in this phase. */
    private final DependentAction             triggerEvaluate;

    /** A trigger for skipping nodes in this phase. */
    private final DependentAction             triggerSkip;

    /** The main anonymity property. */
    private final PhaseAnonymityProperty anonymityProperty;

    /**
     * Creates a configuration for an active phase.
     *
     * @param anonymityProperty
     * @param triggerTag
     * @param triggerCheck
     * @param triggerEvaluate
     * @param triggerSkip
     */
    public FLASHPhaseConfiguration(PhaseAnonymityProperty anonymityProperty,
                                   DependentAction triggerTag,
                                   DependentAction triggerCheck,
                                   DependentAction triggerEvaluate,
                                   DependentAction triggerSkip) {
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
    public PhaseAnonymityProperty getAnonymityProperty() {
        return anonymityProperty;
    }

    /**
     * Getter: A trigger for checking nodes in this phase.
     *
     * @return
     */
    public DependentAction getTriggerCheck() {
        return triggerCheck;
    }

    /**
     * Getter: A trigger for evaluating nodes in this phase.
     *
     * @return
     */
    public DependentAction getTriggerEvaluate() {
        return triggerEvaluate;
    }

    /**
     * Getter: A trigger for skipping nodes in this phase.
     *
     * @return
     */
    public DependentAction getTriggerSkip() {
        return triggerSkip;
    }

    /**
     * Getter: A trigger for tagging nodes in this phase.
     *
     * @return
     */
    public DependentAction getTriggerTag() {
        return triggerTag;
    }
}
