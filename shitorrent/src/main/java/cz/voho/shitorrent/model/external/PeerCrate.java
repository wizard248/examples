package cz.voho.shitorrent.model.external;

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
    public String toString() {
        return String.format("%s:%d", host, port);
    }
}
