package service.worker;

import service.ResourceDataService;
import service.ResourceMetaDataService;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

/**
 * Created by vojta on 12/01/16.
 */
public class DownloadingWorker implements Runnable {
    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private final ResourceMetaDataService resourceMetaDataService;
    private final ResourceDataService resourceDataService;
    private final ExecutorService executorService;
    private final Semaphore concurrentDownloadSemaphore;

    private final Object metaLock = new Object();

    public DownloadingWorker(final ResourceMetaDataService resourceMetaDataService, final ResourceDataService resourceDataService) {
        this.resourceMetaDataService = resourceMetaDataService;
        this.resourceDataService = resourceDataService;
        this.executorService = Executors.newFixedThreadPool(4);
        this.concurrentDownloadSemaphore = new Semaphore(4);
    }

    private void download(final String resourceId, final int chunkIndex) {
        LOGGER.info("Downloading " + resourceId + " . " + chunkIndex);

        byte data[] = new byte[256]; // TODO actual download

        try {
            Thread.sleep(new Random().nextInt(3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (metaLock) {
            Path outputPath = resourceMetaDataService.getTargetPath(resourceId);
            resourceDataService.writeChunk(outputPath, chunkIndex, data.length, data);
            resourceMetaDataService.markChunkCompleted(resourceId, chunkIndex);
        }
    }

    @Override
    public void run() {
        while (concurrentDownloadSemaphore.tryAcquire()) {
            LOGGER.info("New download thread will be started.");

            synchronized (metaLock) {
                Optional<String> resourceId = resourceMetaDataService.getRandomUnfinishedResourceId();

                if (resourceId.isPresent()) {
                    Optional<Integer> chunkIndex = resourceMetaDataService.getRandomUnfinishedChunk(resourceId.get());

                    if (chunkIndex.isPresent()) {
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    download(resourceId.get(), chunkIndex.get());
                                } finally {
                                    concurrentDownloadSemaphore.release();
                                }
                            }
                        });
                    } else {
                        LOGGER.info("No unfinished chunk.");
                    }
                } else {
                    LOGGER.info("No unfinished resources.");
                }
            }
        }

        LOGGER.info("Already all download threads running.");
    }


}
