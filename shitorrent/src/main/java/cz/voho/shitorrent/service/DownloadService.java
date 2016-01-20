package cz.voho.shitorrent.service;

import cz.voho.shitorrent.exception.ErrorWritingChunkException;
import cz.voho.shitorrent.model.external.ChunkCrate;
import cz.voho.shitorrent.model.external.InfoForLeechingCrate;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;
import cz.voho.shitorrent.model.internal.Configuration;
import cz.voho.shitorrent.model.internal.ResourceAvailabilityState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by vojta on 18/01/16.
 */
@Service
public class DownloadService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Configuration configuration;
    @Autowired
    private BasicInputOutputService basicInputOutputService;
    @Autowired
    private ResourceManagementService resourceManagementService;
    @Autowired
    private OtherPeerClientService otherPeerClientService;

    private ScheduledExecutorService scheduledExecutorService;
    private Semaphore concurrentDownloadCounter;

    @PostConstruct
    public void initialize() throws IOException {
        Files.createDirectories(configuration.getOutputDirectory());
        scheduledExecutorService = Executors.newScheduledThreadPool(configuration.getSchedulerThreads());
        concurrentDownloadCounter = new Semaphore(configuration.getMaxNumberOfConcurrentDownloads());
    }

    public void scheduleLeeching(final InfoForLeechingCrate infoForLeechingCrate) {
        final String resourceKey = infoForLeechingCrate.getResourceKey();
        final List<PeerCrate> seeders = infoForLeechingCrate.getSeeders();
        scheduleInitialDownload(resourceKey, new LinkedHashSet<>(seeders));
    }

    private void scheduleInitialDownload(final String key, final Set<PeerCrate> seeders) {
        scheduleSooner(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("Initial download of {}...", key);
                    final Optional<PeerCrate> seeder = chooseBestPeer(seeders);
                    if (!seeder.isPresent()) {
                        log.warn("No seeder found.");
                        return;
                    }
                    final ResourceMetaDetailCrate detail = otherPeerClientService.downloadResourceDetail(seeder.get(), key);
                    log.info("Initial detail downloaded: {}", detail);
                    final ResourceAvailabilityState state = resourceManagementService.updateAfterInitialDownload(key, detail, seeders, seeder.get());
                    Path targetPath = configuration.getOutputDirectory().resolve(detail.getName());
                    state.reserveNewBackingFile(targetPath);
                    log.info("State updated. Scheduling more tasks...");
                    scheduleChunkDownloads(state);
                    scheduleSwarmExtension(state);
                    scheduleSwarmReduction(state);
                } catch (final Exception e) {
                    // never mind, try again
                    log.error("Error during the initial download.", e);
                    scheduleInitialDownload(key, seeders);
                }
            }
        });
    }

    private void scheduleChunkDownloads(final ResourceAvailabilityState resource) {
        scheduleSooner(new Runnable() {
            @Override
            public void run() {
                while (concurrentDownloadCounter.tryAcquire()) {
                    createDownloadTask(resource);
                }

                scheduleChunkDownloads(resource);
            }
        });
    }

    private void createDownloadTask(final ResourceAvailabilityState resource) {
        scheduleSooner(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!resource.isComplete()) {
                        log.info("Creating download task for {}...", resource);

                        final Optional<Integer> randomChunkIndexOption = resource.getRandomUnavailableChunkIndex();
                        if (!randomChunkIndexOption.isPresent()) {
                            log.warn("No random chunk selected.");
                            return;
                        }

                        final Set<PeerCrate> peers = resource.getSeedersWithChunk(randomChunkIndexOption.get());
                        log.info("Available peers: {}", peers);
                        final Optional<PeerCrate> bestPeer = chooseBestPeer(peers);
                        if (!bestPeer.isPresent()) {
                            log.warn("No seeder peer was selected.");
                            return;
                        }

                        log.info("Downloading chunk data...");
                        final ChunkCrate chunk = otherPeerClientService.downloadChunk(bestPeer.get(), resource.getSummary().get().getKey(), randomChunkIndexOption.get());
                        final byte[] data = chunk.getData();

                        final Optional<Path> backingFileOption = resource.getBackingFile();

                        if (!backingFileOption.isPresent()) {
                            log.warn("No backing file detected.");
                            return;
                        }

                        final Path backingFile = backingFileOption.get();
                        final int randomChunkIndex = randomChunkIndexOption.get();
                        final int chunkSize = resource.getSummary().get().getChunkSize();
                        log.info("Downloaded chunk {} ({} bytes) and writing to {}...", randomChunkIndex, chunkSize, backingFile);
                        basicInputOutputService.writeBinaryChunk(backingFile, randomChunkIndex, chunkSize, data);

                        log.info("Marking chunk {} as complete...", randomChunkIndex);
                        resource.markChunkAsAvailable(randomChunkIndex);
                    } else {
                        log.info("Resource {} is already complete.", resource);
                    }
                } catch (final ErrorWritingChunkException e) {
                    log.error("Error while writing chunk.", e);
                } catch (Exception e) {
                    log.error("Internal exception while writing chunk.", e);
                } finally {
                    concurrentDownloadCounter.release();
                }
            }
        });
    }

    private void scheduleSwarmReduction(final ResourceAvailabilityState resource) {
        scheduleLater(new Runnable() {
            @Override
            public void run() {
                // TODO nothing so far
                log.debug("Swarm reduction...");
            }
        });
    }

    private void scheduleSwarmExtension(final ResourceAvailabilityState resource) {
        scheduleLater(new Runnable() {
            @Override
            public void run() {
                // TODO nothing so far
                log.debug("Swarm extension...");
            }
        });
    }

    private void scheduleSooner(final Runnable runnable) {
        scheduledExecutorService.schedule(runnable, 100, TimeUnit.MILLISECONDS);
    }

    private void scheduleLater(final Runnable runnable) {
        scheduledExecutorService.schedule(runnable, 10, TimeUnit.SECONDS);
    }

    private Optional<PeerCrate> chooseBestPeer(final Set<PeerCrate> seeders) {
        if (seeders.isEmpty()) {
            return Optional.empty();
        } else {
            // TODO randomly or the fastest or whatever
            return Optional.of(seeders.iterator().next());
        }
    }
}
