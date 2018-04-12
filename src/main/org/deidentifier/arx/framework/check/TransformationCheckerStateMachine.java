/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.framework.check.history.History;

/**
 * This class implements a state machine, which determines which optimizations
 * can be applied to the current transition depending on the previous
 * transition.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TransformationCheckerStateMachine {

    /**
     * The resulting transition.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static class Transition {

        /** Which columns can be projected away?. */
        public long           projection;

        /** Snapshot, if available. */
        public int[]          snapshot;

        /** The actual type of the transition. */
        public TransitionType type;
    };

    /**
     * The possible transition type.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    public static enum TransitionType {

        /** Apply the roll-up optimization. */
        ROLLUP,

        /** Apply the snapshot optimization. */
        SNAPSHOT,

        /** Unfortunately all rows need to be transformed. */
        UNOPTIMIZED
    }

    /** The history. */
    private History    history  = null;

    /** The last node, which has been checked for k-anonymity. */
    private int[]      lastTransformation;

    /** The last transition, which has been performed. */
    private Transition lastTransition;

    /** The current snapshot, if any. */
    private int[]      snapshot = null;

    /** The node for the current snapshot. */
    private int[]      snapshotTransformation;

    /**
     * Instantiates a new state machine.
     * 
     * @param history the history
     */
    public TransformationCheckerStateMachine(final History history) {
        this.lastTransformation = null;
        this.lastTransition = null;
        this.history = history;
    }

    /**
     * Returns the last transformation.
     *
     * @return the last transformation, which has been checked for k-anonymity
     */
    public int[] getLastTransformation() {
        return lastTransformation;
    }

    /**
     * Returns the last transition.
     *
     * @return
     */
    public Transition getLastTransition() {
        return lastTransition;
    }

    /**
     * Resets the state machine.
     */
    public void reset() {
        lastTransformation = null;
        lastTransition = null;
    }

    /**
     * Calculates the best state transition.
     * 
     * @param transformation the current transformation
     * @return the transition
     */
    public Transition transition(final int[] transformation) {

        // Result
        Transition result = new Transition();

        // First transition
        if (lastTransition == null) {
            result.type = TransitionType.UNOPTIMIZED;
            result.projection = 0L;
            result.snapshot = null;
        } else {
            switch (lastTransition.type) {
            case UNOPTIMIZED:
                result.projection = getProjection(transformation);
                if (isPossibleSnapshot(transformation)) {
                    result.type = TransitionType.SNAPSHOT;
                    result.snapshot = snapshot;
                } else if (isPossibleRollup(transformation)) {
                    result.type = TransitionType.ROLLUP;
                    result.snapshot = null;
                } else {
                    result.type = TransitionType.UNOPTIMIZED;
                    result.snapshot = null;
                }
                break;
            case ROLLUP:
            case SNAPSHOT:
                if (isPossibleSnapshot(transformation)) {
                    result.projection = isPredecessor(snapshotTransformation, lastTransformation) ? getProjection(transformation) : 0L;
                    result.type = TransitionType.SNAPSHOT;
                    result.snapshot = snapshot;
                } else if (isPossibleRollup(transformation)) {
                    result.projection = getProjection(transformation);
                    result.type = TransitionType.ROLLUP;
                    result.snapshot = null;
                } else {
                    result.projection = 0L;
                    result.type = TransitionType.UNOPTIMIZED;
                    result.snapshot = null;
                }
                break;
            }
        }

        // Store
        lastTransformation = transformation;
        lastTransition = result;

        // Return
        return result;
    }

    /**
     * Returns the projection. All bits are set for columns that don't need to be checked.
     * 
     * @param transformation the current transformation
     * @return the projection
     */
    private long getProjection(final int[] transformation) {
        long projection = 0L;
        for (int i = 0; i < transformation.length; i++) {
            if (transformation[i] == lastTransformation[i]) {
                projection |= 1L << i;
            }
        }
        return projection;
    }

    /**
     * Is a rollup optimization possible.
     * 
     * @param transformation the current transformation
     * @return true, if is possible rollup
     */
    private boolean isPossibleRollup(final int[] transformation) {
        for (int i = 0; i < lastTransformation.length; i++) {
            if (transformation[i] < lastTransformation[i]) { return false; }
        }
        return true;
    }

    /**
     * Is a snapshot optimization possible.
     * 
     * @param transformation the current transformation
     * @return true, if is possible snapshot
     */
    private boolean isPossibleSnapshot(final int[] transformation) {
        snapshot = history.get(transformation);
        snapshotTransformation = history.getTransformation();
        if (snapshot != null) { return true; }
        return false;
    }

    /**
     * Is transformation1 a predecessor of or equal to transformation2?.
     *
     * @param transformation1
     * @param transformation2
     * @return
     */
    private boolean isPredecessor(final int[] transformation1, final int[] transformation2) {
        for (int i = 0; i < transformation2.length; i++) {
            if (transformation1[i] < transformation2[i]) { return false; }
        }
        return true;
    }
}
