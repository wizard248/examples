package graph.impl;

import graph.api.Node;
import graph.api.UndirectedEdge;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by voho on 14.08.15.
 */
public class UndirectedEdgeImpl<NV, EV> implements UndirectedEdge<NV, EV> {
    private final EV value;
    private final Node<NV> first;
    private final Node<NV> second;

    public UndirectedEdgeImpl(Node<NV> first, Node<NV> second, EV value) {
        this.first = first;
        this.second = second;
        this.value = value;
    }

    @Override
    public EV getValue() {
        return value;
    }

    @Override
    public Set<Node<NV>> getIncidentNodes() {
        return new LinkedHashSet<>(Arrays.asList(first, second));
    }
}
