package cz.voho.shitorrent.service;

import cz.voho.shitorrent.exception.CannotLeechException;
import cz.voho.shitorrent.exception.CannotSeedException;
import cz.voho.shitorrent.exception.ChunkNotFoundException;
import cz.voho.shitorrent.exception.ErrorReadingChunkException;
import cz.voho.shitorrent.exception.ResourceNotFoundException;
import cz.voho.shitorrent.model.external.ChunkCrate;
import cz.voho.shitorrent.model.external.InfoForLeechingCrate;
import cz.voho.shitorrent.model.external.InfoForSeedingCrate;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;
import cz.voho.shitorrent.model.external.ResourceMetaSummaryCrate;
import cz.voho.shitorrent.model.internal.Bitmap;
import cz.voho.shitorrent.model.internal.Configuration;
import cz.voho.shitorrent.model.internal.ResourceAvailabilityState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by vojta on 18/01/16.
 */
@Service
public class ResourceManagementService {
    private static final int DEFAULT_CHUNK_SIZE = 1024;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<String, ResourceAvailabilityState> resourceStateByKey;
    private final Object resourcesLock;
    @Autowired
    private Configuration configuration;
    @Autowired
    private BasicInputOutputService basicInputOutputService;

    public ResourceManagementService() {
        resourceStateByKey = new LinkedHashMap<>();
        resourcesLock = new Object();
    }

    public List<ResourceMetaSummaryCrate> getResourceSummaryList() {
        synchronized (resourcesLock) {
            return resourceStateByKey
                    .values()
                    .stream()
                    .map(ResourceAvailabilityState::getSummary)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
    }

    public ResourceMetaDetailCrate getResourceDetail(final String key) throws ResourceNotFoundException {
        synchronized (resourcesLock) {
            final ResourceAvailabilityState state = resourceStateByKey.get(key);

            if (state == null) {
                throw new ResourceNotFoundException(key);
            }

            final Optional<ResourceMetaSummaryCrate> summaryOption = state.getSummary();

            if (!summaryOption.isPresent()) {
                throw new ResourceNotFoundException(key);
            }

            final ResourceMetaSummaryCrate summary = summaryOption.get();
            final ResourceMetaDetailCrate result = new ResourceMetaDetailCrate();
            result.setKey(summary.getKey());
            result.setName(summary.getName());
            result.setFileSize(summary.getFileSize());
            result.setChunkSize(summary.getChunkSize());
            result.setBitmap(state.getAvailabilityBitmap());
            result.setSwarm(state.getAllSeeders());
            return result;
        }
    }

    public ChunkCrate getResourceChunk(final String key, final int chunkIndex) throws ResourceNotFoundException, ChunkNotFoundException, ErrorReadingChunkException {
        synchronized (resourcesLock) {
            final ResourceAvailabilityState state = resourceStateByKey.get(key);

            if (state == null) {
                throw new ResourceNotFoundException(key);
            }

            final Optional<ResourceMetaSummaryCrate> summaryOption = state.getSummary();

            if (!summaryOption.isPresent()) {
                throw new ResourceNotFoundException(key);
            }

            if (!state.isChunkAvailable(chunkIndex)) {
                throw new ChunkNotFoundException(key, chunkIndex);
            }

            final ResourceMetaSummaryCrate summary = summaryOption.get();

            final Optional<Path> backingFileOption = state.getBackingFile();

            if (!backingFileOption.isPresent()) {
                throw new ChunkNotFoundException(key, chunkIndex);
            }

            final Path backingFile = backingFileOption.get();

            final int chunkSize = summary.getChunkSize();
            final byte[] data = basicInputOutputService.readBinaryChunk(backingFile, chunkIndex, chunkSize);

            final ChunkCrate result = new ChunkCrate();
            result.setData(data);
            return result;
        }
    }

    public ResourceAvailabilityState newSeedResource(final InfoForSeedingCrate infoForSeeding) throws CannotSeedException {
        final Path sourcePath;

        try {
            sourcePath = Paths.get(infoForSeeding.getSourcePath());
        } catch (final InvalidPathException e) {
            throw new CannotSeedException(e);
        }

        if (!Files.isRegularFile(sourcePath)) {
            throw new CannotSeedException(sourcePath, "Path is not a directory.");
        }

        if (!Files.isReadable(sourcePath)) {
            throw new CannotSeedException(sourcePath, "Path is not readable.");
        }

        synchronized (resourcesLock) {
            final String key = generateHash(sourcePath);
            final ResourceAvailabilityState resource = new ResourceAvailabilityState(key);
            resource.fillAvailabilityBitmap();
            final ResourceMetaSummaryCrate summary = new ResourceMetaSummaryCrate();
            summary.setKey(key);
            summary.setName(sourcePath.getFileName().toString());
            summary.setFileSize(getTotalSize(sourcePath));
            summary.setChunkSize(DEFAULT_CHUNK_SIZE);
            resource.updateAfterInitialSeeding(summary, sourcePath);
            resourceStateByKey.put(key, resource);
            return resource;
        }
    }

    public ResourceAvailabilityState newLeechResource(final InfoForLeechingCrate infoForLeeching) throws CannotLeechException {
        synchronized (resourcesLock) {
            final String key = infoForLeeching.getResourceKey();

            try {
                Files.createDirectories(configuration.getOutputDirectory());
            } catch (final IOException e) {
                throw new CannotLeechException(e);
            }

            final ResourceAvailabilityState resource = new ResourceAvailabilityState(key);
            resourceStateByKey.put(key, resource);
            return resource;
        }
    }

    public ResourceAvailabilityState updateAfterInitialDownload(final String key, final ResourceMetaDetailCrate detail, final Set<PeerCrate> seeders, final PeerCrate seeder) {
        // TODO check availability, maybe rename
        final ResourceAvailabilityState resource = resourceStateByKey.get(key);
        resource.updateAfterInitialDownload(detail);
        for (PeerCrate peer : detail.getSwarm()) {
            final Bitmap emptyBitmap = new Bitmap(detail.getChunkCount());
            emptyBitmap.markAllUnavailable();
            resource.updatePeerAvailability(peer, emptyBitmap);
        }
        resource.updatePeerAvailability(seeder, new Bitmap(detail.getBitmap()));
        return resource;
    }

    private long getTotalSize(final Path path) throws CannotSeedException {
        try {
            return Files.size(path);
        } catch (final IOException e) {
            throw new CannotSeedException(path, e);
        }
    }

    private String generateHash(final Path path) throws CannotSeedException {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final String absolutePath = path.toAbsolutePath().toString();
            final byte[] digestBytes = digest.digest(absolutePath.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digestBytes);
        } catch (final NoSuchAlgorithmException e) {
            throw new CannotSeedException(path, e);
        }
    }
}
