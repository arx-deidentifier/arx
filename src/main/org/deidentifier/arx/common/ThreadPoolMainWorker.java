package org.deidentifier.arx.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class ThreadPoolMainWorker extends AbstractExecutorService {
    
    /**
     * Class holding the thread instance.
     *
     */
    final class PooledThread implements Runnable {
        
        /** Is this thread still alive? */
        private boolean      isClosed;
        /** Reference to the own thread object. */
        private final Thread self;
                             
        /**
         * Constructor.
         */
        public PooledThread() {
            this.isClosed = false;
            this.self = new Thread(this);
            this.self.setDaemon(true);
            this.self.start();
        }
        
        /**
         * Shutdown the thread.
         */
        public synchronized void close() {
            this.isClosed = true;
            this.self.interrupt(); // break pool thread out of take() call.
        }
        
        /**
         * Interrupt the thread.
         */
        public void interruptCurrentRunningTask() {
            this.self.interrupt();
        }
        
        /**
         * Returns true if the thread is shutdown.
         * @return
         */
        public synchronized boolean isClosed() {
            return this.isClosed;
        }
        
        @Override
        public void run() {
            while (!isClosed()) {
                runNextJobWaiting();
                // Clear possible interrupted state
                Thread.interrupted();
            }
        }
        
    }
    
    /** The job queue */
    private final BlockingQueue<Runnable> queue;
    /** The pool */
    private final PooledThread[]          threads;
    /** Is the pool shutdown? */
    private boolean                       shutdown;
                                          
    /**
     * Constructor
     * @param numThreads
     */
    public ThreadPoolMainWorker(final int numThreads) {
        this.shutdown = false;
        this.queue = new LinkedBlockingQueue<>();
        // Create n-1 threads
        this.threads = new PooledThread[numThreads - 1];
        for (int i = 0; i < this.threads.length; i++) {
            this.threads[i] = new PooledThread();
        }
    }
    
    @Override
    public synchronized boolean awaitTermination(long paramLong, TimeUnit paramTimeUnit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Allows the main thread to enter the execution of the queued tasks.
     * It blocks until all queued task are finished or the execution is interrupted.
     */
    public void enter() {
        while (!this.queue.isEmpty() && !Thread.interrupted()) {
            final Runnable runnable = this.queue.poll();
            if (runnable != null) {
                runnable.run();
            }
        }
    }
    
    @Override
    public void execute(Runnable command) {
        if (this.shutdown) {
            throw new RejectedExecutionException();
        }
        this.queue.add(command);
    }
    
    @Override
    public synchronized boolean isShutdown() {
        return this.shutdown;
    }
    
    @Override
    public synchronized boolean isTerminated() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    /**
     * Shutdown the thread pool immediately and interrupts all running tasks.
     * BEWARE: same behavior than shutdownNow.
     */
    public synchronized void shutdown() {
        shutdownNow();
    }
    
    @Override
    public synchronized List<Runnable> shutdownNow() {
        // Do not accept new tasks.
        this.shutdown = true;
        List<Runnable> waitingJobs = new ArrayList<>();
        this.queue.drainTo(waitingJobs);
        
        // Shutdown all threads in pool
        for (int i = 0; i < this.threads.length; i++) {
            this.threads[i].close();
            this.threads[i] = null;
        }
        return waitingJobs;
    }
    
    /**
     * Executes the next job in queue, waiting if no jobs are available.
     */
    private final void runNextJobWaiting() {
        try {
            final Runnable runnable = this.queue.take();
            runnable.run();
        } catch (InterruptedException e) {
            // Do nothing.
        }
    }
    
}
