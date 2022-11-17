package org.deidentifier.arx.distributed;

import java.io.IOException;
import java.util.concurrent.Future;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.exceptions.RollbackRequiredException;

/**
 * Worker for anonymization prozesses
 * @author Fabian Prasser
 */
public interface ARXWorker {
    
    /**
     * Anonymize a dataset in a worker
     * @param partition
     * @param config
     * @param globalTransformation
     * @param oMin
     * @return
     * @throws IOException
     * @throws RollbackRequiredException
     */
    public Future<DataHandle> anonymize(Data partition, 
                                        ARXConfiguration config, 
                                        boolean globalTransformation,
                                        double oMin) throws IOException, RollbackRequiredException;
}
