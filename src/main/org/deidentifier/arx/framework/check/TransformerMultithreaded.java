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

package org.deidentifier.arx.framework.check;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.StateMachine.TransitionType;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.groupify.HashGroupifyEntry;
import org.deidentifier.arx.framework.check.transformer.AbstractTransformer;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * The class Transformer.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TransformerMultithreaded extends Transformer {
    
    private static final int              MINIMAL_NUMER_OF_ENTRIES_PER_THREAD = 5000;
                                                                              
    private ExecutorService               pool;
    private final int                     threads;
    private final AbstractTransformer[][] transformers;
                                          
    private final HashGroupify[]          groupifies;
                                          
    /**
     * Instantiates a new transformer.
     *
     * @param inputGeneralized
     * @param inputAnalyzed
     * @param hierarchies
     * @param initialGroupifySize
     * @param config
     * @param dictionarySensValue
     * @param dictionarySensFreq
     */
    public TransformerMultithreaded(final int[][] inputGeneralized,
                                    final int[][] inputAnalyzed,
                                    final GeneralizationHierarchy[] hierarchies,
                                    final int initialGroupifySize,
                                    final ARXConfigurationInternal config,
                                    final IntArrayDictionary dictionarySensValue,
                                    final IntArrayDictionary dictionarySensFreq) {
                                    
        super(inputGeneralized,
              inputAnalyzed,
              hierarchies,
              config,
              dictionarySensValue,
              dictionarySensFreq);
              
        this.threads = config.getNumThreads();
        this.transformers = new AbstractTransformer[threads][];
        this.transformers[0] = super.getTransformers(); // Reuse
        for (int i = 1; i < threads; i++) {
            this.transformers[i] = super.createTransformers();
        }
        this.groupifies = new HashGroupify[this.threads - 1];
        for (int i = 0; i < groupifies.length; i++) {
            this.groupifies[i] = new HashGroupify(initialGroupifySize / threads, config);
        }
    }
    
    @Override
    public void shutdown() {
        if (pool != null) {
            pool.shutdown();
            pool = null;
        }
    }
    
    /**
     * Returns a transformer for a specific region of the dataset
     * @param projection
     * @param transformation
     * @param source
     * @param target
     * @param snapshot
     * @param transition
     * @param thread
     * @return
     */
    private AbstractTransformer getTransformer(long projection,
                                               int[] transformation,
                                               HashGroupify source,
                                               HashGroupify target,
                                               int[] snapshot,
                                               TransitionType transition,
                                               int startIndex,
                                               int stopIndex,
                                               int thread) {
                                               
        AbstractTransformer app = getTransformer(projection, transformers[thread]);
        app.init(projection,
                 transformation,
                 target,
                 source,
                 snapshot,
                 transition,
                 startIndex,
                 stopIndex);
                 
        return app;
    }
    
    /**
     * Apply internal.
     * 
     * @param projection the projection
     * @param transformation the state
     * @param source the source
     * @param target the target
     * @param snapshot the snapshot
     * @param transition the transition
     * @return the hash groupify
     */
    protected void applyInternal(final long projection,
                                 final int[] transformation,
                                 final HashGroupify source,
                                 final HashGroupify target,
                                 final int[] snapshot,
                                 final TransitionType transition) {
                                 
        // Determine total
        final int total;
        switch (transition) {
        case UNOPTIMIZED:
            total = getDataLength();
            break;
        case ROLLUP:
            total = source.getNumberOfEquivalenceClasses();
            break;
        case SNAPSHOT:
            total = snapshot.length / getSnapshotLength();
            break;
        default:
            throw new IllegalStateException("Unknown transition type");
        }
        
        // Create pool
        if (this.pool == null) {
            this.pool = Executors.newFixedThreadPool(threads - 1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setName("ARX Transformer & Analyzer");
                    return thread;
                }
            });
        }
        
        // calculate number of threads to use
        int numTreads = (int) ((double) total / (double) MINIMAL_NUMER_OF_ENTRIES_PER_THREAD);
        
        // Always use at least one thread, but not more than specified
        if (numTreads == 0) {
            numTreads = 1;
        } else if (numTreads > threads) {
            numTreads = threads;
        }
        
        // sync on lock
        final ReentrantLock lock = new ReentrantLock();
        final CountDownLatch runningThreads = new CountDownLatch(numTreads);
        
        // number of items per thread
        final int stepping = total / numTreads;
        
        // For each thread (if more than one)
        for (int i = 1; i < numTreads; i++) {
            
            // Execute
            final int thread = i;
            final int startIndex = thread * stepping;
            final int stopIndex = thread == threads - 1 ? total : (thread + 1) * stepping;
            
            // Worker thread
            pool.execute(new Runnable() {
                public void run() {
                    getTransformer(projection,
                                   transformation,
                                   source,
                                   groupifies[thread - 1],
                                   snapshot,
                                   transition,
                                   startIndex,
                                   stopIndex,
                                   thread).call();
                                   
                    // Write back
                    HashGroupifyEntry element = groupifies[thread - 1].getFirstEquivalenceClass();
                    
                    // Only one thread at a time
                    lock.lock();
                    while (element != null) {
                        
                        // Add
                        target.addFromThread(element.getHashcode(),
                                             element.getKey(),
                                             element.getDistributions(),
                                             element.getRepresentative(),
                                             element.getCount(),
                                             element.getPcount());
                                             
                        // Next element
                        element = element.getNextOrdered();
                        
                    }
                    lock.unlock();
                    
                    groupifies[thread - 1].stateClear();
                    runningThreads.countDown();
                    
                }
            });
        }
        
        // Prepare main thread
        final int thread = 0;
        final int startIndex = 0;
        final int stopIndex = numTreads == 1 ? total : (thread + 1) * stepping;
        
        // Main thread
        lock.lock();
        getTransformer(projection, transformation, source, target, snapshot, transition, startIndex, stopIndex, thread).call();
        lock.unlock();
        
        runningThreads.countDown();
        // Wait for all threads to finish
        try {
            runningThreads.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Waiting interrupted: " + e);
        }
        
    }
}
