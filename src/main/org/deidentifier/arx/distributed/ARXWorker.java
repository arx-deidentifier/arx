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
import java.util.concurrent.Future;

import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.exceptions.RollbackRequiredException;

/**
 * Worker for anonymization processes
 * @author Fabian Prasser
 */
public interface ARXWorker {
    
    /**
     * Performs global transformation
     * @param partition
     * @param config
     * @return
     * @throws IOException
     * @throws RollbackRequiredException
     */
    public Future<DataHandle> anonymize(DataHandle partition, 
                                        ARXConfiguration config) throws IOException, RollbackRequiredException;
    
    /**
     * Performs local transformation
     * @param partition
     * @param config
     * @param recordsPerIteration
     * @return
     * @throws IOException
     * @throws RollbackRequiredException
     */
    public Future<DataHandle> anonymize(DataHandle partition, 
                                        ARXConfiguration config,
                                        double recordsPerIteration) throws IOException, RollbackRequiredException;
    
    /**
     * Performs global transformation and returns a transformation scheme
     * @param partition
     * @param config
     * @return
     * @throws IOException
     * @throws RollbackRequiredException
     */
    public Future<int[]> transform(DataHandle partition, ARXConfiguration config) throws IOException;
}
