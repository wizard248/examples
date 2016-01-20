package cz.voho.shitorrent.model.internal;

import cz.voho.shitorrent.model.external.PeerCrate;

/**
 * Created by vojta on 18/01/16.
 */
public class ResourceSwarmPeer {
    private final PeerCrate peer;
    private Bitmap availableOnPeer;

    public ResourceSwarmPeer(final PeerCrate peer, final Bitmap availableOnPeer) {
        this.peer = peer;
        this.availableOnPeer = availableOnPeer;
    }

    public PeerCrate getPeer() {
        return peer;
    }

    public Bitmap getAvailableOnPeer() {
        return availableOnPeer;
    }

    public void updateBitmap(final Bitmap bitmap) {
        this.availableOnPeer = bitmap;
    }
}
