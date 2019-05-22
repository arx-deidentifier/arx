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

package org.deidentifier.arx.framework.check.history;

import java.util.HashMap;
import java.util.Iterator;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.distribution.Distribution;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.lattice.DependentAction;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;

/**
 * The Class History.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class History {
    
    /**
     * Two types of storage strategies for the history
     * @author Fabian Prasser
     */
    public static enum StorageStrategy {
        ALL,
        NON_ANONYMOUS
    }

    /** The actual buffer. */
    private MRUCache<MRUCacheEntryMetadata> cache                         = null;

    /** Current configuration. */
    private final ARXConfigurationInternal  config;

    /** The dictionary for frequencies of the distributions. */
    private final IntArrayDictionary        dictionarySensFreq;

    /** The dictionary for values of the distributions. */
    private final IntArrayDictionary        dictionarySensValue;

    /** A map from nodes to snapshots. */
    private HashMap<Object, int[]>          nodeToSnapshot                = null;

    /** The current requirements. */
    private final int                       requirements;

    /** The node backing the last returned snapshot. */
    private MRUCacheEntryMetadata           resultMetadata;

    /** Maximal number of entries. */
    private int                             size;

    /** The snapshotSizeDataset for the size of entries. */
    private final long                      snapshotSizeDataset;

    /** The snapshotSizeDataset for the minimum required reduction of a snapshot. */
    private final double                    snapshotSizeSnapshot;

    /** The solution space */
    private final SolutionSpace<?>             solutionSpace;

    /** Store the results of all types of transformations. */
    private final DependentAction STORAGE_TRIGGER_ALL = new DependentAction(){
        @Override
        public boolean appliesTo(Transformation<?> node) {
            return true;
        }
    };

    /** Store only the results of non-anonymous transformations. */
    private final DependentAction STORAGE_TRIGGER_NON_ANONYMOUS = new DependentAction(){
        @Override
        public boolean appliesTo(Transformation<?> node) {
            return node.hasProperty(solutionSpace.getPropertyNotAnonymous());
        }
    };

    /** The current storage strategy. */
    private DependentAction                      storageTrigger;

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
     * @param solutionSpace
     */
    public History(final int rowCount,
                   final int size,
                   final double snapshotSizeDataset,
                   final double snapshotSizeSnapshot,
                   final ARXConfigurationInternal config,
                   final IntArrayDictionary dictionarySensValue,
                   final IntArrayDictionary dictionarySensFreq,
                   final SolutionSpace<?> solutionSpace) {
        
        this.snapshotSizeDataset = (long) (rowCount * snapshotSizeDataset);
        this.snapshotSizeSnapshot = snapshotSizeSnapshot;
        this.cache = new MRUCache<MRUCacheEntryMetadata>(size);
        this.nodeToSnapshot = new HashMap<Object, int[]>(size);
        this.size = size;
        this.dictionarySensFreq = dictionarySensFreq;
        this.dictionarySensValue = dictionarySensValue;
        this.config = config;
        this.requirements = config.getRequirements();
        this.storageTrigger = STORAGE_TRIGGER_NON_ANONYMOUS;
        this.solutionSpace = solutionSpace;
    }
    
    /**
     * Retrieves a snapshot.
     * 
     * @param transformation
     * @return snapshot
     */
    public int[] get(final int[] transformation) {

        // Init
        int[] resultSnapshot = null;
        MRUCacheEntryMetadata resultMetadata = null;
        int level = solutionSpace.getLevel(transformation);

        // Search
        MRUCacheEntry<MRUCacheEntryMetadata> entry = cache.getHead();
        while (entry != null) {
            MRUCacheEntryMetadata currentMetadata = entry.data;
            if (currentMetadata.level < level) {
                final int[] currentSnapshot = nodeToSnapshot.get(currentMetadata.id);
                if ((resultMetadata == null) || (currentSnapshot.length < resultSnapshot.length)) {
                    if (solutionSpace.isParentChildOrEqual(transformation, currentMetadata.transformation)) {
                        resultMetadata = currentMetadata;
                        resultSnapshot = currentSnapshot;
                    }
                }
            }
            entry = entry.next;
        }

        // Manager
        if (resultMetadata != null) {
            cache.touch(resultMetadata);
        }
        this.resultMetadata = resultMetadata;

        // Return
        return resultSnapshot;
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
     * Returns the current storage strategy.
     *
     * @return
     */
    public DependentAction getStorageTrigger() {
        return storageTrigger;
    }

    /**
     * Returns the node backing the last returned snapshot.
     *
     * @return
     */
    public int[] getTransformation() {
        if (resultMetadata == null) {
            return null;
        } else {
            return resultMetadata.transformation;
        }
    }
    
    /**
     * Clears the history.
     */
    public void reset() {
        this.cache.clear();
        this.nodeToSnapshot.clear();
        this.dictionarySensFreq.clear();
        this.dictionarySensValue.clear();
        this.resultMetadata = null;
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
     * Sets the storage strategy
     * @param strategy
     */
    public void setStorageStrategy(StorageStrategy strategy) {
        if (strategy == StorageStrategy.ALL) {
            this.storageTrigger = STORAGE_TRIGGER_ALL;
        } else if (strategy == StorageStrategy.NON_ANONYMOUS) {
            this.storageTrigger = STORAGE_TRIGGER_NON_ANONYMOUS;
        }
    }
    
    /**
     * Stores a snapshot in the buffer.
     *
     * @param transformation The transformation
     * @param groupify The groupify operator
     * @param snapshot The snapshot that was previously used, if any
     * @return
     */
    public boolean store(final Transformation<?> transformation, final HashGroupify groupify, final int[] snapshot) {

        // Early abort if too large, or no space
        if (size == 0 || groupify.getNumberOfEquivalenceClasses() > snapshotSizeDataset) {
            return false;
        }

        // Early abort if too large
        if (snapshot != null) {
            final double relativeSize = (groupify.getNumberOfEquivalenceClasses() / ((double) snapshot.length / config.getSnapshotLength()));
            if (relativeSize > snapshotSizeSnapshot) { return false; }
        }
        
        // Early abort if conditions are not triggered
        if (!transformation.hasProperty(solutionSpace.getPropertyForceSnapshot()) && 
            (transformation.hasProperty(solutionSpace.getPropertySuccessorsPruned()) || !storageTrigger.appliesTo(transformation))) {
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

        // Assign snapshot and keep reference for cache
        nodeToSnapshot.put(transformation.getIdentifier(), data);
        cache.append(new MRUCacheEntryMetadata(transformation));

        // Success
        return true;
    }

    /**
     * Remove pruned entries from the cache.
     */
    private final void cleanUpHistory() {

        final Iterator<MRUCacheEntryMetadata> metadata = cache.iterator();
        while (metadata.hasNext()) {
            final MRUCacheEntryMetadata node = metadata.next();
            if (solutionSpace.hasProperty(node.transformation, solutionSpace.getPropertySuccessorsPruned())) {
                metadata.remove();
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
    private final int[] createSnapshot(final HashGroupify g) {

        final int[] data = new int[g.getNumberOfEquivalenceClasses() * config.getSnapshotLength()];
        int index = 0;
        HashGroupifyEntry m = g.getFirstEquivalenceClass();
        while (m != null) {
            // Store element
            data[index] = m.representative;
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
                    int[][] distributionData = distribution.pack();
                    data[index + 3 + i * 2] = dictionarySensValue.probe(distributionData[0]);
                    data[index + 4 + i * 2] = dictionarySensFreq.probe(distributionData[1]);
                }
                break;
            // TODO: If we only need a distribution, we should get rid of the primary counter
            case ARXConfiguration.REQUIREMENT_COUNTER | ARXConfiguration.REQUIREMENT_DISTRIBUTION:
            case ARXConfiguration.REQUIREMENT_DISTRIBUTION:
                for (int i=0; i<m.distributions.length; i++) {
                    Distribution distribution = m.distributions[i];
                    int[][] distributionData = distribution.pack();
                    data[index + 2 + i * 2] = dictionarySensValue.probe(distributionData[0]);
                    data[index + 3 + i * 2] = dictionarySensFreq.probe(distributionData[1]);
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
     * @param metadata
     */
    private final void removeHistoryEntry(final MRUCacheEntryMetadata metadata) {
        final int[] snapshot = nodeToSnapshot.remove(metadata.id);

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
