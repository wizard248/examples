package cz.voho.shitorrent.service.worker;

import cz.voho.shitorrent.exception.ErrorWritingChunkException;
import cz.voho.shitorrent.exception.NoPeerConnectionException;
import cz.voho.shitorrent.model.external.ChunkCrate;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;
import cz.voho.shitorrent.model.internal.Bitmap;
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
            final Optional<Resource> unfinishedResource = selectUnfinishedResource();

            if (!unfinishedResource.isPresent()) {
                log.warn("No unfinished resource.");
                sleepALittle();
                continue;
            }

            if (!unfinishedResource.get().isInitialized()) {
                final Optional<PeerCrate> feasiblePeer = selectFeasiblePeer(unfinishedResource.get());

                if (!feasiblePeer.isPresent()) {
                    log.warn("No feasible peer.");
                    sleepALittle();
                    continue;
                }

                try {
                    initialize(unfinishedResource.get(), feasiblePeer.get());
                } catch (NoPeerConnectionException e) {
                    log.warn("Error while downloading detail.");
                }
            } else {
                final Optional<Integer> unfinishedChunkIndex = selectUnfinishedChunk(unfinishedResource.get());

                if (!unfinishedChunkIndex.isPresent()) {
                    log.warn("No unfinished chunk.");
                    sleepALittle();
                    continue;
                }

                try {
                    final Optional<PeerCrate> feasiblePeer = selectFeasiblePeer(unfinishedResource.get(), unfinishedChunkIndex.get());

                    if (!feasiblePeer.isPresent()) {
                        log.warn("No feasible peer.");
                        sleepALittle();
                        continue;
                    }

                    unfinishedResource.get().markChunkDownloading(unfinishedChunkIndex.get());
                    downloadChunk(unfinishedResource.get(), unfinishedChunkIndex.get(), feasiblePeer.get());
                    unfinishedResource.get().markChunkAvailable(unfinishedChunkIndex.get());
                } catch (final NoPeerConnectionException e) {
                    log.warn("Error while downloading chunk.", e);
                } catch (final ErrorWritingChunkException e) {
                    log.warn("Error while writing chunk.", e);
                } finally {
                    unfinishedResource.get().markChunkNotDownloading(unfinishedChunkIndex.get());
                }
            }
        }
    }

    private Optional<Resource> selectUnfinishedResource() {
        return resourceManagementService.getUnfinishedResources().stream().findFirst();
    }

    private Optional<Integer> selectUnfinishedChunk(final Resource resource) {
        return resource.selectUnfinishedChunk();
    }

    private Optional<PeerCrate> selectFeasiblePeer(final Resource resource) {
        final Set<PeerCrate> peers = resource.getPeers();
        return peers.stream().findAny();
    }

    private Optional<PeerCrate> selectFeasiblePeer(final Resource resource, final int chunkIndex) {
        final Set<PeerCrate> peers = resource.getPeersWithChunk(chunkIndex);
        return peers.stream().findAny();
    }

    private void initialize(final Resource resource, final PeerCrate sourcePeer) throws NoPeerConnectionException {
        log.info("Starting initial download of {} from {}...", resource.getKey(), sourcePeer);

        ResourceMetaDetailCrate detail = otherPeerClientService.downloadResourceDetail(sourcePeer, resource.getKey());
        Path output = configuration.getOutputDirectory().resolve(detail.getName());
        resource.onInitialDownloadCompleted(detail, output);
        resource.updatePeerAvailability(sourcePeer, new Bitmap(detail.getBitmap()));

        log.info("Initial download of {} from {} completed.", resource.getKey(), sourcePeer);
    }

    private void downloadChunk(final Resource resource, final int chunkIndex, final PeerCrate sourcePeer) throws NoPeerConnectionException, ErrorWritingChunkException {
        log.info("Starting download of chunk {} of {} from {}...", chunkIndex, resource.getKey(), sourcePeer);

        final ChunkCrate chunk = otherPeerClientService.downloadChunk(sourcePeer, resource.getKey(), chunkIndex);
        final Optional<Path> targetPath = resource.getBackingFile();

        if (!targetPath.isPresent()) {
            log.warn("No target file.");
            return;
        }

        basicInputOutputService.writeBinaryChunk(targetPath.get(), resource.getFileSize(), chunkIndex, resource.getChunkSize(), chunk.getData());

        log.info("Chunk {} of {} was downloaded from {}.", chunkIndex, resource.getKey(), sourcePeer);
    }

    private void sleepALittle() {
        sleep(3000);
    }
}
