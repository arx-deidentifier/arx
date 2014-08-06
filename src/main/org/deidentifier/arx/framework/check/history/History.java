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

package org.deidentifier.arx.framework.check.history;

import java.util.HashMap;
import java.util.Iterator;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.groupify.IHashGroupify;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.framework.lattice.NodeAction;

/**
 * The Class History.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class History {
    
    /**
     * Store only non-anonymous transformations
     */
    public static final NodeAction STORAGE_TRIGGER_NON_ANONYMOUS = new NodeAction(){
        @Override
        public boolean appliesTo(Node node) {
            return node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
        }
    };

    /**
     * Store all transformations
     */
    public static final NodeAction STORAGE_TRIGGER_ALL = new NodeAction(){
        @Override
        public boolean appliesTo(Node node) {
            return true;
        }
    };

    /**
     * Evict transformations for which all successors are anonymous
     */
    public static final NodeAction EVICTION_TRIGGER_ANONYMOUS = new NodeAction(){
        @Override
        public boolean appliesTo(Node node) {
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                    return false;
                }
            }
            return true;
        }
    };

    /**
     * Evict transformations for which all successors have been checked
     */
    public static final NodeAction EVICTION_TRIGGER_CHECKED = new NodeAction(){
        @Override
        public boolean appliesTo(Node node) {
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.hasProperty(Node.PROPERTY_CHECKED)) {
                    return false;
                }
            }
            return true;
        }
    };

    /**
     * Evict transformations for which all successors are k-anonymous
     */
    public static final NodeAction EVICTION_TRIGGER_K_ANONYMOUS = new NodeAction(){
        @Override
        public boolean appliesTo(Node node) {
            for (final Node upNode : node.getSuccessors()) {
                if (!upNode.hasProperty(Node.PROPERTY_K_ANONYMOUS)) {
                    return false;
                }
            }
            return true;
        }
    };

    /** The actual buffer. */
    private MRUCache<Node>           cache          = null;

    /** Current configuration */
    private final ARXConfiguration   config;

    /** The dictionary for frequencies of the distributions */
    private final IntArrayDictionary dictionarySensFreq;

    /** The dictionary for values of the distributions */
    private final IntArrayDictionary dictionarySensValue;

    /** Maximal number of entries. */
    private int                      size;

    /** A map from nodes to snapshots. */
    private HashMap<Node, int[]>     nodeToSnapshot = null;

    /** The current pruning strategy */
    private NodeAction              evictionTrigger;

    /** The current storage strategy */
    private NodeAction              storageTrigger;

    /** The current requirements */
    private final int                requirements;

    /** The node backing the last returned snapshot */
    private Node                     resultNode;

    /** The snapshotSizeDataset for the size of entries. */
    private final long               snapshotSizeDataset;

    /** The snapshotSizeDataset for the minimum required reduction of a snapshot */
    private final double             snapshotSizeSnapshot;

    /**
     * Creates a new history.
     * 
     * @param rowCount
     *            the row count
     * @param size
     *            the max size
     * @param snapshotSizeDataset
     *            the snapshotSizeDataset
     */
    public History(final int rowCount,
                   final int size,
                   final double snapshotSizeDataset,
                   final double snapshotSizeSnapshot,
                   final ARXConfiguration config,
                   final IntArrayDictionary dictionarySensValue,
                   final IntArrayDictionary dictionarySensFreq) {
        
        this.snapshotSizeDataset = (long) (rowCount * snapshotSizeDataset);
        this.snapshotSizeSnapshot = snapshotSizeSnapshot;
        this.cache = new MRUCache<Node>(size);
        this.nodeToSnapshot = new HashMap<Node, int[]>(size);
        this.size = size;
        this.dictionarySensFreq = dictionarySensFreq;
        this.dictionarySensValue = dictionarySensValue;
        this.config = config;
        this.requirements = config.getRequirements();
        this.evictionTrigger = EVICTION_TRIGGER_ANONYMOUS;
        this.storageTrigger = STORAGE_TRIGGER_NON_ANONYMOUS;
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
    
    /**
     * Method needed for benchmarking
     * 
     * @return
     */
    public IntArrayDictionary getDictionarySensFreq() {
        return dictionarySensFreq;
    }

    /**
     * Method needed for benchmarking
     * 
     * @return
     */
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
     * Returns the current pruning strategy
     * 
     * @return
     */
    public NodeAction getEvictionTrigger() {
        return evictionTrigger;
    }

    /**
     * Returns the current storage strategy
     * 
     * @return
     */
    public NodeAction getStorageTrigger() {
        return storageTrigger;
    }

    /**
     * Clears the history.
     */
    public void reset() {
        this.cache.clear();
        this.nodeToSnapshot.clear();
        this.dictionarySensFreq.clear();
        this.dictionarySensValue.clear();
        this.resultNode = null;
    }

    /**
     * Set the pruning strategy
     * 
     * @param strategy
     */
    public void setEvictionTrigger(NodeAction trigger) {
        evictionTrigger = trigger;
    }

    /**
     * Set the storage strategy
     * 
     * @param strategy
     */
    public void setStorageTrigger(NodeAction trigger) {
        storageTrigger = trigger;
    }
    
    /**
     * Sets the size of this history
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
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

        // Early abort if too large
        if (g.size() > snapshotSizeDataset) {
            return false;
        }

        // Early abort if too large
        if (usedSnapshot != null) {
            final double relativeSize = (g.size() / ((double) usedSnapshot.length / config.getSnapshotLength()));
            if (relativeSize > snapshotSizeSnapshot) { return false; }
        }
        
        // Early abort if conditions are not triggered
        if (!node.hasProperty(Node.PROPERTY_FORCE_SNAPSHOT) && 
            (!storageTrigger.appliesTo(node) || evictionTrigger.appliesTo(node))) {
            return false;
        }

        // Create the snapshot
        final int[] data = createSnapshot(g);

        // if cache size is too large purge
        if (cache.size() >= size) {
            purgeCache();
        }

        // assign snapshot and keep reference for cache
        nodeToSnapshot.put(node, data);
        cache.append(node);

        return true;
    }

    /**
     * Creates a generic snapshot for all criteria
     * 
     * @param g
     *            the g
     * @return the int[]
     */
    private final int[] createSnapshot(final IHashGroupify g) {

        final int[] data = new int[g.size() * config.getSnapshotLength()];
        int index = 0;
        HashGroupifyEntry m = g.getFirstEntry();
        while (m != null) {
            // Store element
            data[index] = m.representant;
            data[index + 1] = m.count;
            // Add data for different requirements
            switch (requirements) {
            case ARXConfiguration.REQUIREMENT_COUNTER:
                // do nothing
                break;
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER:
                data[index + 2] = m.pcount;
                break;
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                data[index + 2] = m.pcount;
                for (int i=0; i<m.distributions.length; i++) {
                    Distribution distribution = m.distributions[i];
                    distribution.pack();
                    data[index + 3 + i * 2] = dictionarySensValue.probe(distribution.getPackedElements());
                    data[index + 4 + i * 2] = dictionarySensFreq.probe(distribution.getPackedFrequency());
                }
                break;
            // TODO: If we only need a distribution, we should get rid of the primary counter
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            case ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                for (int i=0; i<m.distributions.length; i++) {
                    Distribution distribution = m.distributions[i];
                    distribution.pack();
                    data[index + 2 + i * 2] = dictionarySensValue.probe(distribution.getPackedElements());
                    data[index + 3 + i * 2] = dictionarySensFreq.probe(distribution.getPackedFrequency());
                }
                break;
            default:
                throw new RuntimeException("Invalid requirements: " + requirements);
            }
            index += config.getSnapshotLength();
            // Next element
            m = m.nextOrdered;
        }
        return data;
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
            if (evictionTrigger.appliesTo(node)) {
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

    /**
     * Removes a snapshot
     * 
     * @param node
     */
    private final void removeHistoryEntry(final Node node) {
        final int[] snapshot = nodeToSnapshot.remove(node);

        switch (requirements) {
        case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_SECONDARY_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            for (int i = 0; i < snapshot.length; i += config.getSnapshotLength()) {
                for (int j = i + 3; j < i + config.getSnapshotLength() - 1; j += 2) {
                    dictionarySensValue.decrementRefCount(snapshot[j]);
                    dictionarySensFreq.decrementRefCount(snapshot[j+1]);
                }
            }
            break;
        // TODO: If we only need a distribution, we should get rid of the primary counter
        case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
        case ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            for (int i = 0; i < snapshot.length; i += config.getSnapshotLength()) {
                for (int j = i + 2; j < i + config.getSnapshotLength() - 1; j += 2) {
                    dictionarySensValue.decrementRefCount(snapshot[j]);
                    dictionarySensFreq.decrementRefCount(snapshot[j+1]);
                }
            }
        }

    }
}
