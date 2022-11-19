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
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.deidentifier.arx.ARXConfiguration;
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
     * Strategy for defining common generalization levels
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
    /** Random */
    private final Random               random               = new Random(0xDEADBEEF);

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
        
        // Partition
        long timePrepare = System.currentTimeMillis();
        List<DataHandle> partitions = null;
        switch (partitioningStrategy) {
        case RANDOM:
            partitions = getPartitionsRandom(data, this.nodes);
            break;
        case SORTED:
            partitions = getPartitionsSorted(data, this.nodes);
            break;
        }
        timePrepare = System.currentTimeMillis() - timePrepare;
        
        // Start time measurement
        long timeAnonymize = System.currentTimeMillis();
        
        // Global transformation
        int[] transformation = null;
        if (transformationStrategy != TransformationStrategy.LOCAL) {
            transformation = getTransformation(partitions, config, transformationStrategy);
        }
        
        // Anonymize
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
            }
        }
        
        // Wait for execution
        List<DataHandle> handles = getResults(futures);
        
        // Merge
        timeAnonymize = System.currentTimeMillis() - timeAnonymize;
        return new ARXDistributedResult(handles, timePrepare, timeAnonymize);
    }
    
    /**
     * Partitions the dataset randomly
     * @param data
     * @param number
     * @return
     */
    private List<DataHandle> getPartitionsRandom(Data data, int number) {
        
        // Copy definition
        DataDefinition definition = data.getDefinition();
        
        // Randomly partition
        DataHandle handle = data.getHandle();
        Iterator<String[]> iter = handle.iterator();
        String[] header = iter.next();

        // Lists
        List<List<String[]>> list = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            List<String[]> _list = new ArrayList<>();
            _list.add(header);
            list.add(_list);
        }
        
        // Distributed records
        while (iter.hasNext()) {
            list.get(random.nextInt(number)).add(iter.next());
        }
        
        // Convert to data
        List<DataHandle> result = new ArrayList<>();
        for (List<String[]> partition : list) {
            Data _data = Data.create(partition);
            _data.getDefinition().read(definition.clone());
            result.add(_data.getHandle());
        }
        
        // Done
        return result;
    }

    /**
     * Partitions the dataset using ordering
     * @param data
     * @param number
     * @return
     */
    private List<DataHandle> getPartitionsSorted(Data data, int number) {

        // Copy definition
        DataDefinition definition = data.getDefinition();
        
        // Prepare
        List<DataHandle> result = new ArrayList<>();
        DataHandle handle = data.getHandle();
        
        // TODO: Would make sense to only sort based on key and sensible variables
        handle.sort(true, 0, handle.getNumColumns() - 1);
        
        // Convert
        List<String[]> rows = new ArrayList<>();
        Iterator<String[]> iter = handle.iterator();
        String[] header = iter.next();
        while (iter.hasNext()) {
            rows.add(iter.next());
        }
        
        // Split
        // TODO: Check for correctness
        double size = (double)handle.getNumRows() / (double)number;
        double start = 0d;
        double end = size;
        for (int i = 0; i < number; i++) {
            List<String[]> _list = new ArrayList<>();
            _list.add(header);
            _list.addAll(rows.subList((int)Math.round(start), (int)Math.round(end)));
            Data _data = Data.create(_list);
            _data.getDefinition().read(definition.clone());
            result.add(_data.getHandle());
            start = end;
            end = end + size;
        }
        
        // Done
        return result;
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
     * Retrieves the generalization scheme using the current strategy
     * @param partitions
     * @param config
     * @return
     * @throws IOException 
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    private int[] getTransformation(List<DataHandle> partitions, ARXConfiguration config, TransformationStrategy strategy) throws IOException, InterruptedException, ExecutionException {
        
        // Calculate schemes
        List<Future<int[]>> futures = new ArrayList<>();
        for (DataHandle partition : partitions) {
            futures.add(new ARXWorkerLocal().transform(partition, config));
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
