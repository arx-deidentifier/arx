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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deidentifier.arx.ARXAnonymizer.Result;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.DistinctLDiversity;
import org.deidentifier.arx.criteria.EntropyLDiversity;
import org.deidentifier.arx.criteria.EqualDistanceTCloseness;
import org.deidentifier.arx.criteria.HierarchicalDistanceTCloseness;
import org.deidentifier.arx.criteria.Inclusion;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.criteria.RecursiveCLDiversity;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.framework.check.NodeChecker;
import org.deidentifier.arx.framework.check.TransformedData;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.Metric;

/**
 * Encapsulates the results of an execution of the ARX algorithm.
 *
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ARXResult {

    /** Lock the buffer. */
    private DataHandle             bufferLockedByHandle = null;

    /** Lock the buffer. */
    private ARXNode                bufferLockedByNode   = null;

    /** The node checker. */
    private final NodeChecker      checker;

    /** The config. */
    private final ARXConfiguration config;

    /** The data definition. */
    private final DataDefinition   definition;

    /** Wall clock. */
    private final long             duration;

    /** The lattice. */
    private final ARXLattice       lattice;

    /** The data manager. */
    private final DataManager      manager;

    /** The global optimum. */
    private final ARXNode          optimalNode;

    /** The registry. */
    private final DataRegistry     registry;

    /** The registry. */
    private final SolutionSpace    solutionSpace;

    /**
     * Internal constructor for deserialization.
     *
     * @param handle
     * @param definition
     * @param lattice
     * @param historySize
     * @param snapshotSizeSnapshot
     * @param snapshotSizeDataset
     * @param metric
     * @param config
     * @param optimum
     * @param time
     * @param solutionSpace
     */
    public ARXResult(final DataHandle handle,
                     final DataDefinition definition,
                     final ARXLattice lattice,
                     final int historySize,
                     final double snapshotSizeSnapshot,
                     final double snapshotSizeDataset,
                     final Metric<?> metric,
                     final ARXConfiguration config,
                     final ARXNode optimum,
                     final long time,
                     final SolutionSpace solutionSpace) {

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
                                                    config.getCriteria(),
                                                    getAggregateFunctions(handle.getDefinition()));

        // Update handle
        ((DataHandleInput)handle).update(manager.getDataGeneralized().getArray(), 
                                         manager.getDataAnalyzed().getArray(),
                                         manager.getDataStatic().getArray());
        
        // Lock handle
        ((DataHandleInput)handle).setLocked(true);
        
        // Initialize
        config.initialize(manager);

        // Initialize the metric
        metric.initialize(manager, definition, manager.getDataGeneralized(), manager.getHierarchies(), config);

        // Create a node checker
        final NodeChecker checker = new NodeChecker(manager,
                                                     metric,
                                                     config.getInternalConfiguration(),
                                                     historySize,
                                                     snapshotSizeDataset,
                                                     snapshotSizeSnapshot,
                                                     solutionSpace);

        // Initialize the result
        this.registry = handle.getRegistry();
        this.manager = manager;
        this.checker = checker;
        this.definition = definition;
        this.config = config;
        this.lattice = lattice;
        this.optimalNode = lattice.getOptimum();
        this.duration = time;
        this.solutionSpace = solutionSpace;
    }
    
    /**
     * Creates a new instance.
     *
     * @param registry
     * @param manager
     * @param checker
     * @param definition
     * @param config
     * @param lattice
     * @param duration
     * @param solutionSpace
     */
    protected ARXResult(DataRegistry registry,
                        DataManager manager,
                        NodeChecker checker,
                        DataDefinition definition,
                        ARXConfiguration config,
                        ARXLattice lattice,
                        long duration,
                        SolutionSpace solutionSpace) {

        this.registry = registry;
        this.manager = manager;
        this.checker = checker;
        this.definition = definition;
        this.config = config;
        this.lattice = lattice;
        this.optimalNode = lattice.getOptimum();
        this.duration = duration;
        this.solutionSpace = solutionSpace;
    }



    /**
     * Returns the configuration used.
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
     * Returns the lattice.
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
            if (bufferLockedByNode == node && !((DataHandleOutput)bufferLockedByHandle).isOptimized()) {
                return bufferLockedByHandle;
            } else {
                registry.release(bufferLockedByHandle);
                bufferLockedByHandle = null;
                bufferLockedByNode = null;
            }
        }
        
        DataHandle handle = registry.getOutputHandle(node);
        if (handle != null) {
            if (!((DataHandleOutput)handle).isOptimized()) {
                return handle;
            } else {
                registry.release(handle);
            }
        }

        // Apply the transformation
        final Transformation transformation = solutionSpace.getTransformation(node.getTransformation());
        TransformedData information = checker.applyTransformation(transformation);
        transformation.setChecked(information.properties);

        // Store
        if (!node.isChecked() || node.getMaximumInformationLoss().compareTo(node.getMinimumInformationLoss()) != 0) {
            
            node.access().setChecked(true);
            if (transformation.hasProperty(solutionSpace.getPropertyAnonymous())) {
                node.access().setAnonymous();
            } else {
                node.access().setNotAnonymous();
            }
            node.access().setMaximumInformationLoss(transformation.getInformationLoss());
            node.access().setMinimumInformationLoss(transformation.getInformationLoss());
            node.access().setLowerBound(transformation.getLowerBound());
            lattice.estimateInformationLoss();
        }
        
        // Clone if needed
        if (fork) {
            information.bufferGeneralized = information.bufferGeneralized.clone(); 
            information.bufferMicroaggregated = information.bufferMicroaggregated.clone(); 
        }

        // Create
        DataHandleOutput result = new DataHandleOutput(this,
                                                       registry,
                                                       manager,
                                                       information.bufferGeneralized,
                                                       information.bufferMicroaggregated,
                                                       node,
                                                       definition,
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
     * Returns the execution time (wall clock).
     *
     * @return
     */
    public long getTime() {
        return duration;
    }

    /**
     * Returns whether local recoding can be applied to the given handle
     * @param handle
     * @return
     */
    public boolean isOptimizable(DataHandle handle) {

        // Check, if output
        if (!(handle instanceof DataHandleOutput)) {
            return false;
        }
        
        // Extract
        DataHandleOutput output = (DataHandleOutput)handle;
        
        // Check, if input matches
        if (output.getInputBuffer() == null || !output.getInputBuffer().equals(this.checker.getInputBuffer())) {
            return false;
        }
        
        // Create a set of supported privacy models
        Set<Class<?>> supportedModels = getOptimizablePrivacyModels();
        for (PrivacyCriterion c : config.getCriteria()) {
            if (!supportedModels.contains(c.getClass())) {
                return false;
            }
        }

        // Check, if there are enough outliers
        int outliers = 0;
        for (int row = 0; row < output.getNumRows(); row++) {
            if (output.isOutlier(row)) {
                outliers++;
            }
        }
        
        // Check minimal group size
        if (config.getMinimalGroupSize() != Integer.MAX_VALUE && outliers < config.getMinimalGroupSize()) {
            return false;
        }
        
        // Check, if there are any outliers
        if (outliers == 0) {
            return false;
        }
        
        // Yes, we probably can do this
        return true;
    }

    /**
     * Indicates if a result is available.
     *
     * @return
     */
    public boolean isResultAvailable() {
        return optimalNode != null;
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @return The number of optimized records
     * @throws RollbackRequiredException 
     */
    public int optimize(DataHandle handle) throws RollbackRequiredException {
        return this.optimize(handle, 0.5d, new ARXListener(){
            @Override
            public void progress(double progress) {
                // Empty by design
            }
        });
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @return The number of optimized records
     * @throws RollbackRequiredException 
     */
    public int optimize(DataHandle handle, double gsFactor) throws RollbackRequiredException {
        return this.optimize(handle, gsFactor, new ARXListener(){
            @Override
            public void progress(double progress) {
                // Empty by design
            }
        });
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param listener 
     * @return The number of optimized records
     */
    public int optimize(DataHandle handle, double gsFactor, ARXListener listener) throws RollbackRequiredException {
        

        if (gsFactor < 0d || gsFactor > 1d) {
            throw new IllegalArgumentException("Generalization/suppression factor must be in [0, 1]");
        }
        
        // Check, if output
        if (!(handle instanceof DataHandleOutput)) {
            throw new IllegalArgumentException("Can only be applied to output data");
        }
        
        // Extract
        DataHandleOutput output = (DataHandleOutput)handle;
        
        // Check, if input matches
        if (output.getInputBuffer() == null || !output.getInputBuffer().equals(this.checker.getInputBuffer())) {
            throw new IllegalArgumentException("This output data is associated to the correct input data");
        }
        
        // Create a set of supported privacy models
        Set<Class<?>> supportedModels = getOptimizablePrivacyModels();
        for (PrivacyCriterion c : config.getCriteria()) {
            if (!supportedModels.contains(c.getClass())) {
                throw new UnsupportedOperationException("This method does currently not supported the model: " + c.getClass().getSimpleName());
            }
        }

        // We are now ready, to go
        // Collect input and row indices
        RowSet rowset = RowSet.create(output.getNumRows());
        for (int row = 0; row < output.getNumRows(); row++) {
            if (output.isOutlier(row)) {
                rowset.add(row);
            }
        }
        
        // Everything that is used from here on, needs to be either
        // (a) state-less, or
        // (b) a fresh copy of the original configuration.

        // We start by creating a projected instance of the configuration
        // - All privacy models will be cloned
        // - Subsets in d-presence will be projected accordingly
        // - Utility measures will be cloned
        ARXConfiguration config = this.config.getSubsetInstance(rowset, gsFactor);

        // In the data definition, only MicroAggregationFunctions maintain a state, but these 
        // are cloned, when cloning the definition
        // TODO: This is probably not necessary, because they are used from the data manager,
        //       which in turn creates a clone by itself
        DataDefinition definition = this.definition.clone();
        
        // Clone the data manager
        DataManager manager = this.manager.getSubsetInstance(rowset);
        
        // Create an anonymizer
        // TODO: It stores some values that should be transferred?
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        anonymizer.setListener(listener);
        
        // Anonymize
        Result result = null;
        try {
            result = anonymizer.anonymize(manager, definition, config);
        } catch (IOException e) {
            // This should not happen at this point in time, as data has already been read from the source
            throw new RuntimeException("Internal error");
        }
        
        // Break, if no solution has been found
        if (result.optimum == null) {
            return 0;
        }
        
        // Else, merge the results back into the given handle
        TransformedData data = result.checker.applyTransformation(result.optimum, output.getOutputBufferMicroaggregated().getDictionary());
        int newIndex = -1;
        int[][] oldGeneralized = output.getOutputBufferGeneralized().getArray();
        int[][] oldMicroaggregated = output.getOutputBufferMicroaggregated().getArray();
        int[][] newGeneralized = data.bufferGeneralized.getArray();
        int[][] newMicroaggregated = data.bufferMicroaggregated.getArray();
        
        try {
            
            int optimized = 0;
            for (int oldIndex = 0; oldIndex < rowset.length(); oldIndex++) {
                if (rowset.contains(oldIndex)) {
                    newIndex++;
                    if (oldGeneralized != null && oldGeneralized.length != 0) {
                        System.arraycopy(newGeneralized[newIndex], 0, oldGeneralized[oldIndex], 0, newGeneralized[newIndex].length);
                        optimized += (newGeneralized[newIndex][0] & Data.OUTLIER_MASK) != 0 ? 0 : 1;
                    }
                    if (oldMicroaggregated != null && oldMicroaggregated.length != 0) {
                        System.arraycopy(newMicroaggregated[newIndex], 0, oldMicroaggregated[oldIndex], 0, newMicroaggregated[newIndex].length);
                    }
                }
            }
            
            // Update data types
            output.updateDataTypes(result.optimum.getGeneralization());
            
            // Mark as optimized
            if (optimized != 0) {
                output.setOptimized(true);
            }
            
            // Return
            return optimized;
            
        // If anything happens in the above block, the operation needs to be rolled back, because
        // the buffer might be in an inconsistent state
        } catch (Exception e) {
            throw new RollbackRequiredException("Handle must be rebuild to ensure privacy", e);
        }
    }
    
    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param maxIterations The maximal number of iterations to perform
     * @param adaptionFactor Is added to the gsFactor when reaching a fixpoint 
     * @param listener 
     * @throws RollbackRequiredException 
     */
    public void optimizeIterative(DataHandle handle,
                                  double gsFactor,
                                  int maxIterations,
                                  double adaptionFactor) throws RollbackRequiredException {
        this.optimizeIterative(handle, gsFactor, maxIterations, adaptionFactor, new ARXListener(){
            @Override
            public void progress(double progress) {
                // Empty by design
            }
        });
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param maxIterations The maximal number of iterations to perform
     * @param adaptionFactor Is added to the gsFactor when reaching a fixpoint 
     * @param listener 
     * @throws RollbackRequiredException 
     */
    public void optimizeIterative(final DataHandle handle,
                                  double gsFactor,
                                  final int maxIterations,
                                  final double adaptionFactor,
                                  final ARXListener listener) throws RollbackRequiredException {
        
        if (gsFactor < 0d || gsFactor > 1d) {
            throw new IllegalArgumentException("Generalization/suppression factor must be in [0, 1]");
        }
        if (adaptionFactor < 0d || adaptionFactor > 1d) {
            throw new IllegalArgumentException("Adaption factor must be in [0, 1]");
        }
        if (maxIterations <= 0) {
            throw new IllegalArgumentException("Max. iterations must be > zero");
        }

        // Outer loop
        int iterations = 0;
        int optimized = Integer.MAX_VALUE;
        double totalAdaption = 0d;
        final double max = maxIterations != Integer.MAX_VALUE ? maxIterations : (1d - gsFactor) / adaptionFactor;
        while (isOptimizable(handle) && iterations < maxIterations && optimized > 0) {

            // Create a wrapped listener
            final double base = maxIterations != Integer.MAX_VALUE ? iterations : totalAdaption / adaptionFactor;
            ARXListener wrapper = new ARXListener() {
                @Override
                public void progress(double progress) {
                    double _max = (max > 1d && !Double.isInfinite(max) && !Double.isNaN(max) ? max : 1d);
                    double _base = (base > 0d && !Double.isInfinite(base) && !Double.isNaN(base)? base : 0d);
                    double value = (progress + _base) / _max;
                    listener.progress(value);
                }
            };

            // Perform individual optimization
            optimized = optimize(handle, gsFactor, wrapper);
            
            // Try to adapt, if possible
            if (optimized == 0 && adaptionFactor > 0d) {
                gsFactor += adaptionFactor;
                totalAdaption += adaptionFactor;
                
                // If valid, try again
                if (gsFactor <= 1d) {
                    optimized = Integer.MAX_VALUE;
                }
            }
            iterations++;
        }
    }
    
    /**
     * Returns a map of all microaggregation functions
     * @param definition
     * @return
     */
    private Map<String, DistributionAggregateFunction> getAggregateFunctions(DataDefinition definition) {
        Map<String, DistributionAggregateFunction> result = new HashMap<String, DistributionAggregateFunction>();
        for (String key : definition.getQuasiIdentifiersWithMicroaggregation()) {
            result.put(key, definition.getMicroAggregationFunction(key).getFunction());
        }
        return result;
    }
    
    /**
     * Returns a set of classes of privacy models that may be optimized
     * @return
     */
    private Set<Class<?>> getOptimizablePrivacyModels() {
        Set<Class<?>> result = new HashSet<Class<?>>();
        result.add(KAnonymity.class);
        result.add(DDisclosurePrivacy.class);
        result.add(DistinctLDiversity.class);
        result.add(RecursiveCLDiversity.class);
        result.add(EntropyLDiversity.class);
        result.add(HierarchicalDistanceTCloseness.class);
        result.add(EqualDistanceTCloseness.class);
        result.add(Inclusion.class);
        return result;
    }
    
    /**
     * Releases the buffer.
     *
     * @param handle
     */
    protected void releaseBuffer(DataHandleOutput handle) {
        if (handle == bufferLockedByHandle) {
            bufferLockedByHandle = null;
            bufferLockedByNode = null;
        }
    }
}
