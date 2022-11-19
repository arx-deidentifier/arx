/*
 * ARX Data Anonymization Tool
 * Copyright 2012 - 2022 Fabian Prasser and contributors
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

package org.deidentifier.arx.distributed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.Monotonicity;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataDefinition;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.exceptions.RollbackRequiredException;

/**
 * Distributed anonymizer
 * @author Fabian Prasser
 *
 */
public class ARXDistributedAnonymizer {
    
    /**
     * Distribution strategy
     * @author Fabian Prasser
     */
    public static enum DistributionStrategy {
        LOCAL
    }

    /**
     * Partitioning strategy
     * @author Fabian Prasser
     */
    public static enum PartitioningStrategy {
        RANDOM,
        SORTED
    }
    
    /**
     * Strategy for defining common transformation levels
     * @author Fabian Prasser
     */
    public static enum TransformationStrategy {
        GLOBAL_AVERAGE,
        GLOBAL_MINIMUM,
        LOCAL
    }
    
    /** O_min*/
    private static final double O_MIN = 0.05d;

    /** Wait time*/
    private static final int WAIT_TIME = 100;
    
    /** Number of nodes to use */
    private final int                  nodes;
    /** Partitioning strategy */
    private final PartitioningStrategy partitioningStrategy;
    /** Distribution strategy*/
    private final DistributionStrategy distributionStrategy;
    /** Distribution strategy*/
    private final TransformationStrategy transformationStrategy;

    /**
     * Creates a new instance
     * @param nodes
     * @param partitioningStrategy
     * @param distributionStrategy
     * @param transformationStrategy
     */
    public ARXDistributedAnonymizer(int nodes,
                                    PartitioningStrategy partitioningStrategy,
                                    DistributionStrategy distributionStrategy,
                                    TransformationStrategy transformationStrategy) {
        this.nodes = nodes;
        this.partitioningStrategy = partitioningStrategy;
        this.distributionStrategy = distributionStrategy;
        this.transformationStrategy = transformationStrategy;
    }
    
    /**
     * Performs data anonymization.
     *
     * @param data The data
     * @param config The privacy config
     * @return ARXResult
     * @throws IOException
     * @throws RollbackRequiredException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public ARXDistributedResult anonymize(Data data, 
                                          ARXConfiguration config) throws IOException, RollbackRequiredException, InterruptedException, ExecutionException {
        
        // Store definition
        DataDefinition definition = data.getDefinition().clone();
        
        // Sanity check
        if (data.getHandle().getNumRows() < 2) {
            throw new IllegalArgumentException("Dataset must contain at least two rows");
        }
        if (data.getHandle().getNumRows() < nodes) {
            throw new IllegalArgumentException("Dataset must contain at least as many records as nodes");
        }
        
        // #########################################
        // STEP 1: PARTITIONING
        // #########################################
        long timePrepare = System.currentTimeMillis();
        List<DataHandle> partitions = null;
        switch (partitioningStrategy) {
        case RANDOM:
            partitions = ARXPartition.getPartitionsRandom(data, this.nodes);
            break;
        case SORTED:
            partitions = ARXPartition.getPartitionsSorted(data, this.nodes);
            break;
        }
        timePrepare = System.currentTimeMillis() - timePrepare;
        
        // #########################################
        // STEP 2: ANONYMIZATION
        // #########################################
        
        // Start time measurement
        long timeAnonymize = System.currentTimeMillis();
        
        // ##########################################
        // STEP 2a: IF GLOBAL, RETRIEVE COMMON SCHEME
        // ##########################################
        
        // Global transformation
        int[] transformation = null;
        if (transformationStrategy != TransformationStrategy.LOCAL) {
            transformation = getTransformation(partitions, 
                                               config,
                                               distributionStrategy, 
                                               transformationStrategy);
        }
        
        // ###############################################
        // STEP 2b: PERFORM LOCAL OR GLOBAL TRANSFORMATION
        // ###############################################

        // Anonymize
        List<Future<DataHandle>> futures = getAnonymization(partitions, 
                                                            config, 
                                                            distributionStrategy, 
                                                            transformation);
        
        // Wait for execution
        List<DataHandle> handles = getResults(futures);
        
        // ###############################################
        // STEP 3: HANDLE NON-MONOTONIC SETTINGS
        // ###############################################
        Map<String, List<Double>> qualityMetrics = null;
        if (config.getMonotonicityOfPrivacy() != Monotonicity.FULL) {
            
            // Prepare merged dataset
            ARXDistributedResult mergedResult = new ARXDistributedResult(handles);
            Data merged = ARXPartition.getData(mergedResult.getOutput());
            merged.getDefinition().read(definition);
            
            // Partition sorted while keeping sure to assign records 
            // within equivalence classes to exactly one partition
            // Also removes all hierarchies
            partitions = ARXPartition.getPartitionsByClass(merged, nodes);
            
            // Fix transformation scheme: all zero
            config = config.clone();
            config.setSuppressionLimit(1d);
            transformation = new int[definition.getQuasiIdentifyingAttributes().size()];
            
            // Suppress equivalence classes
            futures = getAnonymization(partitions, config, distributionStrategy, transformation);
            
            // Wait for execution
            handles = getResults(futures);
            
            // We keep the quality metrics, because there is no other
            // easy way to calculate them later
            qualityMetrics = mergedResult.getQuality();
        }
        
        // ###############################################
        // STEP 4: MERGE AND DONE
        // ###############################################
        
        timeAnonymize = System.currentTimeMillis() - timeAnonymize;
        return new ARXDistributedResult(handles, timePrepare, timeAnonymize, qualityMetrics);
    }
    
    /**
     * Aonymizes the partitions
     * @param partitions
     * @param config
     * @param distributionStrategy2
     * @param transformation
     * @return
     * @throws RollbackRequiredException 
     * @throws IOException 
     */
    private List<Future<DataHandle>> getAnonymization(List<DataHandle> partitions,
                                                      ARXConfiguration config,
                                                      DistributionStrategy distributionStrategy2,
                                                      int[] transformation) throws IOException, RollbackRequiredException {
        List<Future<DataHandle>> futures = new ArrayList<>();
        for (DataHandle partition : partitions) {
            switch (distributionStrategy) {
            case LOCAL:
                if (transformation != null) {
                    
                    // Get handle
                    Set<String> quasiIdentifiers = partition.getDefinition().getQuasiIdentifyingAttributes();
                    
                    // Fix transformation levels
                    int count = 0;
                    for (int column = 0; column < partition.getNumColumns(); column++) {
                        String attribute = partition.getAttributeName(column);
                        if (quasiIdentifiers.contains(attribute)) {
                            int level = transformation[count];
                            partition.getDefinition().setMinimumGeneralization(attribute, level);
                            partition.getDefinition().setMaximumGeneralization(attribute, level);
                            count++;
                        }
                    }
                    
                    futures.add(new ARXWorkerLocal().anonymize(partition, config));
                } else {
                    futures.add(new ARXWorkerLocal().anonymize(partition, config, O_MIN));
                }
                break;
            default:
                throw new IllegalStateException("Unknown distribution strategy");
            }
        }
        
        // Done
        return futures;
    }

    /**
     * Collects results from the futures
     * @param <T>
     * @param futures
     * @return
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    private <T> List<T> getResults(List<Future<T>> futures) throws InterruptedException, ExecutionException {
        ArrayList<T> results = new ArrayList<>();
        while (!futures.isEmpty()) {
            Iterator<Future<T>> iter = futures.iterator();
            while (iter.hasNext()) {
                Future<T> future = iter.next();
                if (future.isDone()) {
                    results.add(future.get());
                    iter.remove();
                }
            }
            Thread.sleep(WAIT_TIME);
        }
        return results;
    }
    
    /**
     * Retrieves the transformation scheme using the current strategy
     * @param partitions
     * @param config
     * @param distributionStrategy
     * @param transformationStrategy
     * @return
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private int[] getTransformation(List<DataHandle> partitions, ARXConfiguration config, DistributionStrategy distributionStrategy, TransformationStrategy transformationStrategy) throws IOException, InterruptedException, ExecutionException {
        
        // Calculate schemes
        List<Future<int[]>> futures = new ArrayList<>();
        for (DataHandle partition : partitions) {
            switch (distributionStrategy) {
            case LOCAL:
                futures.add(new ARXWorkerLocal().transform(partition, config));
                break;
            default:
                throw new IllegalStateException("Unknown distribution strategy");
            }
            
        }

        // Collect schemes
        List<int[]> schemes = getResults(futures);
        
        // Apply strategy
        switch (transformationStrategy) {
            case GLOBAL_AVERAGE:
                // Sum up all levels
                int[] result = new int[schemes.get(0).length];
                for (int[] scheme : schemes) {
                    for (int i=0; i < result.length; i++) {
                        result[i] += scheme[i];
                    }
                }
                // Divide by number of levels
                for (int i=0; i < result.length; i++) {
                    result[i] = (int)Math.round((double)result[i] / (double)schemes.size());
                }
                return result;
            case GLOBAL_MINIMUM:
                // Find minimum levels
                result = new int[schemes.get(0).length];
                Arrays.fill(result, Integer.MAX_VALUE);
                for (int[] scheme : schemes) {
                    for (int i=0; i < result.length; i++) {
                        result[i] = Math.min(result[i], scheme[i]);
                    }
                }
                return result;
            case LOCAL:
                throw new IllegalStateException("Must not be executed when doing global transformation");
            default:
                throw new IllegalStateException("Unknown transformation strategy");
        }
    }
}
