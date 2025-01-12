package com.sihenzhang.autotranslator.translate;

import com.sihenzhang.autotranslator.TranslationManager;
import com.sihenzhang.autotranslator.Utils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TranslationTaskBuffer {
    private Set<TranslationTask> set;
    private final int maxBatchSize;
    private final int schedulerInterval;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledFuture;

    private final Lock lock = new ReentrantLock();

    public TranslationTaskBuffer(int maxBatchSize, int schedulerInterval) {
        this.maxBatchSize = maxBatchSize;
        this.schedulerInterval = schedulerInterval;

        if (this.isBatchExecutionEnabled()) {
            this.set = new LinkedHashSet<>();
            this.scheduler = Executors.newSingleThreadScheduledExecutor(
                    new BasicThreadFactory.Builder().namingPattern("AutoTranslator-Scheduler-%d").daemon(true).build()
            );
            this.resetScheduler();
        }
    }

    public boolean isBatchExecutionEnabled() {
        return maxBatchSize > 1;
    }

    public void add(TranslationTask task) {
        if (this.isBatchExecutionEnabled()) {
            lock.lock();
            try {
                set.add(task);
                if (set.size() >= maxBatchSize) {
                    this.execute();
                    this.resetScheduler();
                }
            } finally {
                lock.unlock();
            }
        } else {
            TranslationManager.translate(task);
        }
    }

    private List<TranslationTask> getTaskBatch() {
        var batch = new ArrayList<TranslationTask>(maxBatchSize);
        lock.lock();
        try {
            var iterator = set.iterator();
            for (var i = 0; i < maxBatchSize && iterator.hasNext(); i++) {
                batch.add(iterator.next());
                iterator.remove();
            }
        } finally {
            lock.unlock();
        }
        return batch;
    }

    private void execute() {
        var batch = this.getTaskBatch();
        if (!batch.isEmpty()) {
            if (batch.size() > 1) {
                TranslationManager.translateBatch(batch);
            } else {
                TranslationManager.translate(batch.getFirst());
            }
        }
    }

    private void resetScheduler() {
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(false);
        }
        scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            while (!set.isEmpty()) {
                this.execute();
            }
        }, schedulerInterval, schedulerInterval, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        if (scheduler != null) {
            Utils.shutdownExecutor(scheduler);
        }
    }
}
