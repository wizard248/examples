package cz.voho.shitorrent.service.worker;

import cz.voho.shitorrent.service.OtherPeerClientService;
import cz.voho.shitorrent.service.ResourceManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by vojta on 21/01/16.
 */
public class SwarmUpdatingWorker implements Worker {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final ResourceManagementService resourceManagementService;
    private final OtherPeerClientService otherPeerClientService;

    public SwarmUpdatingWorker(final ResourceManagementService resourceManagementService, final OtherPeerClientService otherPeerClientService) {
        this.resourceManagementService = resourceManagementService;
        this.otherPeerClientService = otherPeerClientService;
    }

    @Override
    public void run() {

    }
}
