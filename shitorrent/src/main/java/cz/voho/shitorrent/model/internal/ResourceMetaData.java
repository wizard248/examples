package cz.voho.shitorrent.model.internal;

import java.nio.file.Path;

/**
 * Created by vojta on 21/01/16.
 */
public class ResourceMetaData {
    private final String name;
    private final Long fileSize;
    private final Integer chunkSize;
    private final Integer numChunks;
    private final Path backingFile;

    public ResourceMetaData(final String name, final long fileSize, final int chunkSize, final Path backingFile) {
        this.name = name;
        this.fileSize = fileSize;
        this.chunkSize = chunkSize;
        this.numChunks = (int) ((fileSize + chunkSize - 1) / chunkSize);
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

    // TODO remove if unused
    public Integer getNumChunks() {
        return numChunks;
    }

    public Path getBackingFile() {
        return backingFile;
    }
}
