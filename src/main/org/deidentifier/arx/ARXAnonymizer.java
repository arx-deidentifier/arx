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

package org.deidentifier.arx;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.deidentifier.arx.algorithm.AbstractAlgorithm;
import org.deidentifier.arx.algorithm.FLASHAlgorithm;
import org.deidentifier.arx.algorithm.FLASHStrategy;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.Lattice;
import org.deidentifier.arx.framework.lattice.LatticeBuilder;
import org.deidentifier.arx.metric.Metric;

/**
 * This class offers several methods to define parameters and execute the ARX
 * algorithm.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ARXAnonymizer {

    /**
     * Temporary result of the ARX algorithm.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    class Result {

        /** The algorithm. */
        final AbstractAlgorithm algorithm;

        /** The checker. */
        final INodeChecker      checker;

        /** The lattice. */
        final Lattice           lattice;

        /** The data manager. */
        final DataManager       manager;

        /** The metric. */
        final Metric<?>         metric;
        
        /** The time. */
        final long              time;  

        /**
         * Creates a new instance.
         *
         * @param metric the metric
         * @param checker the checker
         * @param lattice the lattice
         * @param manager the manager
         * @param algorithm
         * @param time
         */
        Result(final Metric<?> metric,
               final INodeChecker checker,
               final Lattice lattice,
               final DataManager manager,
               final AbstractAlgorithm algorithm,
               final long time) {
            this.metric = metric;
            this.checker = checker;
            this.lattice = lattice;
            this.manager = manager;
            this.algorithm = algorithm;
            this.time = time;
        }

        /**
         * Creates a final result from this temporary result.
         *
         * @param config
         * @param handle
         * @return
         */
		public ARXResult asResult(ARXConfiguration config, DataHandle handle) {

		    // Create lattice
	        final ARXLattice flattice = new ARXLattice(lattice,
	                                                   algorithm.getGlobalOptimum(),
	                                                   manager.getDataQI().getHeader(),
	                                                   config.getInternalConfiguration());

			// Create output handle
	        ((DataHandleInput)handle).setLocked(true);
            return new ARXResult(handle.getRegistry(),
                                 this.manager,
                                 this.checker,
                                 handle.getDefinition(),
                                 config,
                                 flattice,
                                 System.currentTimeMillis() - time);      
		}
    }

    /** History size. */
    private int         historySize          = 200;

    /** The listener, if any. */
    private ARXListener listener             = null;

    /** Snapshot size. */
    private double      snapshotSizeDataset  = 0.2d;

    /** Snapshot size snapshot. */
    private double      snapshotSizeSnapshot = 0.8d;

    /** The maximal number of QIs that can be processed. */
    private int         maxQuasiIdentifiers  = Integer.MAX_VALUE;

    /** The maximal size of the search space that can be processed. */
    private int         maxTransformations   = 200000;


    /**
     * Creates a new anonymizer with the default configuration.
     */
    public ARXAnonymizer() {
        // Empty by design
    }

    /**
     * Creates a new anonymizer with the given configuration.
     * 
     * @param historySize The maximum number of snapshots stored in the buffer [default=200]
     * @param snapshotSizeDataset The maximum relative size of a snapshot compared to the dataset [default=0.2]
     * @param snapshotSizeSnapshot The maximum relative size of a snapshot compared to its predecessor [default=0.8]
     */
    public ARXAnonymizer(final int historySize, final double snapshotSizeDataset, final double snapshotSizeSnapshot) {
        if (historySize<0) 
            throw new RuntimeException("History size must be >=0");
        this.historySize = historySize;
        if (snapshotSizeDataset<=0 || snapshotSizeDataset>=1) 
            throw new RuntimeException("SnapshotSizeDataset must be >0 and <1");
        this.snapshotSizeDataset = snapshotSizeDataset;
        if (snapshotSizeSnapshot<=0 || snapshotSizeSnapshot>=1) 
            throw new RuntimeException("snapshotSizeSnapshot must be >0 and <1");
        this.snapshotSizeSnapshot = snapshotSizeSnapshot;
    }

    /**
     * Performs data anonymization.
     *
     * @param data The data
     * @param config The privacy config
     * @return ARXResult
     * @throws IOException
     */
    public ARXResult anonymize(final Data data, ARXConfiguration config) throws IOException {
        
        if (((DataHandleInput)data.getHandle()).isLocked()){
            throw new RuntimeException("This data handle is locked. Please release it first");
        }
        
        
        if (data.getDefinition().getSensitiveAttributes().size() > 1 && config.isProtectSensitiveAssociations()) {
            throw new UnsupportedOperationException("Currently not supported!");
        }
        
        DataHandle handle = data.getHandle();
        handle.getDefinition().materialize(handle);
        checkBeforeEncoding(handle, config);
        handle.getRegistry().reset();
        handle.getRegistry().createInputSubset(config);

        // Execute
        return anonymizeInternal(handle, handle.getDefinition(), config).asResult(config, handle);
    }
    
    /**
     * Returns the maximum number of snapshots allowed to store in the history.
     * 
     * @return The size
     */
    public int getHistorySize() {
        return historySize;
    }
    
    /**
     * Gets the snapshot size.
     * 
     * @return The maximum size of a snapshot relative to the dataset size
     */
    public double getMaximumSnapshotSizeDataset() {
        return snapshotSizeDataset;
    }

    /**
     * Gets the snapshot size.
     * 
     * @return The maximum size of a snapshot relative to the previous snapshot
     *         size
     */
    public double getMaximumSnapshotSizeSnapshot() {
        return snapshotSizeSnapshot;
    }

    /**
     * Returns the maximal number of quasi-identifiers.
     * @return
     */
    public int getMaxQuasiIdentifiers() {
        return maxQuasiIdentifiers;
    }

    /**
     * Returns the maximal size of the search space.
     *
     * @return
     */
    public int getMaxTransformations() {
        return maxTransformations;
    }

    /**
     * Sets the maximum number of snapshots allowed to store in the history.
     * 
     * @param historySize
     *            The size
     */
    public void setHistorySize(final int historySize) {
        if (historySize < 0) { throw new IllegalArgumentException("Max. number of snapshots must be positive or 0"); }
        this.historySize = historySize;
    }

    /**
     * Sets a listener.
     * 
     * @param listener
     *            the new listener, if any
     */
    public void setListener(final ARXListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the maximum size of a snapshot relative to the dataset size.
     *
     * @param snapshotSize
     */
    public void setMaximumSnapshotSizeDataset(final double snapshotSize) {
        // Perform sanity checks
        if ((snapshotSize <= 0d) || (snapshotSize > 1d)) { throw new IllegalArgumentException("Snapshot size " + snapshotSize + "must be in [0,1]"); }
        snapshotSizeDataset = snapshotSize;
    }

    /**
     * Sets the maximum size of a snapshot relative to the previous snapshot.
     *
     * @param snapshotSizeSnapshot The size
     */
    public void setMaximumSnapshotSizeSnapshot(final double snapshotSizeSnapshot) {
        // Perform sanity checks
        if ((snapshotSizeSnapshot <= 0d) || (snapshotSizeSnapshot > 1d)) { throw new IllegalArgumentException("Snapshot size " + snapshotSizeSnapshot + "must be in [0,1]"); }
        this.snapshotSizeSnapshot = snapshotSizeSnapshot;
    }

    /**
     * Sets the maximal number of quasi-identifiers. Set to Integer.MAX_VALUE to disable the 
     * restriction. By default, the restriction is disabled.
     * @param maxQuasiIdentifiers
     */
    public void setMaxQuasiIdentifiers(int maxQuasiIdentifiers) {
        this.maxQuasiIdentifiers = maxQuasiIdentifiers;
    }

    /**
     * Sets the maximal size of the search space. Set to Integer.MAX_VALUE to disable the 
     * restriction. Default is 200,000.
     * @param maxTransformations
     */
    public void setMaxTransformations(int maxTransformations) {
        this.maxTransformations = maxTransformations;
    }

    /**
     * Performs some sanity checks.
     *
     * @param config
     * @param manager the manager
     */
    private void checkAfterEncoding(final ARXConfiguration config, final DataManager manager) {

        if (config.containsCriterion(KAnonymity.class)){
            KAnonymity c = config.getCriterion(KAnonymity.class);
            if ((c.getK() > manager.getDataQI().getDataLength()) || (c.getK() < 1)) { 
                throw new IllegalArgumentException("Parameter k (" + c.getK() + ") musst be positive and less or equal than the number of rows (" + manager.getDataQI().getDataLength()+")"); 
            }
        }
        if (config.containsCriterion(LDiversity.class)){
            for (LDiversity c : config.getCriteria(LDiversity.class)){
	            if ((c.getL() > manager.getDataQI().getDataLength()) || (c.getL() < 1)) { 
	                throw new IllegalArgumentException("Parameter l (" + c.getL() + ") musst be positive and less or equal than the number of rows (" + manager.getDataQI().getDataLength()+")"); 
	            }
            }
        }
        
        // Check whether all hierarchies are monotonic
        for (final GeneralizationHierarchy hierarchy : manager.getHierarchies()) {
            hierarchy.checkMonotonicity(manager);
        }

        // check min and max sizes
        final int[] hierarchyHeights = manager.getHierachyHeights();
        final int[] minLevels = manager.getMinLevels();
        final int[] maxLevels = manager.getMaxLevels();

        for (int i = 0; i < hierarchyHeights.length; i++) {
            if (minLevels[i] > (hierarchyHeights[i] - 1)) { throw new IllegalArgumentException("Invalid minimum generalization for attribute '" + manager.getHierarchies()[i].getName() + "': " +
                                                                                               minLevels[i] + " > " + (hierarchyHeights[i] - 1)); }
            if (minLevels[i] < 0) { throw new IllegalArgumentException("The minimum generalization for attribute '" + manager.getHierarchies()[i].getName() + "' has to be positive!"); }
            if (maxLevels[i] > (hierarchyHeights[i] - 1)) { throw new IllegalArgumentException("Invalid maximum generalization for attribute '" + manager.getHierarchies()[i].getName() + "': " +
                                                                                               maxLevels[i] + " > " + (hierarchyHeights[i] - 1)); }
            if (maxLevels[i] < minLevels[i]) { throw new IllegalArgumentException("The minimum generalization for attribute '" + manager.getHierarchies()[i].getName() +
                                                                                  "' has to be lower than or requal to the defined maximum!"); }
        }
    }

    /**
     * Performs some sanity checks.
     * 
     * @param handle
     *            the data handle
     * @param config
     *            the configuration
     */
    private void checkBeforeEncoding(final DataHandle handle, final ARXConfiguration config) {


        // Lots of checks
        if (handle == null) { throw new NullPointerException("Data must not be null!"); }
        if (config.containsCriterion(LDiversity.class) ||
            config.containsCriterion(TCloseness.class)){
            if (handle.getDefinition().getSensitiveAttributes().size() == 0) { throw new IllegalArgumentException("You need to specify a sensitive attribute!"); }
        }
        for (String attr : handle.getDefinition().getSensitiveAttributes()){
            boolean found = false;
            for (LDiversity c : config.getCriteria(LDiversity.class)) {
                if (c.getAttribute().equals(attr)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (TCloseness c : config.getCriteria(TCloseness.class)) {
                    if (c.getAttribute().equals(attr)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                throw new IllegalArgumentException("No criterion defined for sensitive attribute: '"+attr+"'!");
            }
        }
        for (LDiversity c : config.getCriteria(LDiversity.class)) {
            if (handle.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("L-Diversity criterion defined for non-sensitive attribute '"+c.getAttribute()+"'!");
            }
        }
        for (TCloseness c : config.getCriteria(TCloseness.class)) {
            if (handle.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("T-Closeness criterion defined for non-sensitive attribute '"+c.getAttribute()+"'!");
            }
        }

        // Check handle
        if (!(handle instanceof DataHandleInput)) { throw new IllegalArgumentException("Invalid data handle provided!"); }

        // Check if all defines are correct
        DataDefinition definition = handle.getDefinition();
        Set<String> attributes = new HashSet<String>();
        for (int i=0; i<handle.getNumColumns(); i++){
            attributes.add(handle.getAttributeName(i));
        }
        for (String attribute : handle.getDefinition().getSensitiveAttributes()){
            if (!attributes.contains(attribute)) {
                throw new IllegalArgumentException("Sensitive attribute '"+attribute+"' is not contained in the dataset");
            }
        }
        for (String attribute : handle.getDefinition().getInsensitiveAttributes()){
            if (!attributes.contains(attribute)) {
                throw new IllegalArgumentException("Insensitive attribute '"+attribute+"' is not contained in the dataset");
            }
        }
        for (String attribute : handle.getDefinition().getIdentifyingAttributes()){
            if (!attributes.contains(attribute)) {
                throw new IllegalArgumentException("Identifying attribute '"+attribute+"' is not contained in the dataset");
            }
        }
        for (String attribute : handle.getDefinition().getQuasiIdentifyingAttributes()){
            if (!attributes.contains(attribute)) {
                throw new IllegalArgumentException("Quasi-identifying attribute '"+attribute+"' is not contained in the dataset");
            }
        }
        
        // Perform sanity checks
        Set<String> qis = definition.getQuasiIdentifyingAttributes();
        if ((config.getMaxOutliers() < 0d) || (config.getMaxOutliers() > 1d)) { throw new IllegalArgumentException("Suppression rate " + config.getMaxOutliers() + "must be in [0, 1]"); }
        if (qis.size() == 0) { throw new IllegalArgumentException("You need to specify at least one quasi-identifier"); }
        if (qis.size() > maxQuasiIdentifiers) { 
            throw new IllegalArgumentException("Too many quasi-identifiers (" + qis.size()+"). This restriction is configurable."); 
        }
        int transformations = 1;
        for (String qi : qis) {
            transformations *= definition.getMaximumGeneralization(qi) - definition.getMinimumGeneralization(qi) + 1;
        }
        if (transformations > maxTransformations) { 
            throw new IllegalArgumentException("Too many transformations in the search space (" + transformations+ "). This restriction is configurable."); 
        }
    }

    /**
     * Prepares the data manager.
     *
     * @param handle the handle
     * @param definition the definition
     * @param config the config
     * @return the data manager
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private DataManager prepareDataManager(final DataHandle handle, final DataDefinition definition, final ARXConfiguration config) throws IOException {

        // Extract data
        final String[] header = ((DataHandleInput) handle).header;
        final int[][] dataArray = ((DataHandleInput) handle).data;
        final Dictionary dictionary = ((DataHandleInput) handle).dictionary;
        final DataManager manager = new DataManager(header, dataArray, dictionary, definition, config.getCriteria());
        return manager;
    }

    /**
     * Reset a previous lattice and run the algorithm .
     *
     * @param handle
     * @param definition
     * @param config
     * @return
     * @throws IOException
     */
    protected Result anonymizeInternal(final DataHandle handle,
                                       final DataDefinition definition,
                                       final ARXConfiguration config) throws IOException {

        // Encode
        final DataManager manager = prepareDataManager(handle, definition, config);
        
        // Attach arrays to data handle
        ((DataHandleInput)handle).update(manager.getDataQI().getArray(), 
                                         manager.getDataSE().getArray(),
                                         manager.getDataIS().getArray());

        // Initialize
        config.initialize(manager);

        // Check
        checkAfterEncoding(config, manager);

        // Build or clean the lattice
        Lattice lattice = new LatticeBuilder(manager.getMaxLevels(), manager.getMinLevels()).build();
        lattice.setListener(listener);

        // Build a node checker
        final INodeChecker checker = new NodeChecker(manager, config.getMetric(), config.getInternalConfiguration(), historySize, snapshotSizeDataset, snapshotSizeSnapshot);

        // Initialize the metric
        config.getMetric().initialize(definition, manager.getDataQI(), manager.getHierarchies(), config);

        // Create an algorithm instance
        FLASHStrategy strategy = new FLASHStrategy(lattice, manager.getHierarchies());
        AbstractAlgorithm algorithm = FLASHAlgorithm.create(lattice, checker, strategy);
        
        // Execute

        final long time = System.currentTimeMillis();
        algorithm.traverse();
        
        // Deactivate history to prevent bugs when sorting data
        checker.getHistory().reset();
        checker.getHistory().setSize(0);
        
        // Return the result
        return new Result(config.getMetric(), checker, lattice, manager, algorithm, time);
    }
}
