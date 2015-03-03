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

package org.deidentifier.arx.framework.check.history;

import java.util.HashMap;
import java.util.Iterator;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
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
    
    /** Store only non-anonymous transformations. */
    public static final NodeAction STORAGE_TRIGGER_NON_ANONYMOUS = new NodeAction(){
        @Override
        public boolean appliesTo(Node node) {
            return node.hasProperty(Node.PROPERTY_NOT_ANONYMOUS);
        }
    };

    /** Store all transformations. */
    public static final NodeAction STORAGE_TRIGGER_ALL = new NodeAction(){
        @Override
        public boolean appliesTo(Node node) {
            return true;
        }
    };

    /** The actual buffer. */
    private MRUCache<Node>                   cache                         = null;

    /** Current configuration. */
    private final ARXConfigurationInternal config;

    /** The dictionary for frequencies of the distributions. */
    private final IntArrayDictionary         dictionarySensFreq;

    /** The dictionary for values of the distributions. */
    private final IntArrayDictionary         dictionarySensValue;

    /** Maximal number of entries. */
    private int                              size;

    /** A map from nodes to snapshots. */
    private HashMap<Node, int[]>             nodeToSnapshot                = null;

    /** The current storage strategy. */
    private NodeAction                       storageTrigger;

    /** The current requirements. */
    private final int                        requirements;

    /** The node backing the last returned snapshot. */
    private Node                             resultNode;

    /** The snapshotSizeDataset for the size of entries. */
    private final long                       snapshotSizeDataset;

    /** The snapshotSizeDataset for the minimum required reduction of a snapshot. */
    private final double                     snapshotSizeSnapshot;

    /**
     * Creates a new history.
     *
     * @param rowCount the row count
     * @param size the max size
     * @param snapshotSizeDataset the snapshotSizeDataset
     * @param snapshotSizeSnapshot
     * @param config
     * @param dictionarySensValue
     * @param dictionarySensFreq
     */
    public History(final int rowCount,
                   final int size,
                   final double snapshotSizeDataset,
                   final double snapshotSizeSnapshot,
                   final ARXConfigurationInternal config,
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
        MRUCacheEntry<Node> entry = cache.getHead();
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
     * Method needed for benchmarking.
     *
     * @return
     */
    public IntArrayDictionary getDictionarySensFreq() {
        return dictionarySensFreq;
    }

    /**
     * Method needed for benchmarking.
     *
     * @return
     */
    public IntArrayDictionary getDictionarySensValue() {
        return dictionarySensValue;
    }

    /**
     * Returns the node backing the last returned snapshot.
     *
     * @return
     */
    public Node getTransformation() {
        return resultNode;
    }
    
    /**
     * Returns the current storage strategy.
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
     * Sets the size of this history.
     *
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * Set the storage strategy.
     *
     * @param trigger
     */
    public void setStorageTrigger(NodeAction trigger) {
        storageTrigger = trigger;
    }

    /**
     * 
     *
     * @return
     */
    public int size() {
        return cache.size();

    }

    /**
     * Stores a snapshot in the buffer.
     *
     * @param transformation The transformation
     * @param groupify The groupify operator
     * @param snapshot The snapshot that was previously used, if any
     * @return
     */
    public boolean store(final Node transformation, final IHashGroupify groupify, final int[] snapshot) {

        // Early abort if too large, or no space
        if (size == 0 || groupify.size() > snapshotSizeDataset) {
            return false;
        }

        // Early abort if too large
        if (snapshot != null) {
            final double relativeSize = (groupify.size() / ((double) snapshot.length / config.getSnapshotLength()));
            if (relativeSize > snapshotSizeSnapshot) { return false; }
        }
        
        // Early abort if conditions are not triggered
        if (!transformation.hasProperty(Node.PROPERTY_FORCE_SNAPSHOT) && 
            (transformation.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED) || !storageTrigger.appliesTo(transformation))) {
            return false;
        }
        
        // Clear the cache
        cleanUpHistory();

        // Perform LRU eviction, if still too large
        if (cache.size() >= size) {
            removeHistoryEntry(cache.removeHead());
        }
        
        // Create the snapshot
        final int[] data = createSnapshot(groupify);


        // assign snapshot and keep reference for cache
        nodeToSnapshot.put(transformation, data);
        cache.append(transformation);

        return true;
    }

    /**
     * Remove pruned entries from the cache.
     */
    private final void cleanUpHistory() {

        final Iterator<Node> it = cache.iterator();
        while (it.hasNext()) {
            final Node node = it.next();
            if (node.hasProperty(Node.PROPERTY_SUCCESSORS_PRUNED)) {
                it.remove();
                removeHistoryEntry(node);
            }
        }
    }
    
    /**
     * Creates a generic snapshot for all criteria.
     *
     * @param g the g
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
     * Removes a snapshot.
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
