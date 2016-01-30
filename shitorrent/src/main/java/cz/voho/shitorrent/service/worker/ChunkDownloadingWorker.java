package cz.voho.shitorrent.service.worker;

import cz.voho.shitorrent.exception.ErrorWritingChunkException;
import cz.voho.shitorrent.model.external.ChunkCrate;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.internal.Configuration;
import cz.voho.shitorrent.model.internal.Resource;
import cz.voho.shitorrent.service.BasicInputOutputService;
import cz.voho.shitorrent.service.OtherPeerClientService;
import cz.voho.shitorrent.service.ResourceManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by vojta on 20/01/16.
 */
public class ChunkDownloadingWorker implements Worker {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Configuration configuration;
    private final ResourceManagementService resourceManagementService;
    private final OtherPeerClientService otherPeerClientService;
    private final BasicInputOutputService basicInputOutputService;
    private final AtomicBoolean live = new AtomicBoolean(true);

    public ChunkDownloadingWorker(final Configuration configuration, final ResourceManagementService resourceManagementService, final OtherPeerClientService otherPeerClientService, final BasicInputOutputService basicInputOutputService) {
        this.configuration = configuration;
        this.resourceManagementService = resourceManagementService;
        this.otherPeerClientService = otherPeerClientService;
        this.basicInputOutputService = basicInputOutputService;
    }

    @Override
    public void run() {
        while (live.get()) {
            final Optional<Resource> unfinishedResource = selectUnfinishedInitializedResource();

            if (!unfinishedResource.isPresent()) {
                log.warn("No unfinished resource.");
                sleepForResourceChange();
                continue;
            }

            final Optional<Integer> unfinishedChunkIndex = selectUnfinishedChunk(unfinishedResource.get());

            if (!unfinishedChunkIndex.isPresent()) {
                log.warn("No unfinished chunk.");
                sleepForResourceChange();
                continue;
            }

            final Optional<PeerCrate> feasiblePeer = selectFeasiblePeer(unfinishedResource.get(), unfinishedChunkIndex.get());

            if (!feasiblePeer.isPresent()) {
                log.warn("No feasible peer.");
                sleepForSwarmChange();
                continue;
            }

            try {
                downloadChunk(unfinishedResource.get(), unfinishedChunkIndex.get(), feasiblePeer.get());
                unfinishedResource.get().markChunkAvailable(unfinishedChunkIndex.get());
            } catch (final ErrorWritingChunkException e) {
                log.warn("Error while writing chunk.", e);
            } finally {
                unfinishedResource.get().markChunkAsNotDownloading(unfinishedChunkIndex.get());
            }
        }
    }

    private Optional<Resource> selectUnfinishedInitializedResource() {
        return resourceManagementService.getUnfinishedInitializedResources().stream().findFirst();
    }

    private Optional<Integer> selectUnfinishedChunk(final Resource resource) {
        return resource.reserveUnavailableChunkForDownloading();
    }

    private Optional<PeerCrate> selectFeasiblePeer(final Resource resource, final int chunkIndex) {
        final Set<PeerCrate> peers = resource.getPeersWithChunk(chunkIndex);
        return peers.stream().findAny();
    }

    private void downloadChunk(final Resource resource, final int chunkIndex, final PeerCrate sourcePeer) throws ErrorWritingChunkException {
        log.info("Starting download of chunk {} of {} from {}...", chunkIndex, resource.getKey(), sourcePeer);

        final Optional<ChunkCrate> chunk = otherPeerClientService.downloadChunk(sourcePeer, resource.getKey(), chunkIndex);

        if (chunk.isPresent()) {
            final Path targetPath = resource.getBackingFile();
            basicInputOutputService.writeBinaryChunk(targetPath, resource.getFileSize(), chunkIndex, resource.getChunkSize(), chunk.get().getData());
            log.info("Chunk {} of {} was downloaded from {}.", chunkIndex, resource.getKey(), sourcePeer);
        }
    }

    @Override
    public void stop() {
        live.set(false);
    }
}
