package service;

import model.ResourceHandle;
import service.worker.DownloadingWorker;
import service.worker.SwarmUpdatingWorker;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class FrontService {
    private final ResourceDataService resourceDataService;
    private final ResourceMetaDataService resourceMetaDataService;
    private final WorkerSchedulingService workerSchedulingService;

    public FrontService(final ResourceDataService resourceDataService, final ResourceMetaDataService resourceMetaDataService, final WorkerSchedulingService workerSchedulingService) {
        this.resourceDataService = resourceDataService;
        this.resourceMetaDataService = resourceMetaDataService;
        this.workerSchedulingService = workerSchedulingService;
    }

    public void startWorkers() {
        workerSchedulingService.schedule(new SwarmUpdatingWorker(resourceMetaDataService), Duration.ofSeconds(10));
        workerSchedulingService.schedule(new DownloadingWorker(resourceMetaDataService, resourceDataService), Duration.ofSeconds(5));
    }

    public List<String> listResourceIds() {
        return resourceMetaDataService.listResourceIds();
    }

    public List<InetAddress> listPeers(final String resourceId) {
        return resourceMetaDataService.listPeers(resourceId);
    }

    public String listChunks(final String resourceId) {
        return resourceMetaDataService.getAvailabilityFlags(resourceId);
    }

    public byte[] getChunk(final String resourceId, final int chunkIndex) throws IOException {
        final Path path = resourceMetaDataService.getSourcePath(resourceId);
        final int chunkSize = resourceMetaDataService.getChunkSize(resourceId);
        return resourceDataService.getChunk(path, chunkIndex, chunkSize);
    }

    public ResourceHandle seed(final Path fileToShare) throws IOException {
        return resourceMetaDataService.seed(fileToShare);
    }

    public void leech(final ResourceHandle resourceHandle, final Path targetFile) {
        resourceMetaDataService.leech(resourceHandle, targetFile);
    }
}
