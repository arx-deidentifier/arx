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

import org.deidentifier.arx.framework.check.history.History;
import org.deidentifier.arx.framework.lattice.SolutionSpace;

/**
 * This class implements a state machine, which determines which optimizations
 * can be applied to the current transition depending on the previous
 * transition.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class StateMachine {

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
    private History       history  = null;

    /** The last node, which has been checked for k-anonymity. */
    private long          lastNode;

    /** The last transition, which has been performed. */
    private Transition    lastTransition;

    /** The current snapshot, if any. */
    private int[]         snapshot = null;

    /** The node for the current snapshot. */
    private long          snapshotNode;

    /** The solution space */
    private SolutionSpace solutionSpace;

    /**
     * Instantiates a new state machine.
     * @param history
     * @param solutionSpace
     */
    public StateMachine(final History history,
                        final SolutionSpace solutionSpace) {
        this.solutionSpace = solutionSpace;
        this.lastNode = -1;
        this.lastTransition = null;
        this.history = history;
    }

    /**
     * Returns the last node.
     *
     * @return the last node, which has been checked for k-anonymity
     */
    public long getLastNode() {
        return lastNode;
    }

    /**
     * 
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
        lastNode = -1;
        lastTransition = null;
    }

    /**
     * Computes the best state transition.
     * 
     * @param currentNode
     *            the current node
     * @return the transition
     */
    public Transition transition(final long currentNode) {

        final Transition result = new Transition();

        // First transition
        if (lastTransition == null) {
            result.type = TransitionType.UNOPTIMIZED;
            result.projection = 0L;
            result.snapshot = null;
        } else {
            switch (lastTransition.type) {
            case UNOPTIMIZED:
                result.projection = getProjection(currentNode);
                if (isPossibleSnapshot(currentNode)) {
                    result.type = TransitionType.SNAPSHOT;
                    result.snapshot = snapshot;
                } else if (isPossibleRollup(currentNode)) {
                    result.type = TransitionType.ROLLUP;
                    result.snapshot = null;
                } else {
                    result.type = TransitionType.UNOPTIMIZED;
                    result.snapshot = null;
                }
                break;
            case ROLLUP:
            case SNAPSHOT:
                if (isPossibleSnapshot(currentNode)) {
                    result.projection = isPredecessor(snapshotNode, lastNode) ? getProjection(currentNode) : 0L;
                    result.type = TransitionType.SNAPSHOT;
                    result.snapshot = snapshot;
                } else if (isPossibleRollup(currentNode)) {
                    result.projection = getProjection(currentNode);
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
        lastNode = currentNode;
        lastTransition = result;

        // Return
        return result;
    }

    /**
     * Returns the projection. All bits are set for the columns that don't need
     * to be checked
     * 
     * @param currentNode
     *            the current node
     * @return the projection
     */
    private long getProjection(final long currentNode) {
        return solutionSpace.getEqualDimensionsBitMask(currentNode, lastNode);
    }

    /**
     * Is a rollup optimization possible.
     * 
     * @param currentNode
     *            the current node
     * @return true, if is possible rollup
     */
    private boolean isPossibleRollup(final long currentNode) {
        return solutionSpace.isParentChildOrEqual(currentNode, lastNode);
    }

    /**
     * Is a snapshot optimization possible.
     * 
     * @param currentNode
     *            the current node
     * @return true, if is possible snapshot
     */
    private boolean isPossibleSnapshot(final long currentNode) {
        snapshot = history.get(currentNode);
        snapshotNode = history.getTransformation();
        if (snapshot != null) { return true; }
        return false;
    }

    /**
     * Is node2 a predecessor of or equal to node1?.
     *
     * @param successor
     * @param predecessor
     * @return
     */
    private boolean isPredecessor(final long successor, final long predecessor) {
        return solutionSpace.isParentChildOrEqual(successor, predecessor);
    }
}
