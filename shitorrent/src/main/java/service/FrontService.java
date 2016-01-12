package service;

import model.ResourceHandle;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.List;

public class FrontService {
    private final ResourceDataService resourceDataService;
    private final ResourceMetaDataService resourceMetaDataService;

    public FrontService(final ResourceDataService resourceDataService, final ResourceMetaDataService resourceMetaDataService) {
        this.resourceDataService = resourceDataService;
        this.resourceMetaDataService = resourceMetaDataService;
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
