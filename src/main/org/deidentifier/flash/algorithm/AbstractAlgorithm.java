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

package org.deidentifier.flash.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.deidentifier.flash.FLASHListener;
import org.deidentifier.flash.framework.check.INodeChecker;
import org.deidentifier.flash.framework.lattice.Lattice;
import org.deidentifier.flash.framework.lattice.Node;

/**
 * Abstract class for an algorithm, which provides some generic methods.
 * 
 * @author Prasser, Kohlmayer
 */
public abstract class AbstractAlgorithm {

    /** A node checker. */
    protected INodeChecker  checker  = null;

    /** The lattice. */
    protected Lattice       lattice  = null;

    /** A listener */
    protected FLASHListener listener = null;

    /**
     * Walks the lattice.
     * 
     * @param lattice
     *            The lattice
     * @param checker
     *            The checker
     */
    protected AbstractAlgorithm(final Lattice lattice,
                                final INodeChecker checker) {
        this.checker = checker;
        this.lattice = lattice;
    }

    /**
     * Returns a list of all anonymous nodes in the lattice.
     * 
     * @return the all anonymous nodes
     */
    public List<Node> getAllAnonymousNodes() {
        final ArrayList<Node> results = new ArrayList<Node>();
        for (final Node[] level : lattice.getLevels()) {
            for (final Node n : level) {
                if (n.isAnonymous()) {
                    results.add(n);
                }
            }
        }
        return results;
    }

    /**
     * Attaches a listener
     * 
     * @param listener
     */
    public void setListener(final FLASHListener listener) {
        this.listener = listener;
    }

    /**
     * Implement this method in order to provide a new algorithm.
     */
    public abstract void traverse();

}
