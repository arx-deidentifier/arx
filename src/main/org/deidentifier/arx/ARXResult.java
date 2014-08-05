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

package org.deidentifier.arx;

import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.aggregates.StatisticsEquivalenceClasses;
import org.deidentifier.arx.framework.check.INodeChecker;
import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.check.TransformedData;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.lattice.Node;
import org.deidentifier.arx.metric.Metric;

/**
 * Encapsulates the results of an execution of the ARX algorithm
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ARXResult {

    /** Lock the buffer */
    private DataHandle             bufferLockedByHandle = null;

    /** Lock the buffer */
    private ARXNode                bufferLockedByNode   = null;

    /** The node checker. */
    private final INodeChecker     checker;

    /** The config */
    private final ARXConfiguration config;

    /** The data definition */
    private final DataDefinition   definition;

    /** Wall clock. */
    private final long             duration;

    /** The lattice */
    private final ARXLattice       lattice;

    /** The data manager */
    private final DataManager      manager;

    /** The global optimum. */
    private final ARXNode          optimalNode;

    /** The registry */
    private final DataRegistry     registry;

    /** Flag regarding the suppression of outliers */
    private final boolean          removeOutliers;

    /** The suppression string */
    private final String           suppressionString;

    /**
     * Internal constructor for deserialization
     */
    public ARXResult(       final DataHandle handle,
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

        // Set registry and definition
        ((DataHandleInput)handle).setDefinition(definition);
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
        metric.initialize(definition, manager.getDataQI(), manager.getHierarchies(), config);

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
                        long duration,
                        String suppressionString,
                        boolean removeOutliers) {

        this.registry = registry;
        this.manager = manager;
        this.checker = checker;
        this.definition = definition;
        this.config = config;
        this.lattice = lattice;
        this.optimalNode = lattice.getOptimum();
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
     * Returns a handle to the data obtained by applying the optimal transformation. This method will not copy the buffer, 
     * i.e., only one instance can be obtained for each transformation. All previous handles for output data will be invalidated when a new handle is 
     * obtained. Use this only if you know exactly what you are doing.
     * 
     * @return
     */
    @Deprecated
    public DataHandle getHandle() {
        if (optimalNode == null) { return null; }
        return getOutput(optimalNode, false);
    }

    /**
     * Returns a handle to data obtained by applying the given transformation. This method will not copy the buffer, 
     * i.e., only one instance can be obtained for each transformation. All previous handles for output data will be invalidated when a new handle is 
     * obtained. Use this only if you know exactly what you are doing.
     * @param node the transformation
     * 
     * @return
     */
    @Deprecated
    public DataHandle getHandle(ARXNode node) {
        return getOutput(node, false);
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
     * Returns a handle to the data obtained by applying the optimal transformation. This method will fork the buffer, 
     * allowing to obtain multiple handles to different representations of the data set. Note that only one instance can
     * be obtained for each transformation.
     * 
     * @return
     */
    public DataHandle getOutput() {
        if (optimalNode == null) { return null; }
        return getOutput(optimalNode, true);
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
    public DataHandle getOutput(ARXNode node) {
        return getOutput(node, true);
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
    public DataHandle getOutput(ARXNode node, boolean fork) {
        
        // Check lock
        if (fork && bufferLockedByHandle != null) {
            throw new RuntimeException("The buffer is currently locked by another handle");
        }

        // Release lock
        if (!fork && bufferLockedByHandle != null) {
            if (bufferLockedByNode == node) {
                return bufferLockedByHandle;
            } else {
                registry.release(bufferLockedByHandle);
                bufferLockedByHandle = null;
                bufferLockedByNode = null;
            }
        }
        
        DataHandle handle = registry.getOutputHandle(node);
        if (handle != null) return handle;

        final Node transformation = new Node(0);
        int level = 0; for (int i : node.getTransformation()) level+= i;
        transformation.setTransformation(node.getTransformation(), level);
 
        // Apply the transformation
        TransformedData information = checker.applyAndSetProperties(transformation);

        // Store
        if (!node.isChecked()) {
            
            node.access().setChecked(true);
            if (transformation.hasProperty(Node.PROPERTY_ANONYMOUS)) {
                node.access().setAnonymous();
            } else {
                node.access().setNotAnonymous();
            }
            node.access().setMaximumInformationLoss(transformation.getInformationLoss());
            node.access().setMinimumInformationLoss(transformation.getInformationLoss());
            lattice.estimateInformationLoss();
        }
        
        // Clone if needed
        if (fork) {
            information.buffer = information.buffer.clone(); 
        }

        // Create
        DataHandleOutput result = new DataHandleOutput(this,
                                                       registry,
                                                       manager,
                                                       information.buffer,
                                                       node,
                                                       new StatisticsEquivalenceClasses(information.statistics),
                                                       suppressionString,
                                                       definition,
                                                       removeOutliers,
                                                       config);
        
        // Lock
        if (!fork) {
            bufferLockedByHandle = result; 
            bufferLockedByNode = node;
        }
        
        // Return
        return result;
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
    public DataHandle getOutput(boolean fork) {
        if (optimalNode == null) { return null; }
        return getOutput(optimalNode, fork);
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

    /**
     * Releases the buffer
     * @param handle
     */
    protected void releaseBuffer(DataHandleOutput handle) {
        if (handle == bufferLockedByHandle) {
            bufferLockedByHandle = null;
            bufferLockedByNode = null;
        }
    }
}
