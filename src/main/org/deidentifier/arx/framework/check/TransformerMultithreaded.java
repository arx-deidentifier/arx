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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.deidentifier.arx.ARXConfiguration.ARXConfigurationInternal;
import org.deidentifier.arx.framework.check.StateMachine.TransitionType;
import org.deidentifier.arx.framework.check.distribution.IntArrayDictionary;
import org.deidentifier.arx.framework.check.groupify.HashGroupify;
import org.deidentifier.arx.framework.check.transformer.AbstractTransformer;
import org.deidentifier.arx.framework.data.GeneralizationHierarchy;

/**
 * The class Transformer.
 * 
 * @author Fabian Prasser
 * @author Florian Kohlmayer
 */
public class TransformerMultithreaded extends Transformer {

    private final int threads;
    private final AbstractTransformer[][] transformers;
    private final ExecutorService pool;
    
    /**
     * Instantiates a new transformer.
     *
     * @param inputGeneralized
     * @param inputAnalyzed
     * @param hierarchies
     * @param config
     * @param dictionarySensValue
     * @param dictionarySensFreq
     */
    public TransformerMultithreaded(final int[][] inputGeneralized,
                                    final int[][] inputAnalyzed,
                                    final GeneralizationHierarchy[] hierarchies,
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
        for (int i=1; i<threads; i++) {
            this.transformers[i] = super.createTransformers();
        }
        
        // TODO: Shutdown
        this.pool = Executors.newFixedThreadPool(threads, new ThreadFactory(){

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("ARX Transformer & Analyzer");
                return thread;
            }
            
        });
    }

    /**
     * Apply internal.
     * 
     * @param projection
     *            the projection
     * @param transformation
     *            the state
     * @param source
     *            the source
     * @param target
     *            the target
     * @param snapshot
     *            the snapshot
     * @param transition
     *            the transition
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

        // Single threaded
        if (total < 5000) {

            int startIndex = 0;
            int stopIndex = total;
            getTransformer(projection, transformation, source, target, snapshot, transition, startIndex, stopIndex, 0).call();
            return;
        }
        
        // List
        List<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();

        // For each thread
        for (int i = 0; i < threads; i++) {

            // Execute
            final int thread = i;
            final int stepping = total / this.threads;
            futures.add(pool.submit(new Runnable() {
                    public void run() {
                        
                        int startIndex = thread * stepping;
                        int stopIndex = (thread + 1) * stepping;
                        if (thread == threads - 1) {
                            stopIndex = total;
                        }                        
                        getTransformer(projection, transformation, source, target, snapshot, transition, startIndex, stopIndex, thread).call();
                    }
            }, true));
        }
        
        // Wait
        for (Future<Boolean> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new RuntimeException("Error executing thread", e);
            }
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
    
    @Override
    public void finalize() {
        this.pool.shutdown();
        System.out.println("Shutting down");
    }
}
