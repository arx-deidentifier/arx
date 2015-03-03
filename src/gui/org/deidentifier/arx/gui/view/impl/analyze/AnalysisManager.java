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

package org.deidentifier.arx.gui.view.impl.analyze;

import org.eclipse.swt.widgets.Display;


/**
 * This class manages the execution of asynchronous analyses.
 *
 * @author Fabian Prasser
 */
public class AnalysisManager {
    
    /**
     * A worker for analyses.
     *
     * @author Fabian Prasser
     */
    private class AnalysisWorker implements Runnable {

        /** Stop flag. */
        private volatile boolean stopped = false;
        
        /** Analysis to perform. */
        private final Analysis analysis;
        
        /** The thread. */
        private Thread thread;
        
        /**
         * Creates a new instance.
         *
         * @param analysis
         */
        private AnalysisWorker(Analysis analysis){
            this.analysis = analysis;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                this.analysis.run();
                synchronized(this){
                    if (this.isStopped()) {
                        onInterrupt();
                    }
                    else {
                        onFinish(); 
                    }
                }
            } catch (InterruptedException e){
                onInterrupt();
            } catch (Exception e){
                onError();
            }
        }
        
        /**
         * Trigger.
         */
        private void onInterrupt() {
            display.asyncExec(new Runnable(){
                public void run(){
                    analysis.onInterrupt();
                }
            });
        }
        
        /**
         * Trigger.
         */
        private void onError() {
            display.asyncExec(new Runnable(){
                public void run(){
                    analysis.onError();
                }
            });
        }
        
        /**
         * Trigger.
         */
        private void onFinish() {
            display.asyncExec(new Runnable(){
                public void run(){
                    analysis.onFinish();
                }
            });
        }
        
        /**
         * Returns the thread.
         *
         * @return
         */
        public Thread getThread(){
            return this.thread;
        }
        
        /**
         * Starts this analysis.
         */
        public void start(){
            this.thread = new Thread(this);
            this.thread.setName("StatisticsBuilder");
            this.thread.setDaemon(true);
            this.thread.start();
        }
        
        /**
         * Stops this analysis.
         */
        public synchronized void stop(){
            this.stopped = true;
            this.analysis.stop();
        }
        
        /**
         * Is this analysis stopped.
         *
         * @return
         */
        public synchronized boolean isStopped(){
            return this.stopped;
        }
    }
    
    /** The current worker. */
    private AnalysisWorker worker = null;
    
    /** The current worker. */
    private Display display = null;

    /**
     * Creates a new instance.
     *
     * @param display
     */
    public AnalysisManager(Display display){
        this.display = display;
    }
    
    /**
     * Start a new analysis. Analyses already executing
     * will be canceled.
     *  
     * @param analysis
     */
    public synchronized void start(Analysis analysis) {
        
        // Stop
        stop();
        
        // Start new work
        worker = new AnalysisWorker(analysis);
        worker.start();
    }

    /**
     * Stops all running analysis threads.
     */
    public void stop() {

        // Stop old work
        if (worker != null && !worker.isStopped()) {
            worker.stop();
            try {
                try {
                    worker.getThread().interrupt();
                } catch (SecurityException e) {
                    /* Ignore*/
                }
                if (worker.getThread().isAlive()) {
                    worker.getThread().join();
                }
            } catch (InterruptedException e) {
                /* Ignore*/
            }
            worker = null;
        }
    }
}
