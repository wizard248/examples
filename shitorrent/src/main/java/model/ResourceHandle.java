package model;

import java.net.InetAddress;

/**
 * Created by vojta on 12/01/16.
 */
public class ResourceHandle {
    private final InetAddress seederAddress;
    private final String resourceId;
    private final int chunkSize;
    private final long fileSize;

    public ResourceHandle(final InetAddress seederAddress, final String resourceId, final int chunkSize, final long fileSize) {
        this.seederAddress = seederAddress;
        this.resourceId = resourceId;
        this.chunkSize = chunkSize;
        this.fileSize = fileSize;
    }

    public InetAddress getSeederAddress() {
        return seederAddress;
    }

    public String getResourceId() {
        return resourceId;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getNumberOfChunks() {
        return (int) Math.ceil((double) fileSize / (double) chunkSize);
    }

    @Override
    public String toString() {
        return String.format("%s @ %s (%d bytes split by %d)", seederAddress, resourceId, fileSize, chunkSize);
    }
}
