package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.model.external.PeerCrate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by vojta on 13/01/16.
 */
public class ResourceSwarm {
    private final Set<ResourceSwarmPeer> peersInSwarm;

    public ResourceSwarm() {
        this.peersInSwarm = new LinkedHashSet<>();
    }

    public void initializeAsSeeded() {
        this.peersInSwarm.clear();
    }

    public void initializeAsLeeched(final Set<PeerCrate> seeders, int numChunks) {
        this.peersInSwarm.clear();

        for (PeerCrate seeder : seeders) {
            final Bitmap bitmap = new Bitmap(numChunks);
            bitmap.markAllUnavailable();
            this.peersInSwarm.add(new ResourceSwarmPeer(seeder, bitmap));
        }
    }

    public Optional<Bitmap> getAvailabilityFor(PeerCrate peer) {
        return findExisting(peer).map(ResourceSwarmPeer::getAvailableOnPeer);
    }

    public Set<PeerCrate> getPeers() {
        return peersInSwarm.stream().map(ResourceSwarmPeer::getPeer).collect(Collectors.toSet());
    }

    public Set<PeerCrate> getPeersWithChunk(final int chunkIndex) {
        return peersInSwarm
                .stream()
                .filter(p -> p.getAvailableOnPeer().isAvailable(chunkIndex))
                .map(p -> p.getPeer())
                .collect(Collectors.toSet());
    }

    public void updatePeerAvailability(PeerCrate peer, Bitmap bitmap) {
        Optional<ResourceSwarmPeer> existing = findExisting(peer);

        if (existing.isPresent()) {
            existing.get().updateBitmap(bitmap);
        } else {
            peersInSwarm.add(new ResourceSwarmPeer(peer, bitmap));
        }
    }

    public void removePeer(PeerCrate peer) {
        // TODO
    }

    public void clear() {
        peersInSwarm.clear();
    }

    private Optional<ResourceSwarmPeer> findExisting(final PeerCrate peer) {
        return peersInSwarm.stream().filter(p -> p.getPeer().getHost().equalsIgnoreCase(peer.getHost()) && p.getPeer().getPort() == peer.getPort()).findFirst();
    }
}
