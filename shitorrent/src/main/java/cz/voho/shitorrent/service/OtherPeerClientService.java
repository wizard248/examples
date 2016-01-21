package cz.voho.shitorrent.service;

import cz.voho.shitorrent.exception.NoPeerConnectionException;
import cz.voho.shitorrent.model.external.ChunkCrate;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;
import cz.voho.shitorrent.model.internal.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

/**
 * Created by vojta on 18/01/16.
 */
@Service
public class OtherPeerClientService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private Configuration configuration;
    private SimpleClientHttpRequestFactory httpRequestFactory;

    @PostConstruct
    public void initialize() {
        httpRequestFactory = new SimpleClientHttpRequestFactory();
        httpRequestFactory.setConnectTimeout(configuration.getPeerConnectionTimeoutMs());
        httpRequestFactory.setReadTimeout(configuration.getPeerConnectionTimeoutMs());
    }

    public ResourceMetaDetailCrate downloadResourceDetail(final PeerCrate randomSeeder, final String key) throws NoPeerConnectionException {
        String url = getUrlForGetResourceDetail(randomSeeder, key);
        return httpGet(url, ResourceMetaDetailCrate.class);
    }

    public ChunkCrate downloadChunk(final PeerCrate randomSeeder, final String key, final int chunkIndex) throws NoPeerConnectionException {
        String url = getUrlForGetChunk(randomSeeder, key, chunkIndex);
        return httpGet(url, ChunkCrate.class);
    }

    private <T> T httpGet(String url, Class<T> type) throws NoPeerConnectionException {
        log.info("Downloading {} from {}...", type.getName(), url);

        try {
            return new RestTemplate(httpRequestFactory).getForObject(url, type);
        } catch (RestClientException e) {
            throw new NoPeerConnectionException(url, e);
        }
    }

    private String getUrlForGetResourceDetail(final PeerCrate peerCrate, final String key) {
        return String.format(
                "http://%s:%d/resources/%s",
                peerCrate.getHost(),
                peerCrate.getPort(),
                key
        );
    }

    private String getUrlForGetChunk(final PeerCrate peerCrate, final String key, final int chunkIndex) {
        return String.format(
                "http://%s:%d/resources/%s/%d",
                peerCrate.getHost(),
                peerCrate.getPort(),
                key,
                chunkIndex
        );
    }
}
