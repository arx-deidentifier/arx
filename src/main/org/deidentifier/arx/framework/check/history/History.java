/*
 * ARX: Efficient, Stable and Optimal Data Anonymization
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

package org.deidentifier.arx.framework.check.history;

import java.util.HashMap;
import java.util.Iterator;

import org.deidentifier.arx.framework.Configuration;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.lattice.Node;

/**
 * The Class History.
 * 
 * @author Prasser, Kohlmayer
 */
public class History {

    /** The actual buffer. */
    private MRUCache<Node>           cache          = null;

    /** Maximal number of entries. */
    private final int                maxSize;

    /** A map from nodes to snapshots. */
    private HashMap<Node, int[]>     nodeToSnapshot = null;

    /** The snapshotSizeDataset for the size of entries. */
    private final long               snapshotSizeDataset;

    /** The snapshotSizeDataset for the minimum required reduction of a snapshot */
    private final double             snapshotSizeSnapshot;

    /** The dictionary for values of the distributions */
    private final IntArrayDictionary dictionarySensValue;

    /** The dictionary for frequencies of the distributions */
    private final IntArrayDictionary dictionarySensFreq;

    /** Current config */
    private final Configuration      config;

    /** The node backing the last returned snapshot */
    private Node                     resultNode;

    /**
     * Creates a new history.
     * 
     * @param rowCount
     *            the row count
     * @param maxSize
     *            the max size
     * @param snapshotSizeDataset
     *            the snapshotSizeDataset
     */
    public History(final int rowCount,
                   final int maxSize,
                   final double snapshotSizeDataset,
                   final double snapshotSizeSnapshot,
                   final Configuration config,
                   final IntArrayDictionary dictionarySensValue,
                   final IntArrayDictionary dictionarySensFreq) {
        this.snapshotSizeDataset = (long) (rowCount * snapshotSizeDataset);
        this.snapshotSizeSnapshot = snapshotSizeSnapshot;
        cache = new MRUCache<Node>(maxSize);
        nodeToSnapshot = new HashMap<Node, int[]>(maxSize);
        this.maxSize = maxSize;
        this.dictionarySensFreq = dictionarySensFreq;
        this.dictionarySensValue = dictionarySensValue;
        this.config = config;
    }

    /**
     * Can a node be pruned.
     * 
     * @param node
     *            the node
     * @return true, if successful
     */
    private final boolean canPrune(final Node node) {
        boolean prune = true;
        switch (config.getHistoryPruning()) {
        case ANONYMOUS:
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.isAnonymous()) {
                    prune = false;
                    break;
                }
            }
            break;
        case CHECKED:
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.isChecked()) {
                    prune = false;
                    break;
                }
            }
            break;
        case K_ANONYMOUS:
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.isKAnonymous()) {
                    prune = false;
                    break;
                }
            }
            break;
        }
        return prune;
    }

    /**
     * Creates the snapshot.
     * 
     * @param g
     *            the g
     * @return the int[]
     */
    private final int[] createSnapshot(final IHashGroupify g) {
        // Copy Groupify
        final int[] data = new int[g.size() * 2];
        int index = 0;
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            // Store element
            data[index] = m.representant;
            data[index + 1] = m.count;
            index += 2;
            // Next element
            m = m.nextOrdered;
        }
        return data;
    }

    /**
     * Creates the snapshot for d presence including the subset count.
     * 
     * @param g
     *            the g
     * @return the int[]
     */
    private final int[] createSnapshotDPresence(final IHashGroupify g) {
        // Copy Groupify
        final int[] data = new int[g.size() * 3];
        int index = 0;
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            // Store element
            data[index] = m.representant;
            data[index + 1] = m.count;
            data[index + 1] = m.pcount;
            index += 3;
            // Next element
            m = m.nextOrdered;
        }
        return data;
    }

    /**
     * Creates the snapshot including the dictionary encoded frequency set
     * 
     * @param g
     * @return
     */
    private final int[] createSnapshotFrequencySet(final IHashGroupify g) {
        // Copy Groupify
        final int[] data = new int[g.size() * 4];
        int index = 0;

        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {

            // Store element
            data[index] = m.representant;
            data[index + 1] = m.count;
            final Distribution fSet = m.distribution;
            fSet.pack();
            data[index + 2] = dictionarySensValue.probe(fSet.getElements());
            data[index + 3] = dictionarySensFreq.probe(fSet.getFrequency());
            index += 4;
            // Next element
            m = m.nextOrdered;
        }
        return data;
    }

    /**
     * Retrieves a snapshot.
     * 
     * @param node
     *            the node
     * @return the int[]
     */
    public int[] get(final Node node) {

        int[] rData = null;
        Node rNode = null;

        // Iterate over nodes with snapshots
        MRUCacheEntry<Node> entry = cache.getFirst();
        while (entry != null) {
            final Node cNode = entry.data;

            if (cNode.getLevel() < node.getLevel()) {
                final int[] cSnapshot = nodeToSnapshot.get(cNode);

                if ((rNode == null) || (cSnapshot.length < rData.length)) {

                    boolean synergetic = true;
                    for (int i = 0; i < cNode.getTransformation().length; i++) {
                        if (node.getTransformation()[i] < cNode.getTransformation()[i]) {
                            synergetic = false;
                            break;
                        }
                    }
                    if (synergetic) {
                        rNode = cNode;
                        rData = cSnapshot;
                    }
                }
            }
            entry = entry.next;
        }

        if (rNode != null) {
            cache.touch(rNode);
        }

        resultNode = rNode;

        return rData;
    }

    public IntArrayDictionary getDictionarySensFreq() {
        return dictionarySensFreq;
    }

    public IntArrayDictionary getDictionarySensValue() {
        return dictionarySensValue;
    }

    /**
     * Returns the node backing the last returned snapshot
     * 
     * @return
     */
    public Node getNode() {
        return resultNode;
    }

    /**
     * Remove least recently used from cache and index.
     */
    private final void purgeCache() {
        int purged = 0;

        // Purge prunable nodes
        final Iterator<Node> it = cache.iterator();
        while (it.hasNext()) {
            final Node node = it.next();
            if (canPrune(node)) {
                purged++;
                it.remove();
                removeHistoryEntry(node);

            }
        }

        // Purge LRU
        if (purged == 0) {
            final Node node = cache.removeHead();
            removeHistoryEntry(node);
        }
    }

    private final void removeHistoryEntry(final Node node) {
        final int[] snapshot = nodeToSnapshot.remove(node);

        // in case of l-diversity/t-closeness clear freqdictionary
        switch (config.getCriterion()) {
        case L_DIVERSITY:
        case T_CLOSENESS:
            for (int i = 0; i < snapshot.length; i += 4) {
                dictionarySensValue.decrementRefCount(snapshot[i + 2]);
                dictionarySensFreq.decrementRefCount(snapshot[i + 3]);
            }
            break;
        default:
            break;
        }
    }

    /**
     * Clears the history.
     */
    public void reset() {
        cache.clear();
        nodeToSnapshot.clear();
    }

    public int size() {
        return cache.size();

    }

    /**
     * Stores a snapshot.
     * 
     * @param node
     *            the node
     * @param g
     *            the g
     */
    public boolean store(final Node node, final IHashGroupify g, final int[] usedSnapshot) {

        if ((node.isAnonymous() || (g.size() > snapshotSizeDataset) || canPrune(node))) { return false; }

        // Store only if significantly smaller
        if (usedSnapshot != null) {
            final double percentSize = (g.size() / ((double) usedSnapshot.length / config.getCriterionSpecificSnapshotLength()));
            if (percentSize > snapshotSizeSnapshot) { return false; }
        }

        // Create the snapshot
        int[] data = null;

        switch (config.getCriterion()) {
        case K_ANONYMITY:
            data = createSnapshot(g);
            break;

        case L_DIVERSITY:
        case T_CLOSENESS:

            data = createSnapshotFrequencySet(g);
            break;

        case D_PRESENCE:
            data = createSnapshotDPresence(g);
            break;

        default:
            throw new UnsupportedOperationException(config.getCriterion() + ": currenty not supported");
        }

        // if cache size is to large purge
        if (cache.size() >= maxSize) {
            purgeCache();
        }

        // assign snapshot and keep reference for cache
        nodeToSnapshot.put(node, data);
        cache.append(node);

        return true;
    }
}
