package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by vojta on 21/01/16.
 */
public class Resource {
    private final String key;
    private Optional<String> name;
    private Optional<Long> fileSize;
    private Optional<Integer> chunkSize;
    private Optional<Integer> numChunks;
    private final Map<PeerCrate, Bitmap> swarm;
    private Optional<Bitmap> available;
    private Optional<Bitmap> downloading;
    private Optional<Path> backingFile;

    public Resource(String key) {
        this.key = key;
        this.name = Optional.empty();
        this.fileSize = Optional.empty();
        this.chunkSize = Optional.empty();
        this.numChunks = Optional.empty();
        this.swarm = new LinkedHashMap<>();
        this.available = Optional.empty();
        this.downloading = Optional.empty();
        backingFile = Optional.empty();
    }

    public void onInitialDownloadCompleted(ResourceMetaDetailCrate detail, Path targetPath) {
        this.name = Optional.of(detail.getName());
        this.fileSize = Optional.of(detail.getFileSize());
        this.chunkSize = Optional.of(detail.getChunkSize());
        this.numChunks = Optional.of((int) Math.ceil((double) detail.getFileSize() / (double) detail.getChunkSize()));
        mergeToSwarmEmptyBitmap(detail.getSwarm(), numChunks.get());
        this.available = Optional.of(new Bitmap(numChunks.get()));
        this.downloading = Optional.of(new Bitmap(numChunks.get()));
        backingFile = Optional.of(targetPath);
    }

    public void onSeeding(final Path sourcePath, final long fileSize) {
        this.name = Optional.of(sourcePath.getFileName().toString());
        this.fileSize = Optional.of(fileSize);
        this.chunkSize = Optional.of(4096);
        this.numChunks = Optional.of((int) Math.ceil((double) this.fileSize.get() / (double) this.chunkSize.get()));
        this.swarm.clear();
        this.available = Optional.of(new Bitmap(numChunks.get()));
        this.available.get().markAllAvailable();
        this.downloading = Optional.of(new Bitmap(numChunks.get()));
        backingFile = Optional.of(sourcePath);
    }

    public void onLeeching(final List<PeerCrate> seeders) {
        this.swarm.clear();
        seeders.forEach(e -> this.swarm.put(e, new Bitmap(0)));
    }

    public void updatePeerAvailability(final PeerCrate sourcePeer, final Bitmap bitmap) {
        mergeToSwarm(sourcePeer, bitmap);
    }

    private void mergeToSwarmEmptyBitmap(final List<PeerCrate> swarm, int numChunks) {
        swarm.forEach(peer -> mergeToSwarm(peer, new Bitmap(numChunks)));
    }

    private void mergeToSwarm(PeerCrate peer, Bitmap bitmap) {
        this.swarm.put(peer, bitmap);
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name.orElse("N/A");
    }

    public long getFileSize() {
        return fileSize.orElse(-1L);
    }

    public int getChunkSize() {
        return chunkSize.orElse(-1);
    }

    public boolean isIncomplete() {
        return !isComplete();
    }

    public boolean isChunkAvailable(final int chunkIndex) {
        return isInitialized() && available.get().isValidIndex(chunkIndex) && available.get().isAvailable(chunkIndex);
    }

    public Set<PeerCrate> getPeersWithChunk(final int chunkIndex) {
        return swarm
                .entrySet()
                .stream()
                .filter(e -> e.getValue().isAvailable(chunkIndex))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Optional<Bitmap> getAvailabilityBitmap() {
        return available;
    }

    public void markChunkDownloading(final int chunkIndex) {
        if (isInitialized()) {
            downloading.get().markAvailable(chunkIndex);
        }
    }

    public void markChunkNotDownloading(final int chunkIndex) {
        if (isInitialized()) {
            downloading.get().markUnavailable(chunkIndex);
        }
    }

    public void markChunkAvailable(final int chunkIndex) {
        if (isInitialized()) {
            available.get().markAvailable(chunkIndex);
        }
    }

    public Optional<Integer> selectUnfinishedChunk() {
        Optional<Integer> result = Optional.empty();

        if (isInitialized()) {
            // TODO randomize a little

            for (int i = 0; i < numChunks.get(); i++) {
                if (!available.get().isAvailable(i) && !downloading.get().isAvailable(i)) {
                    result = Optional.of(i);
                    break;
                }
            }
        }

        if (result.isPresent()) {
            downloading.get().markAvailable(result.get());
        }

        return result;
    }

    public boolean isComplete() {
        return isInitialized() && available.get().isAllAvailable();
    }

    public Set<PeerCrate> getPeers() {
        return swarm.keySet();
    }

    public Optional<Path> getBackingFile() {
        return backingFile;
    }

    public boolean isInitialized() {
        // TODO better flagging
        return name.isPresent();
    }
}
