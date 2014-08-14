/*
 * ARX: Powerful Data Anonymization
 * Copyright (C) 2012 - 2014 Florian Kohlmayer, Fabian Prasser
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.deidentifier.arx.gui.view.impl.analyze;

import org.eclipse.swt.widgets.Display;


/**
 * This class manages the execution of asynchronous analyses
 * @author Fabian Prasser
 */
public class AnalysisManager {
    
    /**
     * A worker for analyses
     * @author Fabian Prasser
     */
    private class AnalysisWorker implements Runnable {

        /** Stop flag*/
        private volatile boolean stopped = false;
        /** Analysis to perform*/
        private final Analysis analysis;
        /** The thread*/
        private Thread thread;
        
        /** Creates a new instance*/
        private AnalysisWorker(Analysis analysis){
            this.analysis = analysis;
        }
        
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
        
        /** Trigger*/
        private void onInterrupt() {
            display.asyncExec(new Runnable(){
                public void run(){
                    analysis.onInterrupt();
                }
            });
        }
        /** Trigger*/
        private void onError() {
            display.asyncExec(new Runnable(){
                public void run(){
                    analysis.onError();
                }
            });
        }
        /** Trigger*/
        private void onFinish() {
            display.asyncExec(new Runnable(){
                public void run(){
                    analysis.onFinish();
                }
            });
        }
        
        /**
         * Returns the thread
         * @return
         */
        public Thread getThread(){
            return this.thread;
        }
        
        /**
         * Starts this analysis
         */
        public void start(){
            this.thread = new Thread(this);
            this.thread.setName("StatisticsBuilder");
            this.thread.setDaemon(true);
            this.thread.start();
        }
        
        /** Stops this analysis*/
        public synchronized void stop(){
            this.stopped = true;
            this.analysis.stop();
        }
        
        /** Is this analysis stopped*/
        public synchronized boolean isStopped(){
            return this.stopped;
        }
    }
    
    /** The current worker*/
    private AnalysisWorker worker = null;
    /** The current worker*/
    private Display display = null;

    /**
     * Creates a new instance
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
        
        // Start new work
        worker = new AnalysisWorker(analysis);
        worker.start();
    }
}
