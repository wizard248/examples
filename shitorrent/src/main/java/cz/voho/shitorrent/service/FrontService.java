package cz.voho.shitorrent.service;

import cz.voho.shitorrent.exception.CannotLeechException;
import cz.voho.shitorrent.exception.CannotSeedException;
import cz.voho.shitorrent.exception.ChunkNotFoundException;
import cz.voho.shitorrent.exception.ErrorReadingChunkException;
import cz.voho.shitorrent.exception.ResourceNotFoundException;
import cz.voho.shitorrent.model.external.ChunkCrate;
import cz.voho.shitorrent.model.external.InfoForLeechingCrate;
import cz.voho.shitorrent.model.external.InfoForSeedingCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;
import cz.voho.shitorrent.model.external.ResourceMetaSummaryCrate;
import cz.voho.shitorrent.model.internal.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FrontService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ResourceManagementService resourceManagementService;
    @Autowired
    private WorkerExecutingService workerService;

    public List<ResourceMetaSummaryCrate> getResources() {
        return resourceManagementService.getAllResource()
                .stream()
                .map(resource -> {
                    ResourceMetaSummaryCrate result = new ResourceMetaSummaryCrate();
                    result.setKey(resource.getKey());
                    result.setName(resource.getName());
                    result.setFileSize(resource.getFileSize());
                    result.setChunkSize(resource.getChunkSize());
                    return result;
                })
                .collect(Collectors.toList());
    }

    public ResourceMetaDetailCrate getResourceDetail(final String key) throws ResourceNotFoundException {
        return resourceManagementService.getResource(key)
                .filter(Resource::isInitialized)
                .map(resource -> {
                    final ResourceMetaDetailCrate result = new ResourceMetaDetailCrate();
                    result.setKey(resource.getKey());
                    result.setName(resource.getName());
                    result.setFileSize(resource.getFileSize());
                    result.setChunkSize(resource.getChunkSize());
                    result.setBitmap(resource.getAvailabilityBitmap().toString());
                    result.setSwarm(new ArrayList<>(resource.getPeers()));
                    return result;
                })
                .orElseThrow(() -> new ResourceNotFoundException(key));
    }

    public ChunkCrate getResourceChunk(final String key, final int chunkIndex) throws ResourceNotFoundException, ErrorReadingChunkException, ChunkNotFoundException {
        return resourceManagementService.getResourceChunk(key, chunkIndex)
                .map(data -> {
                    ChunkCrate result = new ChunkCrate();
                    result.setData(data);
                    return result;
                })
                .orElseThrow(() -> new ChunkNotFoundException(key, chunkIndex));
    }

    public void leech(final InfoForLeechingCrate infoForLeechingCrate) throws CannotLeechException {
        resourceManagementService.newLeechResource(infoForLeechingCrate);
    }

    public void seed(final InfoForSeedingCrate infoForSeeding) throws CannotSeedException {
        resourceManagementService.newSeedResource(infoForSeeding);
    }
}
