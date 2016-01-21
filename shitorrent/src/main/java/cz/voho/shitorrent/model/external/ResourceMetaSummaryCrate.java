package cz.voho.shitorrent.model.external;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by vojta on 13/01/16.
 */
public class ResourceMetaSummaryCrate {
    private String key;
    private String name;
    private long fileSize;
    private int chunkSize;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(final long fileSize) {
        this.fileSize = fileSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(final int chunkSize) {
        this.chunkSize = chunkSize;
    }
}
