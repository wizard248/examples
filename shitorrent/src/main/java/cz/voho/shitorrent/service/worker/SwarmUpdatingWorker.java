package cz.voho.shitorrent.service.worker;

import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;
import cz.voho.shitorrent.model.internal.Bitmap;
import cz.voho.shitorrent.model.internal.Configuration;
import cz.voho.shitorrent.model.internal.Resource;
import cz.voho.shitorrent.service.OtherPeerClientService;
import cz.voho.shitorrent.service.ResourceManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by vojta on 21/01/16.
 */
public class SwarmUpdatingWorker implements Worker {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Configuration configuration;
    private final ResourceManagementService resourceManagementService;
    private final OtherPeerClientService otherPeerClientService;
    private final AtomicBoolean live = new AtomicBoolean(true);

    public SwarmUpdatingWorker(final Configuration configuration, final ResourceManagementService resourceManagementService, final OtherPeerClientService otherPeerClientService) {
        this.configuration = configuration;
        this.resourceManagementService = resourceManagementService;
        this.otherPeerClientService = otherPeerClientService;
    }

    @Override
    public void run() {
        while (live.get()) {
            final Optional<Resource> unfinishedResource = selectUnfinishedResource();

            if (!unfinishedResource.isPresent()) {
                log.warn("No unfinished resource.");
                sleepForResourceChange();
                continue;
            }

            final Optional<PeerCrate> feasiblePeer = selectFeasiblePeer(unfinishedResource.get());

            if (!feasiblePeer.isPresent()) {
                log.warn("No feasible peer.");
                sleepForSwarmChange();
                continue;
            }

            updateSeeders(unfinishedResource.get(), feasiblePeer.get());
        }
    }

    private Optional<Resource> selectUnfinishedResource() {
        return resourceManagementService.getUnfinishedResources().stream().findFirst();
    }

    private Optional<PeerCrate> selectFeasiblePeer(final Resource resource) {
        final Set<PeerCrate> peers = resource.getPeers();
        return peers.stream().findAny();
    }

    private void updateSeeders(final Resource resource, final PeerCrate sourcePeer) {
        log.info("Starting swarm-peer update of {} from {}...", resource.getKey(), sourcePeer);

        Optional<ResourceMetaDetailCrate> detail = otherPeerClientService.downloadResourceDetail(sourcePeer, resource.getKey());

        if (detail.isPresent()) {
            if (!resource.isInitialized()) {
                Path output = configuration.getOutputDirectory().resolve(detail.get().getName());
                resource.initializeForLeeching(detail.get(), output);
            }

            resource.mergeToSwarmWithUnknownAvailability(detail.get().getSwarm());
            resource.updateSeederAvailability(sourcePeer, new Bitmap(detail.get().getBitmap()));
        }

        log.info("Swarm-peer update of {} from {} completed.", resource.getKey(), sourcePeer);
    }

    @Override
    public void stop() {
        live.set(false);
    }
}
