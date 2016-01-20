package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;
import cz.voho.shitorrent.model.external.ResourceMetaSummaryCrate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by vojta on 18/01/16.
 */
public class ResourceAvailabilityState {
    private final String key;
    private Optional<ResourceMetaSummaryCrate> data;
    private Optional<Path> backingFile;
    private final ResourceSwarm swarm;
    private Bitmap availableBitmap;
    private Bitmap downloadingBitmap;

    public ResourceAvailabilityState(String key) {
        this.key = key;
        data = Optional.empty();
        backingFile = Optional.empty();
        swarm = new ResourceSwarm();
        availableBitmap = new Bitmap(0);
        downloadingBitmap = new Bitmap(0);
    }

    public void updateAfterInitialDownload(ResourceMetaDetailCrate data) {
        if (!this.data.isPresent()) {
            this.data = Optional.of(data);
        }
        backingFile = Optional.empty();
        swarm.clear();
        availableBitmap = new Bitmap(data.getChunkCount());
        availableBitmap.markAllUnavailable();
        downloadingBitmap = new Bitmap(data.getChunkCount());
        downloadingBitmap.markAllUnavailable();
    }

    public void updateAfterInitialSeeding(final ResourceMetaSummaryCrate data, Path backingFile) {
        if (!this.data.isPresent()) {
            this.data = Optional.of(data);
        }
        this.backingFile = Optional.of(backingFile);
        swarm.clear();
        availableBitmap = new Bitmap(data.getChunkCount());
        availableBitmap.markAllAvailable();
        downloadingBitmap = new Bitmap(data.getChunkCount());
        downloadingBitmap.markAllUnavailable();
    }

    public void updatePeerAvailability(PeerCrate peer, Bitmap bitmap) {
        swarm.updatePeerAvailability(peer, bitmap);
    }

    public Optional<ResourceMetaSummaryCrate> getSummary() {
        return data.map(s -> {
            ResourceMetaSummaryCrate result = new ResourceMetaSummaryCrate();
            result.setKey(s.getKey());
            result.setName(s.getName());
            result.setFileSize(s.getFileSize());
            result.setChunkSize(s.getChunkSize());
            return result;
        });
    }

    public List<PeerCrate> getAllSeeders() {
        return new ArrayList<>(swarm.getPeers());
    }

    public Set<PeerCrate> getSeedersWithChunk(final int chunkIndex) {
        return swarm.getPeersWithChunk(chunkIndex);
    }

    public boolean isDownloading() {
        return downloadingBitmap.hasAnyAvailable();
    }

    public boolean isComplete() {
        return availableBitmap.isAllAvailable();
    }

    public Optional<Integer> getRandomUnavailableChunkIndex() {
        return availableBitmap.getRandomUnavailableIndex();
    }

    public boolean isChunkAvailable(final int chunkIndex) {
        return availableBitmap.isValidIndex(chunkIndex) && availableBitmap.isAvailable(chunkIndex);
    }

    public void markChunkAsAvailable(final int chunkIndex) {
        availableBitmap.markAvailable(chunkIndex);
    }

    public void markChunkDownloadStart(final int chunkIndex) {
        downloadingBitmap.markAvailable(chunkIndex);
    }

    public void markChunkDownloadEnd(final int chunkIndex) {
        downloadingBitmap.markUnavailable(chunkIndex);
    }

    public String getAvailabilityBitmap() {
        return availableBitmap.toString();
    }

    public Optional<Path> getBackingFile() {
        return backingFile;
    }

    public void reserveNewBackingFile(final Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());
        Files.deleteIfExists(targetPath);
        Files.write(targetPath, new byte[0], StandardOpenOption.CREATE_NEW);
        backingFile = Optional.of(targetPath);
    }

    public void fillAvailabilityBitmap() {
        availableBitmap.markAllAvailable();
    }

    @Override
    public String toString() {
        return String.format("%s [%.2f%% done]", key, availableBitmap.getAvailableRatio() * 100.0);
    }
}
