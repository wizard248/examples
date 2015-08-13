package graph.api;

/**
 * Created by voho on 13.08.15.
 */
public interface DirectedEdge<NV, EV> extends Edge<NV, EV> {
    Node<NV> getSourceNode();

    Node<NV> getTargetNode();
}
