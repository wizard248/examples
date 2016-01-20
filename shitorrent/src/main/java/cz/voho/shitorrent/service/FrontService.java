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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by vojta on 18/01/16.
 */
@Service
public class FrontService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private ResourceManagementService resourceManagementService;
    @Autowired
    private DownloadService downloadService;

    public void leech(final InfoForLeechingCrate infoForLeechingCrate) throws CannotLeechException {
        resourceManagementService.newLeechResource(infoForLeechingCrate);
        downloadService.scheduleLeeching(infoForLeechingCrate);
    }

    public void seed(final InfoForSeedingCrate infoForSeeding) throws CannotSeedException {
        resourceManagementService.newSeedResource(infoForSeeding);
    }

    public List<ResourceMetaSummaryCrate> getResources() {
        return resourceManagementService.getResourceSummaryList();
    }

    public ResourceMetaDetailCrate getResourceDetail(final String key) throws ResourceNotFoundException {
        return resourceManagementService.getResourceDetail(key);
    }

    public ChunkCrate getResourceChunk(final String key, final int chunkIndex) throws ResourceNotFoundException, ErrorReadingChunkException, ChunkNotFoundException {
        return resourceManagementService.getResourceChunk(key, chunkIndex);
    }
}
