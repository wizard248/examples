package graph.api;

/**
 * Created by voho on 14.08.15.
 */
public interface MutableGraph<NV, EV> extends Graph<NV, EV> {
    void addNode(NV value);

    void removeNode(NV value);
}
