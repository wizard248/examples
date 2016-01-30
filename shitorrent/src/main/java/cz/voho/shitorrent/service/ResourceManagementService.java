package cz.voho.shitorrent.service;

import cz.voho.shitorrent.exception.CannotLeechException;
import cz.voho.shitorrent.exception.CannotSeedException;
import cz.voho.shitorrent.exception.ChunkNotFoundException;
import cz.voho.shitorrent.exception.ErrorReadingChunkException;
import cz.voho.shitorrent.exception.ResourceAlreadyPresentException;
import cz.voho.shitorrent.exception.ResourceNotFoundException;
import cz.voho.shitorrent.model.external.InfoForLeechingCrate;
import cz.voho.shitorrent.model.external.InfoForSeedingCrate;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.internal.Configuration;
import cz.voho.shitorrent.model.internal.Resource;
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
import java.util.stream.Collectors;

/**
 * Created by vojta on 18/01/16.
 */
@Service
public class ResourceManagementService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<String, Resource> resourceByKey;
    private final Object resourcesLock;
    @Autowired
    private Configuration configuration;
    @Autowired
    private BasicInputOutputService basicInputOutputService;

    public ResourceManagementService() {
        resourceByKey = new LinkedHashMap<>();
        resourcesLock = new Object();
    }

    public List<Resource> getInitializedResources() {
        synchronized (resourcesLock) {
            return resourceByKey
                    .values()
                    .stream()
                    .filter(Resource::isInitialized)
                    .collect(Collectors.toList());
        }
    }

    public List<Resource> getUnfinishedResources() {
        synchronized (resourcesLock) {
            return resourceByKey
                    .values()
                    .stream()
                    .filter(Resource::isIncomplete)
                    .collect(Collectors.toList());
        }
    }

    public List<Resource> getUnfinishedInitializedResources() {
        synchronized (resourcesLock) {
            return resourceByKey
                    .values()
                    .stream()
                    .filter(Resource::isIncomplete)
                    .filter(Resource::isInitialized)
                    .collect(Collectors.toList());
        }
    }

    public Optional<Resource> getResource(final String key) {
        synchronized (resourcesLock) {
            return Optional.ofNullable(resourceByKey.get(key));
        }
    }

    public Optional<byte[]> getResourceChunk(final String key, final int chunkIndex) throws ResourceNotFoundException, ChunkNotFoundException, ErrorReadingChunkException {
        synchronized (resourcesLock) {
            final Resource state = resourceByKey.get(key);

            if (state == null) {
                throw new ResourceNotFoundException(key);
            }

            if (!state.isChunkAvailable(chunkIndex)) {
                throw new ChunkNotFoundException(key, chunkIndex);
            }

            final Path backingFile = state.getBackingFile();
            final int chunkSize = state.getChunkSize();
            final long backingFileSize = state.getFileSize();
            final byte[] data = basicInputOutputService.readBinaryChunk(backingFile, backingFileSize, chunkIndex, chunkSize);

            return Optional.of(data);
        }
    }

    public Resource newSeedResource(final InfoForSeedingCrate infoForSeeding) throws CannotSeedException, ResourceAlreadyPresentException {
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

            if (!resourceByKey.containsKey(key)) {
                final PeerCrate localPeer = configuration.getLocalPeer();
                final Resource resource = new Resource(key, localPeer);
                resource.initializeForSeeding(sourcePath, getTotalSize(sourcePath));
                resource.updateSeederFullyAvailable(localPeer);
                resourceByKey.put(key, resource);
                return resource;
            } else {
                throw new ResourceAlreadyPresentException(key);
            }
        }
    }

    public Resource newLeechResource(final InfoForLeechingCrate infoForLeeching) throws CannotLeechException, ResourceAlreadyPresentException {
        synchronized (resourcesLock) {
            final String key = infoForLeeching.getResourceKey();

            if (!resourceByKey.containsKey(key)) {
                final PeerCrate localPeer = configuration.getLocalPeer();
                final Resource resource = new Resource(key, localPeer);
                resource.initializeForLeeching(infoForLeeching.getSeeders());
                resourceByKey.put(key, resource);
                return resource;
            } else {
                throw new ResourceAlreadyPresentException(key);
            }
        }
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
