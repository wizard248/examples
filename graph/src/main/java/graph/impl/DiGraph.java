package graph.impl;

import graph.api.Edge;
import graph.api.Graph;
import graph.api.Node;

import java.util.Set;

/**
 * Created by voho on 14.08.15.
 */
public class DiGraph<NV, EV> implements Graph<NV, EV> {
    @Override
    public Set<Node<NV>> getNodes() {
        return null;
    }

    @Override
    public Set<Edge<NV, EV>> getEdges() {
        return null;
    }

    @Override
    public Set<Edge<NV, EV>> getIncidentEdges(Node<NV> node) {
        return null;
    }
}
