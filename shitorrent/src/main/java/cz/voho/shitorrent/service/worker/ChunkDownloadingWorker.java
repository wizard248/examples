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

/**
 * Created by vojta on 20/01/16.
 */
public class ChunkDownloadingWorker implements Worker {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Configuration configuration;
    private final ResourceManagementService resourceManagementService;
    private final OtherPeerClientService otherPeerClientService;
    private final BasicInputOutputService basicInputOutputService;

    public ChunkDownloadingWorker(final Configuration configuration, final ResourceManagementService resourceManagementService, final OtherPeerClientService otherPeerClientService, final BasicInputOutputService basicInputOutputService) {
        this.configuration = configuration;
        this.resourceManagementService = resourceManagementService;
        this.otherPeerClientService = otherPeerClientService;
        this.basicInputOutputService = basicInputOutputService;
    }

    @Override
    public void run() {
        while (true) {
            final Optional<Resource> unfinishedResource = selectUnfinishedInitializedResource();

            if (!unfinishedResource.isPresent()) {
                log.warn("No unfinished resource.");
                sleepALittle();
                continue;
            }

            final Optional<Integer> unfinishedChunkIndex = selectUnfinishedChunk(unfinishedResource.get());

            if (!unfinishedChunkIndex.isPresent()) {
                log.warn("No unfinished chunk.");
                sleepALittle();
                continue;
            }

            final Optional<PeerCrate> feasiblePeer = selectFeasiblePeer(unfinishedResource.get(), unfinishedChunkIndex.get());

            if (!feasiblePeer.isPresent()) {
                log.warn("No feasible peer.");
                sleepALittle();
                continue;
            }

            try {
                unfinishedResource.get().markChunkDownloading(unfinishedChunkIndex.get());
                downloadChunk(unfinishedResource.get(), unfinishedChunkIndex.get(), feasiblePeer.get());
                unfinishedResource.get().markChunkAvailable(unfinishedChunkIndex.get());
            } catch (final NoPeerConnectionException e) {
                log.warn("Error while downloading chunk.", e);
                otherPeerClientService.markPeerAsNonResponsive(feasiblePeer.get());
            } catch (final ErrorWritingChunkException e) {
                log.warn("Error while writing chunk.", e);
            } finally {
                unfinishedResource.get().markChunkNotDownloading(unfinishedChunkIndex.get());
            }
        }
    }

    private Optional<Resource> selectUnfinishedInitializedResource() {
        return resourceManagementService.getUnfinishedInitializedResources().stream().findFirst();
    }

    private Optional<Integer> selectUnfinishedChunk(final Resource resource) {
        return resource.selectUnfinishedChunk();
    }

    private Optional<PeerCrate> selectFeasiblePeer(final Resource resource, final int chunkIndex) {
        final Set<PeerCrate> peers = resource.getPeersWithChunk(chunkIndex);
        return peers.stream().findAny();
    }

    private void downloadChunk(final Resource resource, final int chunkIndex, final PeerCrate sourcePeer) throws ErrorWritingChunkException {
        log.info("Starting download of chunk {} of {} from {}...", chunkIndex, resource.getKey(), sourcePeer);

        final ChunkCrate chunk = otherPeerClientService.downloadChunk(sourcePeer, resource.getKey(), chunkIndex);
        final Path targetPath = resource.getBackingFile();
        basicInputOutputService.writeBinaryChunk(targetPath, resource.getFileSize(), chunkIndex, resource.getChunkSize(), chunk.getData());

        log.info("Chunk {} of {} was downloaded from {}.", chunkIndex, resource.getKey(), sourcePeer);
    }

    private void sleepALittle() {
        sleep(3000);
    }
}
