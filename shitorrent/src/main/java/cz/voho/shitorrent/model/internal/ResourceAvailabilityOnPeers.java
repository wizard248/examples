package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.model.external.PeerCrate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Created by vojta on 27/01/16.
 */
public class ResourceAvailabilityOnPeers {
    private final Map<PeerCrate, Optional<Bitmap>> swarm;

    public ResourceAvailabilityOnPeers() {
        this.swarm = new HashMap<>();
    }

    public void removeFromSwarm(PeerCrate peerCrate) {
        swarm.remove(peerCrate);
    }

    public void mergeWithSeedersUnknownAvailability(final List<PeerCrate> seeders) {
        seeders.forEach(peer -> mergeToSwarm(peer, Optional.empty()));
    }

    private void mergeToSwarm(final PeerCrate seeder, final Optional<Bitmap> bitmap) {
        this.swarm.merge(seeder, bitmap, new BiFunction<Optional<Bitmap>, Optional<Bitmap>, Optional<Bitmap>>() {
            @Override
            public Optional<Bitmap> apply(final Optional<Bitmap> oldValue, final Optional<Bitmap> newValue) {
                if (newValue.isPresent()) {
                    return newValue;
                } else {
                    return oldValue;
                }
            }
        });
    }

    public void mergeSeederAvailability(final PeerCrate seeder, final Bitmap bitmap) {
        mergeToSwarm(seeder, Optional.of(bitmap));
    }

    public void markChunkAvailable(final PeerCrate seeder, final int chunkIndex) {
        final Optional<Bitmap> localBitmap = getOrDefault(seeder);

        if (localBitmap.isPresent()) {
            localBitmap.get().markAvailable(chunkIndex);
        }
    }

    public boolean isChunkAvailable(final PeerCrate seeder, final int chunkIndex) {
        final Optional<Bitmap> localBitmap = getOrDefault(seeder);

        if (localBitmap.isPresent()) {
            return localBitmap.get().isAvailable(chunkIndex);
        } else {
            return false;
        }
    }

    public void markAvailable(final PeerCrate seeder, final int chunkIndex) {
        final Optional<Bitmap> localBitmap = getOrDefault(seeder);

        if (localBitmap.isPresent()) {
            localBitmap.get().markAvailable(chunkIndex);
        }
    }

    public void markAllAvailable(final PeerCrate localPeer, final int numChunks) {
        Bitmap bitmap = new Bitmap(numChunks);
        bitmap.markAllAvailable();
        this.swarm.put(localPeer, Optional.of(bitmap));
    }

    public Bitmap getAvailabilityBitmap(final PeerCrate seeder) {
        final Optional<Bitmap> localBitmap = getOrDefault(seeder);

        if (localBitmap.isPresent()) {
            return localBitmap.get();
        } else {
            // TODO not nice
            Bitmap b = new Bitmap(0);
            return b;
        }
    }

    public Set<PeerCrate> getPeersWithChunk(final int chunkIndex) {
        return swarm
                .entrySet()
                .stream()
                .filter(e -> e.getValue().orElse(new Bitmap(0)).isAvailable(chunkIndex))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public boolean areAllChunksAvailable(final PeerCrate localPeer) {
        final Optional<Bitmap> localBitmap = getOrDefault(localPeer);

        if (localBitmap.isPresent()) {
            return localBitmap.get().isAllAvailable();
        } else {
            return false;
        }
    }

    private Optional<Bitmap> getOrDefault(final PeerCrate peer) {
        return this.swarm.getOrDefault(peer, Optional.empty());
    }

    public Set<PeerCrate> getAllPeers() {
        return swarm.keySet();
    }
}
