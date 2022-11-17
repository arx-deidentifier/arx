package org.deidentifier.arx.distribution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
     * Partitioning strategy
     * @author Fabian Prasser
     */
    public static enum PartitioningStrategy {
        RANDOM,
        SORTED
    }

    /**
     * Distribution strategy
     * @author Fabian Prasser
     */
    public static enum DistributionStrategy {
        LOCAL
    }
    
    /** O_min*/
    private static final double O_MIN = 0.05d;
    
    /** Number of nodes to use */
    private final int                  nodes;
    /** Partitioning strategy */
    private final PartitioningStrategy partitioningStrategy;
    /** Distribution strategy*/
    private final DistributionStrategy distributionStrategy;
    /** Random */
    private final Random               random               = new Random(0xDEADBEEF);
    /** Global transformation */
    private final boolean              globalTransformation;

    /**
     * Creates a new instance
     * @param nodes
     * @param partitioningStrategy
     * @param distributionStrategy
     * @param globalTransformation
     */
    public ARXDistributedAnonymizer(int nodes,
                                    PartitioningStrategy partitioningStrategy,
                                    DistributionStrategy distributionStrategy,
                                    boolean globalTransformation) {
        this.nodes = nodes;
        this.partitioningStrategy = partitioningStrategy;
        this.globalTransformation = globalTransformation;
        this.distributionStrategy = distributionStrategy;
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
        List<Data> partitions = null;
        switch (partitioningStrategy) {
        case RANDOM:
            partitions = getPartitionsRandom(data, this.nodes);
            break;
        case SORTED:
            partitions = getPartitionsSorted(data, this.nodes);
            break;
        }
        timePrepare = System.currentTimeMillis() - timePrepare;
        
        // Anonymize
        List<Future<DataHandle>> futures = new ArrayList<>();
        
        // Execute
        long timeAnonymize = System.currentTimeMillis();
        for (Data partition : partitions) {
            switch (distributionStrategy) {
            case LOCAL:
                futures.add(new ARXWorkerLocal().anonymize(partition, config, globalTransformation, O_MIN));
                break;
            }
        }
        
        // Wait for execution
        List<DataHandle> handles = new ArrayList<>();
        while (!futures.isEmpty()) {
            Iterator<Future<DataHandle>> iter = futures.iterator();
            while (iter.hasNext()) {
                Future<DataHandle> future = iter.next();
                if (future.isDone()) {
                    handles.add(future.get());
                    iter.remove();
                }
            }
            Thread.sleep(100);
        }
        
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
    private List<Data> getPartitionsRandom(Data data, int number) {
        
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
        List<Data> result = new ArrayList<>();
        for (List<String[]> partition : list) {
            Data _data = Data.create(partition);
            _data.getDefinition().read(definition.clone());
            result.add(_data);
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
    private List<Data> getPartitionsSorted(Data data, int number) {

        // Copy definition
        DataDefinition definition = data.getDefinition();
        
        // Prepare
        List<Data> result = new ArrayList<>();
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
            result.add(_data);
            start = end;
            end = end + size;
        }
        
        // Done
        return result;
    }
}
