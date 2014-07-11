/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

import java.util.List;
import java.util.PriorityQueue;

import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.history.History.PruningStrategy;
import org.deidentifier.arx.framework.check.history.History.StorageStrategy;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * This class provides a reference implementation of the Binary FLASH Algorithm.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class FLASHAlgorithmBinary extends AbstractFLASHAlgorithm {

    /** The heap. */
    protected final PriorityQueue<Node> pqueue;


    /**
     * Creates a new instance of the FLASH algorithm.
     * 
     * @param lattice
     *            The lattice
     * @param history
     *            The history
     * @param checker
     *            The checker
     * @param strategy
     *            The strategy
     */
    public FLASHAlgorithmBinary(final Lattice lattice, final INodeChecker checker, final FLASHStrategy strategy) {

        super(lattice, checker, strategy);
        this.pqueue = new PriorityQueue<Node>(11, strategy);
        this.history.setPruningStrategy(PruningStrategy.ANONYMOUS);
        this.history.setStorageStrategy(StorageStrategy.NON_ANONYMOUS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deidentifier.ARX.algorithm.AbstractAlgorithm#traverse()
     */
    @Override
    public void traverse() {

        // Init
        pqueue.clear();
        if (!lattice.getBottom().isChecked()){
            checker.check(lattice.getBottom(), true);
            lattice.getBottom().setTagged();
            lattice.decUntaggedCount(lattice.getBottom().getLevel());
            lattice.triggerTagged();
        }
        
        // For each node
        final int length = lattice.getLevels().length;
        for (int i = 0; i < length; i++) {
            Node[] level;
            level = this.sort(i);
            for (final Node node : level) {
                if (!node.isTagged()) {
                    pqueue.add(node);
                    while (!pqueue.isEmpty()) {
                        Node head = pqueue.poll();
                        // if anonymity is unknown
                        if (!head.isTagged()) {
                            findPath(head);
                            head = checkPathBinary(path);
                        }
                    }
                }
            }
        }
        
        if (lattice.getTop().getInformationLoss() == null) {
            if (!lattice.getTop().isChecked()) {
                checker.check(lattice.getTop(), true);
            }
        }
    }

    /**
     * Checks a path binary.
     * 
     * @param path
     *            The path
     */
    protected final Node checkPathBinary(final List<Node> path) {
        int low = 0;
        int high = path.size() - 1;
        Node lastAnonymousNode = null;

        while (low <= high) {

            final int mid = (low + high) >>> 1;
            final Node node = path.get(mid);

            if (!node.isTagged()) {
                checker.check(node);
                lattice.tagAnonymous(node, node.isAnonymous());
                if (!node.isAnonymous()) {
                    for (final Node up : node.getSuccessors()) {
                        if (!up.isTagged()) {
                            pqueue.add(up);
                        }
                    }
                }
            }

            if (node.isAnonymous()) {
                lastAnonymousNode = node;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return lastAnonymousNode;
    }
}
