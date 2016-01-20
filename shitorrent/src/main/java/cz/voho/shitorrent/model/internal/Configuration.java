package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.model.external.PeerCrate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by vojta on 18/01/16.
 */
public class Configuration {
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

    public PeerCrate getLocalPeer() {
        try {
            PeerCrate result = new PeerCrate();
            result.setHost(InetAddress.getLocalHost().getHostAddress());
            result.setPort(localPort);
            return result;
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Cannot get local peer.", e);
        }
    }

    public int getSchedulerThreads() {
        return getMaxNumberOfConcurrentDownloads() + 5;
    }

    public int getMaxNumberOfConcurrentDownloads() {
        return 10;
    }
}
