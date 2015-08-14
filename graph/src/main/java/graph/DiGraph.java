package graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by voho on 14.08.15.
 */
public class DiGraph<NV, EV> {
    private final Set<NV> nodes;
    private final Table<NV, NV, EV> edges;

    public DiGraph() {
        this.nodes = new LinkedHashSet<>(10);
        this.edges = HashBasedTable.create(10, 10);
    }

    public Set<NV> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public Set<NV> getNeighbourNodes(NV node) {
        Preconditions.checkState(hasNode(node), "graph does not contain the node");
        if (edges.containsRow(node)) {
            return edges.row(node).keySet();
        } else {
            return Collections.emptySet();
        }
    }

    public void addNode(NV value) {
        Preconditions.checkState(!hasNode(value), "graph already contains this node");
        nodes.add(value);
    }

    public void addEdge(NV a, NV b, EV value) {
        Preconditions.checkState(hasNode(a), "graph does not contain the first node");
        Preconditions.checkState(hasNode(b), "graph does not contain the second node");
        Preconditions.checkState(!hasEdge(a, b), "graph already contains this edge");
        edges.put(a, b, value);
    }

    public boolean hasNode(NV value) {
        return nodes.contains(value);
    }

    public boolean hasEdge(NV a, NV b) {
        Preconditions.checkState(hasNode(a), "graph does not contain the first node");
        Preconditions.checkState(hasNode(b), "graph does not contain the second node");
        return edges.contains(a, b);
    }
}
