package service;

import model.ResourceHandle;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by vojta on 12/01/16.
 */
public class ResourceMetaDataService {
    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private final Object lock;
    private final List<ResourceHandle> resourceHandles;
    private final Map<ResourceHandle, Path> resourceToSourceFile;
    private final Map<ResourceHandle, Path> resourceToTargetFile;
    private final Map<ResourceHandle, BitSet> resourceToBitSet;
    private final Map<ResourceHandle, List<InetAddress>> resourceToSwarm;

    public ResourceMetaDataService() {
        lock = new Object();
        resourceHandles = new LinkedList<>();
        resourceToSourceFile = new HashMap<>();
        resourceToTargetFile = new HashMap<>();
        resourceToBitSet = new HashMap<>();
        resourceToSwarm = new HashMap<>();
    }

    public List<String> listResourceIds() {
        synchronized (lock) {
            return resourceHandles.stream().map(ResourceHandle::getResourceId).collect(Collectors.toList());
        }
    }

    public List<InetAddress> listPeers(final String resourceId) {
        synchronized (lock) {
            return resourceToSwarm.get(findById(resourceId));
        }
    }

    public Path getSourcePath(final String resourceId) {
        synchronized (lock) {
            return resourceToSourceFile.get(findById(resourceId));
        }
    }

    public int getChunkSize(final String resourceId) {
        synchronized (lock) {
            return findById(resourceId).getChunkSize();
        }
    }

    public String getAvailabilityFlags(final String resourceId) {
        synchronized (lock) {
            final ResourceHandle handle = findById(resourceId);
            final BitSet bits = resourceToBitSet.get(handle);
            final int numChunks = handle.getNumberOfChunks();
            final StringBuilder buffer = new StringBuilder(numChunks);
            for (int i = 0; i < numChunks; i++) {
                buffer.append(bits.get(i) ? '1' : '0');
            }
            return buffer.toString();
        }
    }

    public ResourceHandle seed(final Path fileToShare) throws IOException {
        synchronized (lock) {
            assurePathIsUnused(fileToShare);

            final ResourceHandle resourceToSeed = new ResourceHandle(
                    InetAddress.getLocalHost(),
                    fileToShare.getFileName().toString(),
                    256,
                    Files.size(fileToShare)
            );

            resourceHandles.add(resourceToSeed);
            resourceToSourceFile.put(resourceToSeed, fileToShare);
            resourceToBitSet.put(resourceToSeed, createFilledBitSet(resourceToSeed.getNumberOfChunks()));
            resourceToSwarm.put(resourceToSeed, new LinkedList<>(Collections.singletonList(InetAddress.getLocalHost())));
            LOGGER.info(String.format("Sharing file %s located at %s.", resourceToSeed, fileToShare));
            return resourceToSeed;
        }
    }

    public void leech(final ResourceHandle resourceToLeech, final Path fileToWrite) {
        synchronized (lock) {
            assurePathIsUnused(fileToWrite);

            resourceHandles.add(resourceToLeech);
            resourceToTargetFile.put(resourceToLeech, fileToWrite);
            resourceToBitSet.put(resourceToLeech, createEmptyBitSet(resourceToLeech.getNumberOfChunks()));
            resourceToSwarm.put(resourceToLeech, new LinkedList<>(Collections.singletonList(resourceToLeech.getSeederAddress())));
            LOGGER.info(String.format("Leeching file %s to %s.", resourceToLeech, fileToWrite));
        }
    }

    private ResourceHandle findById(final String resourceId) {
        return resourceHandles.stream().filter(h -> h.getResourceId().equals(resourceId)).findFirst().get();
    }

    private BitSet createFilledBitSet(final int numBits) {
        final BitSet result = new BitSet(numBits);
        result.set(0, numBits);
        return result;
    }

    private BitSet createEmptyBitSet(final int numBits) {
        return new BitSet(numBits);
    }

    private void assurePathIsUnused(final Path path) {
        if (resourceToSourceFile.values().contains(path)) {
            throw new IllegalStateException("Already used as source: " + path);
        }

        if (resourceToTargetFile.values().contains(path)) {
            throw new IllegalStateException("Already used as target: " + path);
        }
    }
}
