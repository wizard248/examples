package cz.voho.shitorrent.model.internal;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vojta on 30/01/16.
 */
public class ResourceActiveDownloading {
    private final Set<Integer> downloadingChunkIndexes;

    public ResourceActiveDownloading() {
        this.downloadingChunkIndexes = new HashSet<>();
    }

    public void markAsDownloading(int index) {
        downloadingChunkIndexes.add(index);
    }

    public boolean isDownloading(int index) {
        return downloadingChunkIndexes.contains(index);
    }

    public void markAsNotDownloading(final int index) {
        downloadingChunkIndexes.remove(index);
    }
}
