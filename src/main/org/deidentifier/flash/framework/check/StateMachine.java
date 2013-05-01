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

import org.deidentifier.flash.framework.check.history.History;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * This class implements a state machine, which determines which optimizations
 * can be applied to the current transition depending on the previous
 * transition.
 * 
 * @author Prasser, Kohlmayer
 */
public class StateMachine {

    /**
     * The resulting transition.
     * 
     * @author Prasser, Kohlmayer
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
     * @author Prasser, Kohlmayer
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
    private Node       lastNode;

    /** The last transition, which has been performed. */
    private Transition lastTransition;

    /** The current snapshot, if any. */
    private int[]      snapshot = null;

    /** The node for the current snapshot */
    private Node       snapshotNode;

    /**
     * Instantiates a new state machine.
     * 
     * @param history
     *            the history
     */
    public StateMachine(final History history) {
        lastNode = null;
        lastTransition = null;
        this.history = history;
    }

    /**
     * Returns the last node
     * 
     * @return the last node, which has been checked for k-anonymity
     */
    public Node getLastNode() {
        return lastNode;
    }

    public Transition getLastTransition() {
        return lastTransition;
    }

    /**
     * Returns the projection. All bits are set for the columns that don't need
     * to be checked
     * 
     * @param currentNode
     *            the current node
     * @return the projection
     */
    private long getProjection(final Node currentNode) {
        long projection = 0L;
        for (int i = 0; i < currentNode.getTransformation().length; i++) {
            if (currentNode.getTransformation()[i] == lastNode.getTransformation()[i]) {
                projection |= 1L << i;
            }
        }
        return projection;
    }

    /**
     * Is a rollup optimization possible.
     * 
     * @param currentNode
     *            the current node
     * @return true, if is possible rollup
     */
    private boolean isPossibleRollup(final Node currentNode) {
        for (int i = 0; i < lastNode.getTransformation().length; i++) {
            if (currentNode.getTransformation()[i] < lastNode.getTransformation()[i]) { return false; }
        }
        return true;

    }

    /**
     * Is a snapshot optimization possible.
     * 
     * @param currentNode
     *            the current node
     * @return true, if is possible snapshot
     */
    private boolean isPossibleSnapshot(final Node currentNode) {
        snapshot = history.get(currentNode);
        snapshotNode = history.getNode();
        if (snapshot != null) { return true; }
        return false;
    }

    /**
     * Is node1 a predecessor of or equal to node2 ?
     * 
     * @param currentNode
     *            the current node
     * @return true, if is possible rollup
     */
    private boolean isPredecessor(final Node node1, final Node node2) {
        for (int i = 0; i < node2.getTransformation().length; i++) {
            if (node1.getTransformation()[i] < node2.getTransformation()[i]) { return false; }
        }
        return true;

    }

    /**
     * Resets the state machine.
     */
    public void reset() {
        lastNode = null;
        lastTransition = null;
    }

    /**
     * Computes the best state transition.
     * 
     * @param currentNode
     *            the current node
     * @return the transition
     */
    public Transition transition(final Node currentNode) {

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
                    result.projection = isPredecessor(snapshotNode, lastNode) ? getProjection(currentNode)
                            : 0L;
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
}
