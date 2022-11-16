package org.deidentifier.arx.distribution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
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
    
    /** O_min*/
    private static final double O_MIN = 0.05d;
    
    /** Number of nodes to use */
    private final int                  nodes;
    /** Partitioning strategy */
    private final PartitioningStrategy partitioningStrategy;
    /** Random */
    private final Random               random               = new Random(0xDEADBEEF);
    /** Global transformation */
    private final boolean              globalTransformation;

    /**
     * Creates a new instance
     * @param nodes
     * @param partitioningStrategy
     * @param globalTransformation
     */
    public ARXDistributedAnonymizer(int nodes,
                                    PartitioningStrategy partitioningStrategy,
                                    boolean globalTransformation) {
        this.nodes = nodes;
        this.partitioningStrategy = partitioningStrategy;
        this.globalTransformation = globalTransformation;
    }
    
    /**
     * Performs data anonymization.
     *
     * @param data The data
     * @param config The privacy config
     * @return ARXResult
     * @throws IOException
     * @throws RollbackRequiredException 
     */
    public ARXDistributedResult anonymize(Data data, ARXConfiguration config) throws IOException, RollbackRequiredException {
        
        // Partition
        List<Data> partitions = null;
        switch (partitioningStrategy) {
        case RANDOM:
            partitions = getPartitionsRandom(data, this.nodes);
            break;
        case SORTED:
            partitions = getPartitionsSorted(data, this.nodes);
            break;
        }
        
        // Anonymize
        List<DataHandle> handles = new ArrayList<>();
        for (Data partition : partitions) {
            handles.add(anonymize(partition, config, globalTransformation));
        }
        
        // Merge
        return new ARXDistributedResult(handles);
    }
    
    /**
     * Anonymize a partition
     * @param partition
     * @param config
     * @param globalTransformation
     * @throws IOException 
     * @throws RollbackRequiredException 
     */
    private DataHandle anonymize(Data partition, ARXConfiguration _config, boolean globalTransformation) throws IOException, RollbackRequiredException {
        
        // Prepare local transformation
        ARXConfiguration config = _config.clone();
        if (!globalTransformation) {
            config.setSuppressionLimit(1d - O_MIN);
        }
        
        // Anonymize
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        ARXResult result = anonymizer.anonymize(partition, config);
        DataHandle handle = result.getOutput();
        
        // Local transformation
        if (!globalTransformation) {
            result.optimizeIterativeFast(handle, O_MIN);
        }
        
        // Done
        return handle;
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
            _data.getDefinition().read(definition);
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
            _data.getDefinition().read(definition);
            result.add(_data);
            start = end;
            end = end + size;
        }
        
        // Done
        return result;
    }
}
