import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DiGraph<NODE> {
    private final Set<NODE> nodes;
    private final Set<Edge<NODE>> edges;

    public DiGraph() {
        this.nodes = new LinkedHashSet<>();
        this.edges = new LinkedHashSet<>();
    }

    public void addNode(NODE node) {
        if (containsNode(node)) {
            throw new IllegalStateException("Node already exists in graph: " + node);
        }

        addNodeInternal(node);
    }

    private boolean containsNode(NODE node) {
        return nodes.contains(node);
    }

    protected void addNodeInternal(NODE node) {
        nodes.add(node);
    }

    public void addEdge(NODE source, NODE target) {
        Edge<NODE> newEdge = new Edge<>(source, target);

        if (containsEdgeInternal(newEdge)) {
            throw new IllegalStateException("Edge already exists in graph: " + newEdge);
        }

        addEdgeInternal(newEdge);
    }

    protected void addEdgeInternal(Edge<NODE> edge) {
        edges.add(edge);
    }

    public Set<NODE> getNeighbours(NODE node) {
        if (!containsNode(node)) {
            throw new IllegalStateException("Node is not in the graph: " + node);
        }

        return edges
                .stream()
                .map(edge -> edge.source)
                .filter(source -> source.equals(node))
                .collect(Collectors.toSet());
    }

    public boolean containsEdge(NODE source, NODE target) {
        return containsEdgeInternal(new Edge<>(source, target));
    }

    protected boolean containsEdgeInternal(Edge<NODE> edge) {
        return edges.contains(edge);
    }

    protected static class Edge<NODE> {
        protected final NODE source;
        protected final NODE target;

        protected Edge(NODE source, NODE target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge<?> edge = (Edge<?>) o;
            return Objects.equals(source, edge.source) &&
                    Objects.equals(target, edge.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, target);
        }

        @Override
        public String toString() {
            return String.format("%s -> %s", source, target);
        }
    }
}
