package cz.voho.shitorrent.model.external;

import java.util.Objects;

/**
 * Created by vojta on 13/01/16.
 */
public class PeerCrate {
    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PeerCrate peerCrate = (PeerCrate) o;
        return port == peerCrate.port &&
                Objects.equals(host, peerCrate.host);
    }

    @Override
    public String toString() {
        return String.format("%s:%d", host, port);
    }
}
