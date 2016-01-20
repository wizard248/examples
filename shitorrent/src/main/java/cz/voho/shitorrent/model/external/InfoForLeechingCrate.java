package cz.voho.shitorrent.model.external;

import java.util.List;

/**
 * Created by vojta on 13/01/16.
 */
public class InfoForLeechingCrate {
    private String resourceKey;
    private List<PeerCrate> seeders;

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(final String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public List<PeerCrate> getSeeders() {
        return seeders;
    }

    public void setSeeders(final List<PeerCrate> seeders) {
        this.seeders = seeders;
    }
}
