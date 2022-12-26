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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Memory tracker
 * @author Fabian Prasser
 */
public class MemoryTracker {
    
    /** Delay and period in milliseconds*/
    private long DELAY = 1000L;
    
    /** Max bytes used*/
    private long maxBytesUsed = 0;
    
    /** Service*/
    private ScheduledExecutorService service; 
    
    /**
     * Creates a new instance
     */
    public MemoryTracker() {
        service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                maxBytesUsed = Math.max(maxBytesUsed, getUsedBytes());
            }
        }, DELAY, DELAY, TimeUnit.MILLISECONDS);
    }

    /**
     * Get used bytes after forced GC
     * 
     * @return
     */
    private long getUsedBytes() {
        
        long before = getGcCount();
        System.gc();
        while (getGcCount() == before) {
            // Nothing
        }
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
               ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }
    
    /**
     * Returns the GC count
     * @return
     */
    private long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) {
                sum += count;
            }
        }
        return sum;
    }
    
    /**
     * Returns the max bytes used
     * @return
     */
    public long getMaxBytesUsed() {
        service.shutdown();
        return this.maxBytesUsed;
    }
}
