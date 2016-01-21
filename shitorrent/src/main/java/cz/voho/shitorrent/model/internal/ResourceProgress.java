package cz.voho.shitorrent.model.internal;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Created by vojta on 21/01/16.
 */
public class ResourceProgress {
    private final String name;
    private final Long fileSize;
    private final Integer chunkSize;
    private final Integer numChunks;
    private final Bitmap available;
    private final Bitmap downloading;
    private final Path backingFile;

    public ResourceProgress(final String name, final long fileSize, final int chunkSize, final Path backingFile) {
        this.name = name;
        this.fileSize = fileSize;
        this.chunkSize = chunkSize;
        this.numChunks = (int) ((fileSize + chunkSize - 1) / chunkSize);
        this.available = new Bitmap(numChunks);
        this.downloading = new Bitmap(numChunks);
        this.backingFile = backingFile;
    }

    public String getName() {
        return name;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public Integer getNumChunks() {
        return numChunks;
    }

    public Bitmap getAvailable() {
        return available;
    }

    public Bitmap getDownloading() {
        return downloading;
    }

    public Path getBackingFile() {
        return backingFile;
    }

    public void markAllChunksAvailable() {
        available.markAllAvailable();
    }

    public boolean isChunkAvailable(final int chunkIndex) {
        return available.isValidIndex(chunkIndex) && available.isAvailable(chunkIndex);
    }

    public void markChunkDownloading(final int chunkIndex) {
        downloading.markAvailable(chunkIndex);
    }

    public void markChunkNotDownloading(final int chunkIndex) {
        downloading.markUnavailable(chunkIndex);
    }

    public void markChunkAvailable(final int chunkIndex) {
        available.markAvailable(chunkIndex);
    }

    public Optional<Integer> selectUnfinishedChunk() {
        Optional<Integer> result = Optional.empty();

        for (int i = 0; i < numChunks; i++) {
            if (!available.isAvailable(i) && !downloading.isAvailable(i)) {
                result = Optional.of(i);
                break;
            }
        }

        return result;
    }
}
