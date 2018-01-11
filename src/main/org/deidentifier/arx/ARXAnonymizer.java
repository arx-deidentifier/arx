/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2017 Fabian Prasser, Florian Kohlmayer and contributors
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

import org.deidentifier.arx.AttributeType.MicroAggregationFunction;
import org.deidentifier.arx.algorithm.AbstractAlgorithm;
import org.deidentifier.arx.algorithm.DataDependentEDDPAlgorithm;
import org.deidentifier.arx.algorithm.FLASHAlgorithm;
import org.deidentifier.arx.algorithm.FLASHAlgorithmImpl;
import org.deidentifier.arx.algorithm.FLASHStrategy;
import org.deidentifier.arx.algorithm.LIGHTNINGAlgorithm;
import org.deidentifier.arx.criteria.BasicBLikeness;
import org.deidentifier.arx.criteria.DDisclosurePrivacy;
import org.deidentifier.arx.criteria.EDDifferentialPrivacy;
import org.deidentifier.arx.criteria.EnhancedBLikeness;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.LDiversity;
import org.deidentifier.arx.criteria.TCloseness;
import org.deidentifier.arx.framework.check.TransformationChecker;
import org.deidentifier.arx.framework.check.distribution.DistributionAggregateFunction;
import org.deidentifier.arx.framework.data.DataManager;
import org.deidentifier.arx.framework.data.DataMatrix;
import org.deidentifier.arx.framework.data.Dictionary;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;
import org.deidentifier.arx.framework.lattice.SolutionSpace;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.v2.MetricSDClassification;

/**
 * This class offers several methods to define parameters and execute the ARX
 * algorithm.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class ARXAnonymizer { // NO_UCD

    /**
     * Temporary result of the ARX algorithm.
     * 
     * @author Fabian Prasser
     * @author Florian Kohlmayer
     */
    class Result {

        /** The algorithm. */
        final AbstractAlgorithm     algorithm;

        /** The checker. */
        final TransformationChecker checker;

        /** The solution space. */
        final SolutionSpace         solutionSpace;

        /** The data manager. */
        final DataManager           manager;

        /** The time. */
        final long                  time;

        /** The global optimum */
        final Transformation        optimum;

        /** Whether the optimum has been found */
        final boolean               optimumFound;

        /**
         * Creates a new instance.
         *
         * @param metric the metric
         * @param checker the checker
         * @param lattice the solution space
         * @param manager the manager
         * @param algorithm
         * @param time
         */
        Result(final TransformationChecker checker,
               final SolutionSpace solutionSpace,
               final DataManager manager,
               final AbstractAlgorithm algorithm,
               final long time,
               final boolean optimumFound) {
            this.checker = checker;
            this.solutionSpace = solutionSpace;
            this.manager = manager;
            this.algorithm = algorithm;
            this.time = time;
            this.optimum = algorithm.getGlobalOptimum();
            this.optimumFound = optimumFound;
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
            final ARXLattice lattice = new ARXLattice(solutionSpace,
                                                      (algorithm instanceof FLASHAlgorithmImpl),
                                                      optimum,
                                                      manager.getDataGeneralized().getHeader(),
                                                      config.getInternalConfiguration());

			// Create output handle
	        ((DataHandleInput)handle).setLocked(true);
            return new ARXResult(ARXAnonymizer.this,
                                 handle.getRegistry(),
                                 this.manager,
                                 this.checker,
                                 handle.getDefinition(),
                                 config,
                                 lattice,
                                 System.currentTimeMillis() - time,
                                 solutionSpace,
                                 optimumFound);      
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
        
        // Update registry
        DataHandle handle = data.getHandle();
        handle.getDefinition().materializeHierarchies(handle);
        checkBeforeEncoding(handle, config);
        handle.getRegistry().reset();
        
        // Create manager
        DataManager manager = getDataManager(handle, handle.getDefinition(), config);

        // Attach subset to handle
        handle.getRegistry().createInputSubset(config);
        
        // Attach arrays to data handle
        ((DataHandleInput)handle).update(manager.getDataGeneralized().getArray(), 
                                         manager.getDataAnalyzed().getArray());


        // Execute
        return anonymize(manager, handle.getDefinition(), config).asResult(config, handle);
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
     * Performs some sanity checks.
     *
     * @param config
     * @param manager the manager
     */
    private void checkAfterEncoding(final ARXConfiguration config, final DataManager manager) {

        if (config.isPrivacyModelSpecified(KAnonymity.class)){
            KAnonymity c = config.getPrivacyModel(KAnonymity.class);
            // TODO: getDataGeneralized().getDataLength() does not consider data subsets
            if ((c.getK() > manager.getDataGeneralized().getDataLength()) || (c.getK() < 1)) { 
                throw new IllegalArgumentException("Parameter k (" + c.getK() + ") must be >=1 and less or equal than the number of rows (" + manager.getDataGeneralized().getDataLength()+")"); 
            }
        }
        if (config.isPrivacyModelSpecified(LDiversity.class)){
            for (LDiversity c : config.getPrivacyModels(LDiversity.class)){
                // TODO: getDataGeneralized().getDataLength() does not consider data subsets
                if ((c.getL() > manager.getDataGeneralized().getDataLength()) || (c.getL() < 1)) { 
                    throw new IllegalArgumentException("Parameter l (" + c.getL() + ") must be >=1 and less or equal than the number of rows (" + manager.getDataGeneralized().getDataLength()+")"); 
                }
            }
        }
        if (config.isPrivacyModelSpecified(DDisclosurePrivacy.class)){
            for (DDisclosurePrivacy c : config.getPrivacyModels(DDisclosurePrivacy.class)){
                if (c.getD() <= 0) { 
                    throw new IllegalArgumentException("Parameter d (" + c.getD() + ") must be positive and larger than 0"); 
                }
            }
        }
        if (config.isPrivacyModelSpecified(BasicBLikeness.class)){
            for (BasicBLikeness c : config.getPrivacyModels(BasicBLikeness.class)){
                if (c.getB() <= 0) { 
                    throw new IllegalArgumentException("Parameter b (" + c.getB() + ") must be positive and larger than 0"); 
                }
            }
        }
        if (config.isPrivacyModelSpecified(EnhancedBLikeness.class)){
            for (EnhancedBLikeness c : config.getPrivacyModels(EnhancedBLikeness.class)){
                if (c.getB() <= 0) { 
                    throw new IllegalArgumentException("Parameter b (" + c.getB() + ") must be positive and larger than 0"); 
                }
            }
        }
        
        // Check whether all hierarchies are monotonic
        for (final GeneralizationHierarchy hierarchy : manager.getHierarchies()) {
            hierarchy.checkMonotonicity(manager);
        }

        // check min and max sizes
        final int[] hierarchyHeights = manager.getHierachiesHeights();
        final int[] minLevels = manager.getHierarchiesMinLevels();
        final int[] maxLevels = manager.getHierarchiesMaxLevels();

        for (int i = 0; i < hierarchyHeights.length; i++) {
            if (minLevels[i] > (hierarchyHeights[i] - 1)) { 
                throw new IllegalArgumentException("Invalid minimum generalization for attribute '" + manager.getHierarchies()[i].getName() + "': " +
                                                                                                      minLevels[i] + " > " + (hierarchyHeights[i] - 1));
            }
            if (minLevels[i] < 0) { 
                throw new IllegalArgumentException("The minimum generalization for attribute '" + manager.getHierarchies()[i].getName() + "' has to be positive");
            }
            if (maxLevels[i] > (hierarchyHeights[i] - 1)) {
                throw new IllegalArgumentException("Invalid maximum generalization for attribute '" + manager.getHierarchies()[i].getName() + "': " +
                                                                                                      maxLevels[i] + " > " + (hierarchyHeights[i] - 1));
            }
            if (maxLevels[i] < minLevels[i]) { 
                throw new IllegalArgumentException("The minimum generalization for attribute '" + manager.getHierarchies()[i].getName() +
                                                   "' has to be lower than or equal to the defined maximum");
            }
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


        // Check for null
        if (handle == null) { throw new NullPointerException("Data must not be null"); }
        
        // Check sensitive attributes
        if (config.isPrivacyModelSpecified(LDiversity.class) ||
            config.isPrivacyModelSpecified(TCloseness.class) ||
            config.isPrivacyModelSpecified(DDisclosurePrivacy.class) ||
            config.isPrivacyModelSpecified(BasicBLikeness.class) ||
            config.isPrivacyModelSpecified(EnhancedBLikeness.class)) {
            
            if (handle.getDefinition().getSensitiveAttributes().size() == 0) { 
                throw new IllegalArgumentException("You need to specify a sensitive attribute");
            }
        }
        
        // Check response variables
        if (config.getQualityModel() instanceof MetricSDClassification) {
            if (handle.getDefinition().getResponseVariables().isEmpty()) {
                throw new IllegalArgumentException("At least one response variable must be defined");
            }
            for (String attribute : handle.getDefinition().getResponseVariables()) {
                if (handle.getDefinition().getIdentifyingAttributes().contains(attribute)) {
                    throw new IllegalArgumentException("Response variables must not be identifying");
                }
            }
        }
        
        // Check if all needed hierarchies have been defined
        for (String attribute : handle.getDefinition().getQuasiIdentifiersWithGeneralization()) {
            if (handle.getDefinition().getHierarchy(attribute) == null) {
                throw new IllegalStateException("No hierarchy available for quasi-identifier (" + attribute + ")");
            }
        }
        
        for (String attr : handle.getDefinition().getSensitiveAttributes()){
            boolean found = false;
            for (LDiversity c : config.getPrivacyModels(LDiversity.class)) {
                if (c.getAttribute().equals(attr)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (TCloseness c : config.getPrivacyModels(TCloseness.class)) {
                    if (c.getAttribute().equals(attr)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                for (DDisclosurePrivacy c : config.getPrivacyModels(DDisclosurePrivacy.class)) {
                    if (c.getAttribute().equals(attr)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                for (BasicBLikeness c : config.getPrivacyModels(BasicBLikeness.class)) {
                    if (c.getAttribute().equals(attr)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                for (EnhancedBLikeness c : config.getPrivacyModels(EnhancedBLikeness.class)) {
                    if (c.getAttribute().equals(attr)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                throw new IllegalArgumentException("No privacy model specified for sensitive attribute: '" + attr + "'");
            }
        }
        for (LDiversity c : config.getPrivacyModels(LDiversity.class)) {
            if (handle.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("L-Diversity model defined for non-sensitive attribute '" + c.getAttribute()+ "'");
            }
        }
        for (TCloseness c : config.getPrivacyModels(TCloseness.class)) {
            if (handle.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("T-Closeness model defined for non-sensitive attribute '" + c.getAttribute()+ "'");
            }
        }
        for (DDisclosurePrivacy c : config.getPrivacyModels(DDisclosurePrivacy.class)) {
            if (handle.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("D-Disclosure privacy model defined for non-sensitive attribute '" + c.getAttribute()+ "'");
            }
        }
        for (BasicBLikeness c : config.getPrivacyModels(BasicBLikeness.class)) {
            if (handle.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("Basic-b-likeness model defined for non-sensitive attribute '" + c.getAttribute()+ "'");
            }
        }
        for (EnhancedBLikeness c : config.getPrivacyModels(EnhancedBLikeness.class)) {
            if (handle.getDefinition().getAttributeType(c.getAttribute()) != AttributeType.SENSITIVE_ATTRIBUTE) {
                throw new RuntimeException("Enhanced-b-likeness model defined for non-sensitive attribute '" + c.getAttribute()+ "'");
            }
        }

        // Check handle
        if (!(handle instanceof DataHandleInput)) { 
            throw new IllegalArgumentException("Invalid data handle provided!"); 
        }

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
        
        for (String attribute : handle.getDefinition().getQuasiIdentifiersWithMicroaggregation()) {
            
            if (handle.getDefinition().getMicroAggregationFunction(attribute)==null) {
                throw new IllegalArgumentException("No aggregation function specified for attribute '" + attribute + "'");
            }
            
            MicroAggregationFunction f = (MicroAggregationFunction) definition.getMicroAggregationFunction(attribute);
            DataType<?> t = definition.getDataType(attribute);
            if (!t.getDescription().getScale().provides(f.getRequiredScale())) {
                throw new IllegalArgumentException("Attribute '" + attribute + "' has an aggregation function specified wich needs a datatype with a scale of measure of at least " + f.getRequiredScale());
            }
        }
        
        // Check constraints for (e,d)-DP
        if (config.isPrivacyModelSpecified(EDDifferentialPrivacy.class)) {
            if (!definition.getQuasiIdentifiersWithMicroaggregation().isEmpty()) {
                throw new IllegalArgumentException("Differential privacy must not be combined with micro-aggregation");
            }
            EDDifferentialPrivacy edpModel = config.getPrivacyModel(EDDifferentialPrivacy.class);
            if (edpModel.getEpsilon() <= 0d) {
                throw new IllegalArgumentException("The privacy budget must be > 0");
            }
            if (edpModel.getDelta() <= 0d) {
                throw new IllegalArgumentException("The privacy parameter delta must be > 0");
            }
            if (edpModel.isDataDependent()) {
                if (!config.getQualityModel().isScoreFunctionSupported()) {
                    throw new IllegalArgumentException("Data-dependent differential privacy for the quality model " + config.getQualityModel().getName() + " is not yet implemented");
                }
                if (config.getDPSearchBudget() <= 0) {
                    throw new IllegalArgumentException("The privacy budget to use for the search algorithm must be > 0");
                }
                if (config.getDPSearchBudget() >= edpModel.getEpsilon()) {
                    throw new IllegalArgumentException("The privacy budget to use for the search algorithm must be smaller than the overall privacy budget");
                }
                if (config.getDPSearchStepNumber() < 0) {
                    throw new IllegalArgumentException("The the number of steps to use for the differentially private search algorithm must be >= 0");
                }
            }
        }
        
        // Perform sanity checks
        Set<String> genQis = definition.getQuasiIdentifiersWithGeneralization();
        Set<String> clusterQis = definition.getQuasiIdentifiersWithClusteringAndMicroaggregation();
        if ((config.getSuppressionLimit() < 0d) || (config.getSuppressionLimit() > 1d)) { 
            throw new IllegalArgumentException("Suppression rate " + config.getSuppressionLimit() + "must be in [0, 1]"); 
        }
        if ((genQis.size() + clusterQis.size()) == 0) { 
            throw new IllegalArgumentException("You need to specify at least one quasi-identifier with generalization"); 
        }
        if ((genQis.size() + clusterQis.size()) > maxQuasiIdentifiers) { 
            throw new IllegalArgumentException("Too many quasi-identifiers (" + genQis.size()+"). This restriction is configurable"); 
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
     * Returns an algorithm for the given problem instance
     * @param config
     * @param manager
     * @param solutionSpace
     * @param checker
     * @return
     */
    private AbstractAlgorithm getAlgorithm(final ARXConfiguration config,
                                          final DataManager manager,
                                          final SolutionSpace solutionSpace,
                                          final TransformationChecker checker) {

        if (config.isPrivacyModelSpecified(EDDifferentialPrivacy.class)){
            EDDifferentialPrivacy edpModel = config.getPrivacyModel(EDDifferentialPrivacy.class);
            if (edpModel.isDataDependent()) {
                return DataDependentEDDPAlgorithm.create(solutionSpace, checker, edpModel.isDeterministic(),
                                                         config.getDPSearchStepNumber(), config.getDPSearchBudget());
            }
        }

        if (config.isHeuristicSearchEnabled() || solutionSpace.getSize() > config.getHeuristicSearchThreshold()) {
            return LIGHTNINGAlgorithm.create(solutionSpace, checker, config.getHeuristicSearchTimeLimit(), config.getHeuristicSearchStepLimit());
            
        } else {
            FLASHStrategy strategy = new FLASHStrategy(solutionSpace, manager.getHierarchies());
            return FLASHAlgorithm.create(solutionSpace, checker, strategy);
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
    private DataManager getDataManager(final DataHandle handle, final DataDefinition definition, final ARXConfiguration config) throws IOException {

        // Extract data
        String[] header = ((DataHandleInput) handle).header;
        DataMatrix dataArray = ((DataHandleInput) handle).data;
        Dictionary dictionary = ((DataHandleInput) handle).dictionary;
        final DataManager manager = new DataManager(header,
                                                    dataArray,
                                                    dictionary,
                                                    definition,
                                                    config.getPrivacyModels(),
                                                    getAggregateFunctions(definition),
                                                    config.getQualityModel());
        return manager;
    }

    /**
     * Reset a previous lattice and run the algorithm.
     *
     * @param manager
     * @param definition
     * @param config
     * @return
     * @throws IOException
     */
    protected Result anonymize(final DataManager manager,
                               final DataDefinition definition,
                               final ARXConfiguration config) throws IOException {

        // Initialize
        config.initialize(manager);

        // Check
        checkAfterEncoding(config, manager);

        // Build or clean the lattice
        SolutionSpace solutionSpace = new SolutionSpace(manager.getHierarchiesMinLevels(), manager.getHierarchiesMaxLevels());

        // Initialize the metric
        config.getQualityModel().initialize(manager, definition, manager.getDataGeneralized(), manager.getHierarchies(), config);

        // Build a transformation checker
        final TransformationChecker checker = new TransformationChecker(manager,
                                                                        config.getQualityModel(),
                                                                        config.getInternalConfiguration(),
                                                                        historySize,
                                                                        snapshotSizeDataset,
                                                                        snapshotSizeSnapshot,
                                                                        solutionSpace);

        // Create an algorithm instance
        AbstractAlgorithm algorithm = getAlgorithm(config,
                                                   manager,
                                                   solutionSpace,
                                                   checker);
        algorithm.setListener(listener);

        
        // Execute

        long time = System.currentTimeMillis();
        boolean optimumFound = algorithm.traverse();
        
        // Free resources
        checker.reset();
        
        // Return the result
        return new Result(checker, solutionSpace, manager, algorithm, time, optimumFound);
    }

    /**
     * Parses the settings provided by the given instance
     * @param anonymizer
     */
    protected void parse(ARXAnonymizer anonymizer) {
        this.historySize = anonymizer.historySize;
        this.snapshotSizeDataset = anonymizer.snapshotSizeDataset;
        this.snapshotSizeSnapshot = anonymizer.snapshotSizeSnapshot;
        this.maxQuasiIdentifiers = anonymizer.maxQuasiIdentifiers;
    }
}
