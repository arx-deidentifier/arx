package org.deidentifier.arx.framework.check.groupify;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.deidentifier.arx.common.ThreadPoolMainWorker;
import org.deidentifier.arx.framework.lattice.Transformation;
import org.deidentifier.arx.metric.InformationLossWithBound;
import org.deidentifier.arx.metric.Metric;

class HashGroupifyDistributionMultithreaded extends HashGroupifyDistribution {
    
    /** The thread pool */
    private ExecutorService threadPool;
    
    /**
     * Creates a new multithreaded instance.
     * 
     * @param metric
     * @param transformation
     * @param hashTableFirstEntry
     * @param threadPool
     */
    HashGroupifyDistributionMultithreaded(Metric<?> metric, Transformation transformation, HashGroupifyEntry entry, ExecutorService threadPool) {
        this.threadPool = threadPool;
        init(metric, transformation, entry);
    }
    
    /**
     * Parallelized version.
     */
    @Override
    protected Map<HashGroupifyEntry, InformationLossWithBound<?>> createCache(final List<HashGroupifyEntry> list, final Transformation transformation, final Metric<?> metric) {
        final Map<HashGroupifyEntry, InformationLossWithBound<?>> cache = new ConcurrentHashMap<HashGroupifyEntry, InformationLossWithBound<?>>();
        final CountDownLatch latch = new CountDownLatch(list.size());
        
        for (int i = 0; i < list.size(); i++) {
            final int idx = i;
            this.threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    HashGroupifyEntry entry = list.get(idx);
                    InformationLossWithBound<?> loss = metric.getInformationLoss(transformation, entry);
                    cache.put(entry, loss);
                    latch.countDown();
                }
            });
        }
        
        if (this.threadPool instanceof ThreadPoolMainWorker) {
            ((ThreadPoolMainWorker) this.threadPool).enter();
        }
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            // Do nothing.
        }
        return cache;
    }
    
}
