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

package org.deidentifier.arx;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.deidentifier.arx.ARXAnonymizer.Result;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.framework.check.TransformationApplicator;
import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.TransformedData;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.data.Data;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.DataMatrix;
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

    /** Anonymizer */
    private ARXAnonymizer                   anonymizer;

    /** Lock the buffer. */
    private DataHandle                      bufferLockedByHandle = null;

    /** Lock the buffer. */
    private ARXNode                         bufferLockedByNode   = null;

    /** The output buffer. */
    private final DataMatrix                buffer;

    /** The config. */
    private final ARXConfiguration          config;

    /** The data definition. */
    private final DataDefinition            definition;

    /** Wall clock. */
    private final long                      duration;

    /** The lattice. */
    private final ARXLattice                lattice;

    /** The data manager. */
    private final DataManager               manager;

    /** The global optimum. */
    private final ARXNode                   optimalTransformation;

    /** The registry. */
    private final DataRegistry              registry;

    /** The solution space. */
    private final SolutionSpace             solutionSpace;

    /** Whether the optimum has been found */
    private final boolean                   optimumFound;

    /** Optimization statistics */
    private final ARXProcessStatistics statistics;

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
     * @param solutionSpace
     * @param statistics
     */
    public ARXResult(DataHandle handle,
                     DataDefinition definition,
                     ARXLattice lattice,
                     int historySize,
                     double snapshotSizeSnapshot,
                     double snapshotSizeDataset,
                     Metric<?> metric,
                     ARXConfiguration config,
                     ARXNode optimum,
                     final long time,
                     SolutionSpace solutionSpace,
                     ARXProcessStatistics statistics) {

        // Set registry and definition
        ((DataHandleInput)handle).setDefinition(definition);
        handle.getRegistry().createInputSubset(config);

        // Set optimum in lattice
        lattice.access().setOptimum(optimum);

        // Extract data
        String[] header = ((DataHandleInput) handle).header;
        DataMatrix dataArray = ((DataHandleInput) handle).data;
        Dictionary dictionary = ((DataHandleInput) handle).dictionary;
     
        // Create manager
        DataManager manager = new DataManager(header,
                                              dataArray,
                                              dictionary,
                                              handle.getDefinition(),
                                              getAggregateFunctions(handle.getDefinition()),
                                              config);

        // Update handle
        ((DataHandleInput)handle).update(manager.getDataGeneralized().getArray(), 
                                         manager.getDataAnalyzed().getArray());
        
        // Lock handle
        ((DataHandleInput)handle).setLocked(true);
        
        // Initialize
        config.initialize(manager);

        // Initialize the metric
        metric.initialize(manager, definition, manager.getDataGeneralized(), manager.getHierarchies(), config);

        this.buffer = new DataMatrix(manager.getDataGeneralized().getArray().getNumRows(), 
                                     manager.getDataGeneralized().getArray().getNumColumns());
        
        // Initialize the result
        this.registry = handle.getRegistry();
        this.manager = manager;
        this.definition = definition;
        this.config = config;
        this.lattice = lattice;
        this.optimalTransformation = lattice.getOptimum();
        this.solutionSpace = solutionSpace;
        this.statistics = statistics != null ? statistics : new ARXProcessStatistics(lattice, optimalTransformation, lattice._legacySearchedWithFlash(), time);
        this.optimumFound = this.statistics.isSolutationAvailable() ? this.statistics.getStep(0).isOptimal() : false;
        this.duration = this.statistics.getDuration();
    }
    
    /**
     * Creates a new instance.
     *
     * @param anonymizer
     * @param registry
     * @param manager
     * @param checker
     * @param definition
     * @param config
     * @param lattice
     * @param duration
     * @param solutionSpace
     * @param optimumFound
     */
    protected ARXResult(ARXAnonymizer anonymizer,
                        DataRegistry registry,
                        DataManager manager,
                        TransformationChecker checker,
                        DataDefinition definition,
                        ARXConfiguration config,
                        ARXLattice lattice,
                        long duration,
                        SolutionSpace solutionSpace,
                        boolean optimumFound) {

        this.anonymizer = anonymizer;
        this.registry = registry;
        this.manager = manager;
        this.buffer = checker.getOutputBuffer();
        this.definition = definition;
        this.config = config;
        this.lattice = lattice;
        this.optimalTransformation = lattice.getOptimum();
        this.duration = duration;
        this.solutionSpace = solutionSpace;
        this.optimumFound = optimumFound;
        this.statistics = new ARXProcessStatistics(lattice, optimalTransformation, optimumFound, duration);
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
     * Returns the data definition
     * @return
     */
    public DataDefinition getDataDefinition() {
        return this.definition;
    }

    /**
     * Gets the global optimum.
     * 
     * @return the global optimum
     */
    public ARXNode getGlobalOptimum() {
        return optimalTransformation;
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
     * Returns whether the global optimum has been found
     * @return
     */
    public boolean getOptimumFound() {
        return this.optimumFound;
    }
    
    /**
     * Returns a handle to the data obtained by applying the optimal transformation. This method will fork the buffer, 
     * allowing to obtain multiple handles to different representations of the data set. Note that only one instance can
     * be obtained for each transformation.
     * 
     * @return
     */
    public DataHandle getOutput() {
        if (optimalTransformation == null) { return null; }
        return getOutput(optimalTransformation, true);
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
        TransformationApplicator applicator = new TransformationApplicator(this.manager,
                                                                           this.buffer,
                                                                           this.config.getQualityModel(),
                                                                           this.config.getInternalConfiguration());
        
        TransformedData information = applicator.applyTransformation(transformation);
        transformation.setChecked(information.properties);

        // Store
        if (!node.isChecked() || node.getHighestScore().compareTo(node.getLowestScore()) != 0) {
            
            node.access().setChecked(true);
            if (transformation.hasProperty(solutionSpace.getPropertyAnonymous())) {
                node.access().setAnonymous();
            } else {
                node.access().setNotAnonymous();
            }
            node.access().setHighestScore(transformation.getInformationLoss());
            node.access().setLowestScore(transformation.getInformationLoss());
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
        if (optimalTransformation == null) { return null; }
        return getOutput(optimalTransformation, fork);
    }
    
    /**
     * Internal method, not for external use
     * 
     * @param stream
     * @param transformation
     * @return
     * @throws IOException 
     * @throws ClassNotFoundException 
     */
    public DataHandle getOutput(InputStream stream, ARXNode transformation) throws ClassNotFoundException, IOException {
        
        // Create
        DataHandleOutput result = new DataHandleOutput(this,
                                                       registry,
                                                       manager,
                                                       stream,
                                                       transformation,
                                                       definition,
                                                       config);
        
        // Lock
        bufferLockedByHandle = result; 
        bufferLockedByNode = transformation;
        
        // Return
        return result;
    }

    /**
     * Returns statistics for the anonymization process
     * @return
     */
    public ARXProcessStatistics getProcessStatistics() {
        return this.statistics;
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
        if (output.getInputBuffer() == null || !output.getInputBuffer().equals(this.manager.getDataGeneralized().getArray())) {
            return false;
        }
        
        // Check if optimizable
        for (PrivacyCriterion c : config.getPrivacyModels()) {
            if (!c.isLocalRecodingSupported()) {
                return false;
            }
        }

        // Baseline records
        RowSet baselineRowSet = config.getSubset() == null ? null : config.getSubset().getSet();
        
        // Check, if there are enough outliers
        int outliers = 0;
        for (int row = 0; row < output.getNumRows(); row++) {
            if (output.isOutlier(row) && (baselineRowSet == null || baselineRowSet.contains(row))) {
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
        return optimalTransformation != null;
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @return The number of optimized records
     * @throws RollbackRequiredException 
     */
    public ARXProcessStatistics optimize(DataHandle handle) throws RollbackRequiredException {
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
    public ARXProcessStatistics optimize(DataHandle handle, double gsFactor) throws RollbackRequiredException {
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
     *                 The default value is 0.5, which means that generalization
     *                 and suppression will be treated equally. A factor of 0
     *                 will favor suppression, and a factor of 1 will favor
     *                 generalization. The values in between can be used for
     *                 balancing both methods.
     * @param listener 
     * @return The number of optimized records
     */
    public ARXProcessStatistics optimize(DataHandle handle, double gsFactor, ARXListener listener) throws RollbackRequiredException {
        return optimizeFast(handle, Double.NaN, gsFactor, listener);
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param records A fraction [0,1] of records that need to be optimized.
     * @return The number of optimized records
     */
    public ARXProcessStatistics optimizeFast(DataHandle handle, double records) throws RollbackRequiredException {
        return optimizeFast(handle, records, Double.NaN, new ARXListener(){
            @Override
            public void progress(double progress) {
                // Empty by design
            }
        });
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param records A fraction [0,1] of records that need to be optimized.
     * @param listener 
     * @return The number of optimized records
     */
    public ARXProcessStatistics optimizeFast(DataHandle handle, double records, ARXListener listener) throws RollbackRequiredException {
        return optimizeFast(handle, records, Double.NaN, listener);
    }
    
    
    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param records A fraction [0,1] of records that need to be optimized.
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods.
     * @param listener 
     * @return The number of optimized records
     */
    public ARXProcessStatistics optimizeFast(DataHandle handle,
                                             double records,
                                             double gsFactor,
                                             ARXListener listener) throws RollbackRequiredException {
        
        // Check if null
        if (listener == null) {
            throw new NullPointerException("Listener must not be null");
        }
        
        // Check if null
        if (handle == null) {
            throw new NullPointerException("Handle must not be null");
        }

        // Check bounds
        if (!Double.isNaN(records) && (records <= 0d || records > 1d)) {
            throw new IllegalArgumentException("Number of records to optimize must be in ]0, 1]");
        }
        
        // Check bounds
        if (!Double.isNaN(gsFactor) && (gsFactor < 0d || gsFactor > 1d)) {
            throw new IllegalArgumentException("Generalization/suppression factor must be in [0, 1]");
        }
        
        // Check if output
        if (!(handle instanceof DataHandleOutput)) {
            throw new IllegalArgumentException("Local recoding can only be applied to output data");
        }
        
        // Check if optimizable
        if (!isOptimizable(handle)) {
            return new ARXProcessStatistics();
        }
        
        // Prepare tracking of duration
        long time = System.currentTimeMillis();
        
        // Extract
        DataHandleOutput output = (DataHandleOutput)handle;
        
        // Check, if input matches
        if (output.getInputBuffer() == null || !output.getInputBuffer().equals(this.manager.getDataGeneralized().getArray())) {
            throw new IllegalArgumentException("This output data is not associated to the correct input data");
        }
        
        // Baseline records
        RowSet baselineRowSet = config.getSubset() == null ? null : config.getSubset().getSet();
        int baselineRecords = baselineRowSet == null ? output.getNumRows() : baselineRowSet.size();
        
        // We are now ready to go
        // Collect input and row indices
        int initialRecords = 0;
        RowSet rowset = RowSet.create(output.getNumRows());
        for (int row = 0; row < output.getNumRows(); row++) {
            if (output.isOutlier(row) && (baselineRowSet == null || baselineRowSet.contains(row))) {
                rowset.add(row);
                initialRecords++;
            }
        }
        initialRecords = baselineRecords - initialRecords;
        
        // Everything that is used from here on, needs to be either
        // (a) state-less, or
        // (b) a fresh copy of the original configuration.

        // We start by creating a projected instance of the configuration
        // - All privacy models will be cloned
        // - Subsets will be projected accordingly
        // - Utility measures will be cloned
        ARXConfiguration config = this.config.getInstanceForLocalRecoding(rowset, gsFactor);
        if (!Double.isNaN(records)) {
            double absoluteRecords = records * baselineRecords;
            double relativeRecords = absoluteRecords / (double)rowset.size();
            relativeRecords = relativeRecords < 0d ? 0d : relativeRecords;
            relativeRecords = relativeRecords > 1d ? 1d : relativeRecords;
            config.setSuppressionLimit(1d - relativeRecords);
        }
        
        // In the data definition, only microaggregation functions maintain a state, but these 
        // are cloned, when cloning the definition
        // TODO: This is probably not necessary, because they are used from the data manager,
        //       which in turn creates a clone by itself
        DataDefinition definition = this.definition.clone();
        
        // Clone the data manager
        DataManager manager = this.manager.getSubsetInstance(rowset);
        
        // Create an anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        if (listener != null) {
            anonymizer.setListener(listener);
        }
        if (this.anonymizer != null) {
            anonymizer.parse(this.anonymizer);
        }
        
        // Anonymize
        Result result = null;
        try {
            result = anonymizer.anonymize(manager, definition, config);
        } catch (IOException e) {
            // This should not happen at this point in time, as data has already been read from the source
            throw new RuntimeException("Internal error: unexpected IO issue");
        }
        
        // Break, if no solution has been found
        if (result.optimum == null) {
            return new ARXProcessStatistics();
        }
        
        // Else, merge the results back into the given handle
        TransformedData data = result.checker.getApplicator().applyTransformation(result.optimum, output.getOutputBufferMicroaggregated().getDictionary());
        int newIndex = -1;
        DataMatrix oldGeneralized = output.getOutputBufferGeneralized().getArray();
        DataMatrix oldMicroaggregated = output.getOutputBufferMicroaggregated().getArray();
        DataMatrix newGeneralized = data.bufferGeneralized.getArray();
        DataMatrix newMicroaggregated = data.bufferMicroaggregated.getArray();
        
        try {
            
            int optimized = 0;
            for (int oldIndex = 0; oldIndex < rowset.length(); oldIndex++) {
                if (rowset.contains(oldIndex)) {
                    newIndex++;
                    if (oldGeneralized != null && oldGeneralized.getNumRows() != 0) {
                        oldGeneralized.copyFrom(oldIndex, newGeneralized, newIndex);
                        optimized += (newGeneralized.get(newIndex, 0) & Data.OUTLIER_MASK) != 0 ? 0 : 1;
                    }
                    if (oldMicroaggregated != null && oldMicroaggregated.getNumRows() != 0) {
                        oldMicroaggregated.copyFrom(oldIndex, newMicroaggregated, newIndex);
                    }
                }
            }
            
            // Update data types
            output.updateDataTypes(result.optimum.getGeneralization());
            
            // Mark as optimized
            if (optimized != 0) {
                output.setOptimized(true);
            }
            
            // Done
            time = System.currentTimeMillis() - time;
            return new ARXProcessStatistics(result, initialRecords, optimized, time);
            
        // If anything happens in the above block, the operation needs to be rolled back, because
        // the buffer might be in an inconsistent state
        } catch (Exception e) {
            throw new RollbackRequiredException("Handle must be rebuilt to guarantee privacy", e);
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
     * @throws RollbackRequiredException 
     */
    public ARXProcessStatistics optimizeIterative(DataHandle handle,
                                                  double gsFactor,
                                                  int maxIterations,
                                                  double adaptionFactor) throws RollbackRequiredException {
        
        return this.optimizeIterative(handle, gsFactor, maxIterations, adaptionFactor, new ARXListener(){
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
    public ARXProcessStatistics optimizeIterative(final DataHandle handle,
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
        
        // Prepare 
        int iterationsTotal = 0;
        int optimizedCurrent = Integer.MAX_VALUE;
        int optimizedTotal = 0;
        int optimizedGoal = 0;
        for (int row = 0; row < handle.getNumRows(); row++) {
            optimizedGoal += handle.isOutlier(row) ? 1 : 0;
        }
        
        // Statistics
        ARXProcessStatistics statistics = new ARXProcessStatistics();

        // Progress
        listener.progress(0d);
        
        // Outer loop
        while (isOptimizable(handle) && iterationsTotal < maxIterations && optimizedCurrent > 0) {

            // Perform individual optimization
            ARXProcessStatistics _statistics = optimize(handle, gsFactor);
            optimizedCurrent = 0;
            if (_statistics.isSolutationAvailable()) {
                optimizedCurrent = _statistics.getStep(0).getNumberOfRecordsTransformed();
                statistics = statistics.merge(_statistics);
            }
            optimizedTotal += optimizedCurrent;
            
            // Try to adapt, if possible
            if (optimizedCurrent == 0 && adaptionFactor > 0d) {
                gsFactor += adaptionFactor;
                
                // If valid, try again
                if (gsFactor <= 1d) {
                    optimizedCurrent = Integer.MAX_VALUE;
                }
            }
            iterationsTotal++;

            // Progress
            double progress1 = (double)optimizedTotal / (double)optimizedGoal;
            double progress2 = (double)iterationsTotal / (double)maxIterations;
            listener.progress(Math.max(progress1, progress2));
        }

        // Progress
        listener.progress(1d);
        
        // Done
        return statistics;
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param records A fraction [0,1] of records that need to be optimized in each step.
     * @throws RollbackRequiredException 
     */
    public ARXProcessStatistics optimizeIterativeFast(DataHandle handle,
                                                      double records) throws RollbackRequiredException {
        return this.optimizeIterativeFast(handle, records, Double.NaN, new ARXListener(){
            @Override
            public void progress(double progress) {
                // Empty by design
            }
        });
    }

    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param records A fraction [0,1] of records that need to be optimized in each step.
     * @param listener
     * @throws RollbackRequiredException 
     */
    public ARXProcessStatistics optimizeIterativeFast(DataHandle handle,
                                                      double records,
                                                      ARXListener listener) throws RollbackRequiredException {
        return this.optimizeIterativeFast(handle, records, Double.NaN, listener);
    }
    
    /**
     * This method optimizes the given data output with local recoding to improve its utility
     * @param handle
     * @param records A fraction [0,1] of records that need to be optimized in each step.
     * @param gsFactor A factor [0,1] weighting generalization and suppression.
     *            The default value is 0.5, which means that generalization
     *            and suppression will be treated equally. A factor of 0
     *            will favor suppression, and a factor of 1 will favor
     *            generalization. The values in between can be used for
     *            balancing both methods. 
     * @param listener 
     * @throws RollbackRequiredException 
     */
    public ARXProcessStatistics optimizeIterativeFast(final DataHandle handle,
                                                      double records,
                                                      double gsFactor,
                                                      final ARXListener listener) throws RollbackRequiredException {
        
        if (!Double.isNaN(gsFactor) && (gsFactor < 0d || gsFactor > 1d)) {
            throw new IllegalArgumentException("Generalization/suppression factor must be in [0, 1]");
        }
        if (records < 0d || records > 1d) {
            throw new IllegalArgumentException("Number of records to optimize must be in [0, 1]");
        }

        // Prepare 
        int optimizedCurrent = Integer.MAX_VALUE;
        int optimizedTotal = 0;
        int optimizedGoal = 0;
        for (int row = 0; row < handle.getNumRows(); row++) {
            optimizedGoal += handle.isOutlier(row) ? 1 : 0;
        }
        
        // Statistics
        ARXProcessStatistics statistics = new ARXProcessStatistics();

        // Progress
        listener.progress(0d);
        
        // Outer loop
        while (isOptimizable(handle) && optimizedCurrent > 0) {

            // Progress
            final double minProgress = (double)optimizedTotal / (double)optimizedGoal;
            final double maxProgress = minProgress + records;
            
            // Perform individual optimization
            ARXProcessStatistics _statistics = optimizeFast(handle, records, gsFactor, new ARXListener() {
                @Override
                public void progress(double progress) {
                    listener.progress(minProgress + progress * (maxProgress - minProgress));
                }
            });
            optimizedCurrent = 0;
            if (_statistics.isSolutationAvailable()) {
                optimizedCurrent = _statistics.getStep(0).getNumberOfRecordsTransformed();
                statistics = statistics.merge(_statistics);
            }
            optimizedTotal += optimizedCurrent;
            
            // Progress
            listener.progress((double)optimizedTotal / (double)optimizedGoal);
        }

        // Progress
        listener.progress(1d);
        
        // Done
        return statistics;
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
