package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.exception.UsingUninitializedResourceException;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Created by vojta on 21/01/16.
 */
public class Resource {
    private final String key;
    private final PeerCrate localPeer;
    private final Object lock;
    private Optional<ResourceMetaData> progress;
    private final ResourceAvailabilityOnPeers availability;
    private final ResourceActiveDownloading downloading;

    public Resource(final String key, final PeerCrate localPeer) {
        this.key = key;
        this.localPeer = localPeer;
        lock = new Object();
        this.progress = Optional.empty();
        this.availability = new ResourceAvailabilityOnPeers();
        this.downloading = new ResourceActiveDownloading();
    }

    // ==========

    public void initializeForLeeching(final ResourceMetaDetailCrate detail, final Path targetPath) {
        synchronized (lock) {
            this.progress = Optional.of(new ResourceMetaData(
                    detail.getName(),
                    detail.getFileSize(),
                    detail.getChunkSize(),
                    targetPath
            ));

            availability.mergeWithSeedersUnknownAvailability(detail.getSwarm());

            Bitmap bitmap = new Bitmap(this.progress.get().getNumChunks());
            bitmap.markAllUnavailable();
            availability.mergeSeederAvailability(localPeer, bitmap);
        }
    }

    public void initializeForLeeching(final List<PeerCrate> seeders) {
        synchronized (lock) {
            this.getAvailability().mergeWithSeedersUnknownAvailability(seeders);
        }
    }

    public void initializeForSeeding(final Path sourcePath, final long fileSize) {
        synchronized (lock) {
            this.progress = Optional.of(new ResourceMetaData(
                    sourcePath.getFileName().toString(),
                    fileSize,
                    4 * 1024,
                    sourcePath
            ));

            Bitmap bitmap = new Bitmap(this.progress.get().getNumChunks());
            bitmap.markAllAvailable();
            availability.mergeSeederAvailability(localPeer, bitmap);
        }
    }

    public void mergeToSwarmWithUnknownAvailability(final List<PeerCrate> seeders) {
        synchronized (lock) {
            getAvailability().mergeWithSeedersUnknownAvailability(seeders);
        }
    }

    public void updateSeederAvailability(final PeerCrate seeder, final Bitmap bitmap) {
        synchronized (lock) {
            getAvailability().mergeSeederAvailability(seeder, bitmap);
        }
    }

    // =============

    public String getKey() {
        return key;
    }

    public String getName() {
        return getMetaData().getName();
    }

    public long getFileSize() {
        return getMetaData().getFileSize();
    }

    public int getChunkSize() {
        return getMetaData().getChunkSize();
    }

    public void markChunkAvailable(final int chunkIndex) {
        synchronized (lock) {
            getAvailability().markAvailable(localPeer, chunkIndex);
        }
    }

    public boolean isChunkAvailable(final int chunkIndex) {
        synchronized (lock) {
            return getAvailability().isChunkAvailable(localPeer, chunkIndex);
        }
    }

    public Bitmap getLocalAvailability() {
        synchronized (lock) {
            return getAvailability().getAvailabilityBitmap(localPeer);
        }
    }

    public void updateSeederFullyAvailable(final PeerCrate localPeer) {
        synchronized (lock) {
            getAvailability().markAllAvailable(localPeer, getMetaData().getNumChunks());
        }
    }

    // =============

    public Optional<Integer> reserveUnavailableChunkForDownloading() {
        Optional<Integer> result = Optional.empty();

        synchronized (lock) {
            Bitmap bitmap = getLocalAvailability();

            if (!bitmap.isAllAvailable()) {
                Random rand = new Random();
                while (!result.isPresent()) {
                    int r = rand.nextInt(bitmap.getSize());
                    if (!isChunkAvailable(r) && !downloading.isDownloading(r)) {
                        result = Optional.of(r);
                    }
                }
            }

            result.ifPresent(i -> downloading.markAsDownloading(i));
        }

        return result;
    }

    public void markChunkAsNotDownloading(int index) {
        synchronized (lock) {
            downloading.markAsNotDownloading(index);
        }
    }

    public Set<PeerCrate> getPeersWithChunk(final int chunkIndex) {
        synchronized (lock) {
            return getAvailability().getPeersWithChunk(chunkIndex);
        }
    }

    public boolean isComplete() {
        synchronized (lock) {
            return getAvailability().areAllChunksAvailable(localPeer);
        }
    }

    public boolean isIncomplete() {
        return !isComplete();
    }

    public Set<PeerCrate> getPeers() {
        synchronized (lock) {
            return getAvailability().getAllPeers();
        }
    }

    public Path getBackingFile() {
        return getMetaData().getBackingFile();
    }

    public boolean isInitialized() {
        synchronized (lock) {
            return progress.isPresent();
        }
    }

    private ResourceMetaData getMetaData() {
        synchronized (lock) {
            return progress.orElseThrow(UsingUninitializedResourceException::new);
        }
    }

    private ResourceAvailabilityOnPeers getAvailability() {
        synchronized (lock) {
            // TODO remove method
            return availability;
        }
    }
}
