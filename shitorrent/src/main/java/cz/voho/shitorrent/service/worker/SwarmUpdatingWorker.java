package cz.voho.shitorrent.service.worker;

import cz.voho.shitorrent.exception.NoPeerConnectionException;
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

/**
 * Created by vojta on 21/01/16.
 */
public class SwarmUpdatingWorker implements Worker {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Configuration configuration;
    private final ResourceManagementService resourceManagementService;
    private final OtherPeerClientService otherPeerClientService;

    public SwarmUpdatingWorker(final Configuration configuration, final ResourceManagementService resourceManagementService, final OtherPeerClientService otherPeerClientService) {
        this.configuration = configuration;
        this.resourceManagementService = resourceManagementService;
        this.otherPeerClientService = otherPeerClientService;
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

            final Optional<PeerCrate> feasiblePeer = selectFeasiblePeer(unfinishedResource.get());

            if (!feasiblePeer.isPresent()) {
                log.warn("No feasible peer.");
                sleepALittle();
                continue;
            }

            try {
                updateSwarm(unfinishedResource.get(), feasiblePeer.get());
            } catch (NoPeerConnectionException e) {
                log.warn("Error while downloading detail.");
            }
        }
    }

    private Optional<Resource> selectUnfinishedResource() {
        return resourceManagementService.getUnfinishedResources().stream().findFirst();
    }

    private Optional<PeerCrate> selectFeasiblePeer(final Resource resource) {
        final Set<PeerCrate> peers = resource.getPeers();
        return peers.stream().findAny();
    }

    private void updateSwarm(final Resource resource, final PeerCrate sourcePeer) throws NoPeerConnectionException {
        log.info("Starting swarm-peer update of {} from {}...", resource.getKey(), sourcePeer);

        ResourceMetaDetailCrate detail = otherPeerClientService.downloadResourceDetail(sourcePeer, resource.getKey());

        if (!resource.isInitialized()) {
            Path output = configuration.getOutputDirectory().resolve(detail.getName());
            resource.initializeForLeeching(detail, output);
        }

        resource.mergeToSwarmWithUnknownAvailability(detail.getSwarm());
        resource.updateSeederAvailability(sourcePeer, new Bitmap(detail.getBitmap()));

        log.info("Swarm-peer update of {} from {} completed.", resource.getKey(), sourcePeer);
    }

    private void sleepALittle() {
        sleep(3000);
    }
}
