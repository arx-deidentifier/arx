/*
 * ARX: Powerful Data Anonymization
 * Copyright 2012 - 2015 Florian Kohlmayer, Fabian Prasser
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
package org.deidentifier.arx.framework.check.groupify;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lock manager
 * 
 * @author Florian Kohlmayer
 * @author Fabian Prasser
 */
public class LockManager {

    /** The locks */
    private AtomicBoolean[]     locks;

    /** The locks */               
    private final AtomicBoolean rehashLock;

    /** The locks */              
    private final AtomicBoolean createLock;
                                
    /**
     * Constructor
     * @param buckets
     */
    public LockManager(int buckets) {
        resize(buckets);
        rehashLock = new AtomicBoolean(false);
        createLock = new AtomicBoolean(false);
    }
    
    /**
     * Acquire lock. Busy wait.
     * @param bucket
     */
    public void lockBucket(int bucket) {
        AtomicBoolean lock = locks[bucket];
        lock(lock);
    }
    
    /**
     * Lock new entry lock.
     */
    public void lockCreate() {
        lock(createLock);
    }
    
    /**
     * Lock global lock.
     */
    public void lockRehash() {
        lock(rehashLock);
    }
    
    /**
     * Sets a new size
     * @param size
     */
    public void resize(int size) {
        locks = new AtomicBoolean[size];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new AtomicBoolean(false);
        }
    }
    
    /**
     * Release
     * @param bucket
     */
    public void releaseBucket(int bucket) {
        release(locks[bucket]);
    }
    
    /**
     * Release global lock.
     */
    public void releaseCreate() {
        release(createLock);
    }
    
    /**
     * Release global lock.
     */
    public void releaseRehash() {
        release(rehashLock);
    }
    
    /**
     * Acquires the lock. Busy wait.
     * @param lock
     */
    private void lock(final AtomicBoolean lock) {
        while (true) {
            if (lock.compareAndSet(false, true)) {
                return;
            }
        }
    }
    
    /**
     * Unlocks the lock.
     * @param lock
     */
    private void release(final AtomicBoolean lock) {
        lock.set(false);
    }
}
