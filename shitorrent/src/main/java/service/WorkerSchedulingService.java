package service;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by vojta on 12/01/16.
 */
public class WorkerSchedulingService {
    private final ScheduledExecutorService executorService;

    public WorkerSchedulingService() {
        executorService = Executors.newScheduledThreadPool(8);
    }

    public void schedule(Runnable worker, Duration interval) {
        long intervalMs = interval.toMillis();
        executorService.scheduleWithFixedDelay(worker, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }
}
