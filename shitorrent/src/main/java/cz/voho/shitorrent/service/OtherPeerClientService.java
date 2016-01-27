package cz.voho.shitorrent.service;

import cz.voho.shitorrent.model.external.ChunkCrate;
import cz.voho.shitorrent.model.external.PeerCrate;
import cz.voho.shitorrent.model.external.ResourceMetaDetailCrate;
import cz.voho.shitorrent.model.internal.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Optional;

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
        httpRequestFactory.setBufferRequestBody(true);
        httpRequestFactory.setOutputStreaming(false);
    }

    public Optional<ResourceMetaDetailCrate> downloadResourceDetail(final PeerCrate randomSeeder, final String key) {
        final String url = getUrlForGetResourceDetail(randomSeeder, key);
        return httpGet(randomSeeder, url, ResourceMetaDetailCrate.class);
    }

    public Optional<ChunkCrate> downloadChunk(final PeerCrate randomSeeder, final String key, final int chunkIndex) {
        final String url = getUrlForGetChunk(randomSeeder, key, chunkIndex);
        return httpGet(randomSeeder, url, ChunkCrate.class);
    }

    private <T> Optional<T> httpGet(final PeerCrate peerCrate, final String url, final Class<T> type) {
        log.info("Downloading {} from {}...", type.getName(), url);

        try {
            final RequestCallback rc = request -> {
                final PeerCrate localPeer = configuration.getLocalPeer();
                final HttpHeaders headers = request.getHeaders();
                headers.set(Configuration.CUSTOM_HEADER_LEECHER_HOST, localPeer.getHost());
                headers.set(Configuration.CUSTOM_HEADER_LEECHER_PORT, String.valueOf(localPeer.getPort()));
            };
            final RestTemplate rest = new RestTemplate(httpRequestFactory);
            final HttpMessageConverterExtractor<T> re = new HttpMessageConverterExtractor<T>(type, rest.getMessageConverters());
            final T object = rest.execute(url, HttpMethod.GET, rc, re);
            return Optional.of(object);
        } catch (final RestClientException e) {
            markPeerAsNonResponsive(peerCrate);
            return Optional.empty();
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

    public void markPeerAsNonResponsive(final PeerCrate peerCrate) {
        // TODO
        System.out.println("PEER NOT RESP: " + peerCrate);
    }

    public void markPeerAsCandidateSeeder(final String host, final int port) {
        // TODO
        PeerCrate peerCrate = new PeerCrate();
        peerCrate.setHost(host);
        peerCrate.setPort(port);
        System.out.println("PEER POSSIBLE: " + peerCrate);
    }
}
