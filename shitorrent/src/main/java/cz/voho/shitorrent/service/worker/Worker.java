package cz.voho.shitorrent.service.worker;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by vojta on 20/01/16.
 */
public interface Worker extends Runnable {
    AtomicInteger COUNTER = new AtomicInteger(1);

    default Thread runInNewThread() {
        Thread t = new Thread(this, getClass().getSimpleName() + COUNTER.getAndIncrement());
        t.start();
        return t;
    }

    default void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
