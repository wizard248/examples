package service.worker;

import service.ResourceMetaDataService;

import java.util.logging.Logger;

/**
 * Created by vojta on 12/01/16.
 */
public class SwarmUpdatingWorker implements Runnable {
    private final static Logger LOGGER = Logger.getAnonymousLogger();

    private final ResourceMetaDataService resourceMetaDataService;

    public SwarmUpdatingWorker(final ResourceMetaDataService resourceMetaDataService) {
        this.resourceMetaDataService = resourceMetaDataService;
    }

    @Override
    public void run() {
        LOGGER.info("Updated swarm.");
        // TODO
    }
}
