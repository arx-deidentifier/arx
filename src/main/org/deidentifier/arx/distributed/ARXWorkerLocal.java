package org.deidentifier.arx.distributed;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.exceptions.RollbackRequiredException;

/**
 * A local worker running in a local thread
 * @author Fabian Prasser
 */
public class ARXWorkerLocal implements ARXWorker {
    
    @Override
    public Future<DataHandle> anonymize(final Data partition,
                                        final ARXConfiguration _config,
                                        final boolean globalTransformation,
                                        final double oMin) throws IOException, RollbackRequiredException {
        
        System.out.println("Anonymizing partition: " + partition.getHandle().getNumRows());
        
        // Executor service
        ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // Clone
        ARXConfiguration config = _config.clone();
        
        // Execute 
        return executor.submit(new Callable<DataHandle>() {
            @Override
            public DataHandle call() throws Exception {
                
                // Prepare local transformation
                if (!globalTransformation) {
                    config.setSuppressionLimit(1d - oMin);
                }
                
                // Anonymize
                ARXAnonymizer anonymizer = new ARXAnonymizer();
                ARXResult result = anonymizer.anonymize(partition, config);
                DataHandle handle = result.getOutput();
                
                // Local transformation
                if (!globalTransformation) {
                    result.optimizeIterativeFast(handle, oMin);
                }
                
                // Done
                executor.shutdown();
                return handle;
            }
        });
    }
}
