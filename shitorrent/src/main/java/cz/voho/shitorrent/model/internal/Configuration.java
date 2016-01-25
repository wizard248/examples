package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.model.external.PeerCrate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by vojta on 18/01/16.
 */
public class Configuration {
    public static final String CUSTOM_HEADER_LEECHER_HOST = "X-LEECHER-HOST";
    public static final String CUSTOM_HEADER_LEECHER_PORT = "X-LEECHER-PORT";

    private String localHost;
    private int localPort;

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(final int localPort) {
        this.localPort = localPort;
    }

    public Path getOutputDirectory() {
        return Paths.get("/Users/vojta/Downloads/tor/" + localPort);
    }

    @PostConstruct
    public void initialize() throws IOException {
        localHost = InetAddress.getLocalHost().getHostAddress();
        Files.createDirectories(getOutputDirectory());
    }

    public PeerCrate getLocalPeer() {
        PeerCrate result = new PeerCrate();
        result.setHost(localHost);
        result.setPort(localPort);
        return result;
    }

    public int getMaxNumberOfConcurrentDownloads() {
        return 5;
    }

    public int getMaxNumberOfConcurrentSwarmUpdaters() {
        return 3;
    }

    public int getPeerConnectionTimeoutMs() {
        return 3000;
    }
}
