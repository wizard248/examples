package cz.voho.shitorrent.service;

import cz.voho.shitorrent.model.internal.Configuration;
import cz.voho.shitorrent.service.worker.ChunkDownloadingWorker;
import cz.voho.shitorrent.service.worker.SwarmUpdatingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by vojta on 18/01/16.
 */
@Service
public class WorkerExecutingService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Configuration configuration;
    @Autowired
    private BasicInputOutputService basicInputOutputService;
    @Autowired
    private ResourceManagementService resourceManagementService;
    @Autowired
    private OtherPeerClientService otherPeerClientService;

    @PostConstruct
    public void startWorkers() {
        for (int i = 0; i < configuration.getMaxNumberOfConcurrentSwarmUpdaters(); i++) {
            new SwarmUpdatingWorker(configuration, resourceManagementService, otherPeerClientService).runInNewThread();
            log.info("SwarmUpdatingWorker started.");
        }

        for (int i = 0; i < configuration.getMaxNumberOfConcurrentDownloads(); i++) {
            new ChunkDownloadingWorker(configuration, resourceManagementService, otherPeerClientService, basicInputOutputService).runInNewThread();
            log.info("ChunkDownloadingWorker started.");
        }
    }
}
