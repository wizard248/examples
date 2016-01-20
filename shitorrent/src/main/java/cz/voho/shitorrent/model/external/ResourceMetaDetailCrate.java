package cz.voho.shitorrent.model.external;

import java.util.List;

/**
 * Created by vojta on 13/01/16.
 */
public class ResourceMetaDetailCrate extends ResourceMetaSummaryCrate {
    private String bitmap;
    private List<PeerCrate> swarm;

    public String getBitmap() {
        return bitmap;
    }

    public void setBitmap(final String bitmap) {
        this.bitmap = bitmap;
    }

    public List<PeerCrate> getSwarm() {
        return swarm;
    }

    public void setSwarm(final List<PeerCrate> swarm) {
        this.swarm = swarm;
    }
}
