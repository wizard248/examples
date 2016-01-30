package cz.voho.shitorrent.service;

import cz.voho.shitorrent.model.internal.Configuration;
import cz.voho.shitorrent.service.worker.ChunkDownloadingWorker;
import cz.voho.shitorrent.service.worker.SwarmUpdatingWorker;
import cz.voho.shitorrent.service.worker.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.LinkedList;
import java.util.List;

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

    private final List<Worker> workers;

    public WorkerExecutingService() {
        this.workers = new LinkedList<>();
    }

    @PostConstruct
    public void startWorkers() {
        for (int i = 0; i < configuration.getMaxNumberOfConcurrentSwarmUpdaters(); i++) {
            final SwarmUpdatingWorker worker = new SwarmUpdatingWorker(configuration, resourceManagementService, otherPeerClientService);
            log.info("SwarmUpdatingWorker created.");
            workers.add(worker);
        }

        for (int i = 0; i < configuration.getMaxNumberOfConcurrentDownloads(); i++) {
            final ChunkDownloadingWorker worker = new ChunkDownloadingWorker(configuration, resourceManagementService, otherPeerClientService, basicInputOutputService);
            log.info("ChunkDownloadingWorker created.");
            workers.add(worker);
        }

        for (Worker worker : workers) {
            worker.runInNewThread();
        }
    }

    @PreDestroy
    public void stopWorkers() {
        for (Worker worker : workers) {
            worker.stop();
        }
    }
}
