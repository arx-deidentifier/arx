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

package org.deidentifier.arx;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.DataStatistics.EquivalenceClassStatistics;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.check.groupify.HashGroupify.GroupStatistics;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.Metric;

/**
 * Encapsulates the results of an execution of the ARX algorithm
 * 
 * @author Prasser, Kohlmayer
 */
public class ARXResult {

    /** The global optimum. */
    private final ARXNode          optimalNode;

    /** Wall clock. */
    private final long             duration;

    /** The node checker. */
    private final INodeChecker     checker;

    /** The registry */
    private final DataRegistry     registry;

    /** The data definition */
    private final DataDefinition   definition;

    /** The lattice */
    private final ARXLattice       lattice;

    /** The data manager */
    private final DataManager      manager;

    /** The suppression string */
    private final String           suppressionString;

    /** Flag regarding the suppression of outliers */
    private final boolean          removeOutliers;

    /** The config */
    private final ARXConfiguration config;

    /** Lock the buffer */
    private DataHandle             bufferLockedBy = null;

    /**
     * Internal constructor for deserialization
     */
    public ARXResult(final DataHandle handle,
                            final DataDefinition definition,
                            final ARXLattice lattice,
                            final boolean removeOutliers,
                            final String suppressionString,
                            final int historySize,
                            final double snapshotSizeSnapshot,
                            final double snapshotSizeDataset,
                            final Metric<?> metric,
                            final ARXConfiguration config,
                            final ARXNode optimum,
                            final long time) {

        // Set registry
        handle.getRegistry().createInputSubset(config);

        // Set optimum in lattice
        lattice.access().setOptimum(optimum);

        // Extract data
        final String[] header = ((DataHandleInput) handle).header;
        final int[][] dataArray = ((DataHandleInput) handle).data;
        final Dictionary dictionary = ((DataHandleInput) handle).dictionary;
        final DataManager manager = new DataManager(header,
                                                    dataArray,
                                                    dictionary,
                                                    handle.getDefinition(),
                                                    config.getCriteria());

        // Update handle
        ((DataHandleInput)handle).update(manager.getDataQI().getArray(), 
                                         manager.getDataSE().getArray(),
                                         manager.getDataIS().getArray());
        
        // Lock handle
        ((DataHandleInput)handle).setLocked(true);
        
        // Initialize
        config.initialize(manager);

        // Initialize the metric
        metric.initialize(manager.getDataQI(), manager.getHierarchies(), config);

        // Create a node checker
        final INodeChecker checker = new NodeChecker(manager,
                                                     metric,
                                                     config,
                                                     historySize,
                                                     snapshotSizeDataset,
                                                     snapshotSizeSnapshot);

        // Initialize the result
        this.registry = handle.getRegistry();
        this.manager = manager;
        this.checker = checker;
        this.definition = definition;
        this.config = config;
        this.lattice = lattice;
        this.optimalNode = lattice.getOptimum();
        this.duration = time;
        this.suppressionString = suppressionString;
        this.removeOutliers = removeOutliers;
    }
    
    
    /**
     * Creates a new instance
     * @param registry
     * @param manager
     * @param checker
     * @param definition
     * @param config
     * @param lattice
     * @param optimalNode
     * @param duration
     * @param suppressionString
     * @param removeOutliers
     */
    protected ARXResult(DataRegistry registry,
                        DataManager manager,
                        INodeChecker checker,
                        DataDefinition definition,
                        ARXConfiguration config,
                        ARXLattice lattice,
                        ARXNode optimalNode,
                        long duration,
                        String suppressionString,
                        boolean removeOutliers) {

        this.registry = registry;
        this.manager = manager;
        this.checker = checker;
        this.definition = definition;
        this.config = config;
        this.lattice = lattice;
        this.optimalNode = optimalNode;
        this.duration = duration;
        this.suppressionString = suppressionString;
        this.removeOutliers = removeOutliers;
    }

    /**
     * Returns the configuration used
     * 
     * @return
     */
    public ARXConfiguration getConfiguration() {
        return config;
    }

    /**
     * Gets the global optimum.
     * 
     * @return the global optimum
     */
    public ARXNode getGlobalOptimum() {
        return optimalNode;
    }

    /**
     * Returns a handle to the data obtained by applying the optimal transformation. This method allows controlling whether
     * the underlying buffer is copied or not. Setting the flag to true will fork the buffer for every handle, allowing to
     * obtain multiple handles to different representations of the data set. When setting the flag to false, all previous
     * handles for output data will be invalidated when a new handle is obtained.
     *  
     * @param fork Set this flag to false, only if you know exactly what you are doing.
     * 
     * @return
     */
    public DataHandle getHandle(boolean fork) {
        if (optimalNode == null) { return null; }
        return getHandle(optimalNode, fork);
    }

    /**
     * Returns a handle to the data obtained by applying the optimal transformation. This method will fork the buffer, 
     * allowing to obtain multiple handles to different representations of the data set. Note that only one instance can
     * be obtained for each transformation.
     * 
     * @return
     */
    public DataHandle getHandle() {
        if (optimalNode == null) { return null; }
        return getHandle(optimalNode, true);
    }
    
    /**
     * Returns a handle to data obtained by applying the given transformation.  This method will fork the buffer, 
     * allowing to obtain multiple handles to different representations of the data set. Note that only one instance can
     * be obtained for each transformation.
     * 
     * @param node the transformation
     * 
     * @return
     */
    public DataHandle getHandle(ARXNode node) {
        return getHandle(node, true);
    }
    
    /**
     * Returns a handle to data obtained by applying the given transformation. This method allows controlling whether
     * the underlying buffer is copied or not. Setting the flag to true will fork the buffer for every handle, allowing to
     * obtain multiple handles to different representations of the data set. When setting the flag to false, all previous
     * handles for output data will be invalidated when a new handle is obtained.
     *  
     * @param node the transformation
     * @param fork Set this flag to false, only if you know exactly what you are doing.
     * 
     * @return
     */
    public DataHandle getHandle(ARXNode node, boolean fork) {
        
        // Check
        if (fork && bufferLockedBy != null) {
            throw new RuntimeException("The buffer is currently locked by another handle");
        }

        // Check
        if (!fork && bufferLockedBy != null) {
            registry.release(bufferLockedBy);
            bufferLockedBy = null;
        }
        
        DataHandle handle = registry.getOutputHandle(node);
        if (handle != null) return handle;

        final Node tNode = new Node(0);
        int level = 0; for (int i : node.getTransformation()) level+= i;
        tNode.setTransformation(node.getTransformation(), level);
 
        // Apply the transformation
        checker.transformAndMarkOutliers(tNode);

        // Store
        if (!node.isChecked()) {
            
            node.access().setChecked(true);
            
            // TODO: Only in this case, due to the special case 
            // with multiple sensitive attributes
            if (definition.getSensitiveAttributes().size()<=1) {
                if (tNode.isAnonymous()) {
                    node.access().setAnonymous();
                } else {
                    node.access().setNotAnonymous();
                }
            }
            node.access().setMaximumInformationLoss(tNode.getInformationLoss());
            node.access().setMinimumInformationLoss(tNode.getInformationLoss());
            lattice.estimateInformationLoss();
        }
        
        // Obtain statistics
        // TODO: Need to obtain statistics for subsets, too
        GroupStatistics[] statistics = checker.getGroupStatistics();
        EquivalenceClassStatistics eqStatistics = new EquivalenceClassStatistics(statistics[0]);
        EquivalenceClassStatistics peqStatistics = new EquivalenceClassStatistics(statistics[1]);
        
        // Clone if needed
        org.deidentifier.arx.framework.data.Data buffer = checker.getBuffer();
        if (!fork) {
            buffer = buffer.clone(); 
        }

        // Create
        DataHandleOutput result = new DataHandleOutput(registry,
                                                       manager,
                                                       checker.getBuffer().clone(),
                                                       node,
                                                       eqStatistics,
                                                       peqStatistics,
                                                       suppressionString,
                                                       definition,
                                                       removeOutliers,
                                                       config);
        
        // Lock
        if (!fork) {
            bufferLockedBy = result; 
        }
        
        // Return
        return result;
    }

    /**
     * Returns the lattice
     * 
     * @return
     */
    public ARXLattice getLattice() {
        return lattice;
    }

    /**
     * Returns the execution time (wall clock)
     * 
     * @return
     */
    public long getTime() {
        return duration;
    }

    /**
     * Indicates if a result is available
     * 
     * @return
     */
    public boolean isResultAvailable() {
        return optimalNode != null;
    }
}
