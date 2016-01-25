package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.exception.UsingUninitializedResourceException;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Created by vojta on 21/01/16.
 */
public class Resource {
    private final String key;
    private final Map<PeerCrate, Optional<Bitmap>> swarm;
    private Optional<ResourceProgress> progress;

    public Resource(final String key) {
        this.key = key;
        this.swarm = new LinkedHashMap<>();
        this.progress = Optional.empty();
    }

    // OPERATIONS
    // ==========

    public void initializeForLeeching(final ResourceMetaDetailCrate detail, final Path targetPath) {
        this.progress = Optional.of(new ResourceProgress(
                detail.getName(),
                detail.getFileSize(),
                detail.getChunkSize(),
                targetPath
        ));
    }

    public void initializeForSeeding(final Path sourcePath, final long fileSize) {
        this.progress = Optional.of(new ResourceProgress(
                sourcePath.getFileName().toString(),
                fileSize,
                4 * 1024,
                sourcePath
        ));

        this.progress.get().markAllChunksAvailable();
    }

    public void initializeForLeeching(final List<PeerCrate> seeders) {
        mergeToSwarmWithUnknownAvailability(seeders);
    }

    public void mergeToSwarmWithUnknownAvailability(final List<PeerCrate> seeders) {
        seeders.forEach(peer -> mergeToSwarm(peer, Optional.empty()));
    }

    private void mergeToSwarm(final PeerCrate seeder, final Optional<Bitmap> bitmap) {
        this.swarm.merge(seeder, bitmap, new BiFunction<Optional<Bitmap>, Optional<Bitmap>, Optional<Bitmap>>() {
            @Override
            public Optional<Bitmap> apply(final Optional<Bitmap> oldValue, final Optional<Bitmap> newValue) {
                if (!newValue.isPresent()) {
                    return oldValue;
                } else {
                    return newValue;
                }
            }
        });
    }

    public void updateSeederAvailability(final PeerCrate seeder, final Bitmap bitmap) {
        mergeToSwarm(seeder, Optional.of(bitmap));
    }

    public void markChunkDownloading(final int chunkIndex) {
        getProgress().markChunkDownloading(chunkIndex);
    }

    public void markChunkNotDownloading(final int chunkIndex) {
        getProgress().markChunkNotDownloading(chunkIndex);
    }

    public void markChunkAvailable(final int chunkIndex) {
        getProgress().markChunkAvailable(chunkIndex);
    }

    // BASIC QUERIES
    // =============

    public String getKey() {
        return key;
    }

    public String getName() {
        return getProgress().getName();
    }

    public long getFileSize() {
        return getProgress().getFileSize();
    }

    public int getChunkSize() {
        return getProgress().getChunkSize();
    }

    // CHUNK QUERIES
    // =============

    public boolean isChunkAvailable(final int chunkIndex) {
        return getProgress().isChunkAvailable(chunkIndex);
    }

    public Bitmap getAvailabilityBitmap() {
        return getProgress().getAvailable();
    }

    // PEERS QUERIES
    // =============

    public Set<PeerCrate> getPeersWithChunk(final int chunkIndex) {
        return swarm
                .entrySet()
                .stream()
                .filter(e -> e.getValue().orElse(new Bitmap(0)).isAvailable(chunkIndex))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }


    public boolean isComplete() {
        return isInitialized() && getProgress().getAvailable().isAllAvailable();
    }

    public boolean isIncomplete() {
        return !isComplete();
    }

    public Set<PeerCrate> getPeers() {
        return swarm.keySet();
    }

    public Path getBackingFile() {
        return getProgress().getBackingFile();
    }

    public Optional<Integer> selectUnfinishedChunk() {
        return getProgress().selectUnfinishedChunk();
    }

    public boolean isInitialized() {
        return progress.isPresent();
    }

    private ResourceProgress getProgress() {
        return progress.orElseThrow(UsingUninitializedResourceException::new);
    }
}
