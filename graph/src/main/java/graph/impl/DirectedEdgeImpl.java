package graph.impl;

import graph.api.DirectedEdge;
import graph.api.Node;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by voho on 14.08.15.
 */
public class DirectedEdgeImpl<NV, EV> implements DirectedEdge<NV, EV> {
    private final Node<NV> source;
    private final Node<NV> target;
    private final EV value;

    public DirectedEdgeImpl(Node<NV> source, Node<NV> target, EV value) {
        this.source = source;
        this.target = target;
        this.value = value;
    }

    @Override
    public Node<NV> getSourceNode() {
        return source;
    }

    @Override
    public Node<NV> getTargetNode() {
        return target;
    }

    @Override
    public EV getValue() {
        return value;
    }

    @Override
    public Set<Node<NV>> getIncidentNodes() {
        return new LinkedHashSet<>(Arrays.asList(source, target));
    }
}
