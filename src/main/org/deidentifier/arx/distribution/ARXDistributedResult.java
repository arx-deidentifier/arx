package org.deidentifier.arx.distribution;

import org.deidentifier.arx.DataHandle;

public class ARXDistributedResult {
    
    /**
     * Returns a handle to the data obtained by applying the optimal transformation. This method will fork the buffer, 
     * allowing to obtain multiple handles to different representations of the data set. Note that only one instance can
     * be obtained for each transformation.
     * 
     * @return
     */
    public DataHandle getOutput() {
        return null;
    }
}
