package cz.voho.shitorrent.controller;

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
import cz.voho.shitorrent.model.internal.Configuration;
import cz.voho.shitorrent.service.FrontService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@RestController
public class FrontController {
    @Autowired
    private Configuration configuration;
    @Autowired
    private FrontService frontService;

    /**
     * Returns resource list. Each resource is represented by the following:
     * <ul>
     * <li>key (unique identifier)</li>
     * <li>file name</li>
     * <li>file size</li>
     * <li>chunk size</li>
     * </ul>
     *
     * @return list of resources
     */
    @RequestMapping(method = RequestMethod.GET, value = "/resources")
    public List<ResourceMetaSummaryCrate> getResources(final HttpServletRequest request) {
        frontService.detectLeecher(request);
        return frontService.getResources();
    }

    /**
     * Returns resource details. The resource is represented by the following:
     * <ul>
     * <li>key (unique identifier)</li>
     * <li>file name</li>
     * <li>file size</li>
     * <li>chunk size</li>
     * <li>availability bitmap</li>
     * <li>list of peers</li>
     * </ul>
     *
     * @param key resource key
     * @return resource detail
     * @throws ResourceNotFoundException if resource is not found
     */
    @RequestMapping(method = RequestMethod.GET, value = "/resources/{key}")
    public ResourceMetaDetailCrate getResourceDetail(@PathVariable final String key, final HttpServletRequest request) throws ResourceNotFoundException {
        Objects.requireNonNull(key);
        frontService.detectLeecher(request);
        return frontService.getResourceDetail(key);
    }

    /**
     * Returns chunk with binary data.
     *
     * @param key resource key
     * @param index chunk index (starting from 0)
     * @return chunk
     * @throws ResourceNotFoundException if resource is not found
     * @throws ChunkNotFoundException if chunk is not found
     * @throws ErrorReadingChunkException if chunk cannot be read
     */
    @RequestMapping(method = RequestMethod.GET, value = "/resources/{key}/{index}")
    public ChunkCrate getResourceChunk(@PathVariable final String key, @PathVariable final Integer index, final HttpServletRequest request) throws ResourceNotFoundException, ChunkNotFoundException, ErrorReadingChunkException {
        Objects.requireNonNull(key);
        Objects.requireNonNull(index);
        frontService.detectLeecher(request);
        return frontService.getResourceChunk(key, index);
    }

    /**
     * Starts leeching a resource.
     *
     * @param infoForLeechingCrate all information needed to start leeching (e.g. resource key and seeders)
     * @throws CannotLeechException if there is an error while leeching
     */
    @RequestMapping(method = RequestMethod.POST, value = "/leech")
    public void leech(@RequestBody final InfoForLeechingCrate infoForLeechingCrate, final HttpServletRequest request) throws CannotLeechException {
        Objects.requireNonNull(infoForLeechingCrate);
        frontService.detectLeecher(request);
        frontService.leech(infoForLeechingCrate);
    }

    /**
     * Starts seeding a resource.
     *
     * @param infoForSeeding all information needed to start seeding (e.g. source path)
     * @throws CannotSeedException if there is an error while seeding
     */
    @RequestMapping(method = RequestMethod.POST, value = "/seed")
    public void seed(@RequestBody final InfoForSeedingCrate infoForSeeding, final HttpServletRequest request) throws CannotSeedException {
        Objects.requireNonNull(infoForSeeding);
        frontService.detectLeecher(request);
        frontService.seed(infoForSeeding);
    }

    // TODO: just for speeding up the dev
    @PostConstruct
    public void makeSomeSeeding() throws CannotSeedException {
        if (configuration.getLocalPort() == 7891) {
            seedX(Paths.get("/Users/vojta/Downloads/XOrgSourcingService_ar.docx"));
            seedX(Paths.get("/Users/vojta/Downloads/LANDR-innovation.wav"));
            seedX(Paths.get("/Users/vojta/Downloads/112775__kyster__forest-ambience-with-beach-in-background.wav"));
        }
    }

    private void seedX(final Path path) throws CannotSeedException {
        final InfoForSeedingCrate info = new InfoForSeedingCrate();
        info.setSourcePath(path.toAbsolutePath().toString());
        frontService.seed(info);
    }
}
