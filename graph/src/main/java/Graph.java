import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by voho on 16.08.15.
 */
public class Graph<NV, EV> {
    private final DiGraph<NV, EV> adaptee;

    public Graph() {
        this.adaptee = new DiGraph<>();
    }

    public void addNode(NV value) {
        adaptee.addNode(value);
    }

    public void addEdge(NV source, NV target, EV value) {
        adaptee.addEdge(source, target, value);
        adaptee.addEdge(target, source, value);
    }

    public Set<NV> getAllNodes() {
        return adaptee.getAllNodes();
    }

    public Set<GraphEdge<NV, EV>> getAllEdges() {
        return adaptee
                .getAllEdges()
                .stream()
                .map(DiGraphEdge::toGraphEdge)
                .collect(Collectors.toSet());
    }

    public Set<NV> getNeighbours(NV source) {
        return adaptee.getNeighbours(source);
    }

    public boolean hasEdge(NV source, NV target) {
        return adaptee.getEdge(source, target).isPresent();
    }

    @Override
    public String toString() {
        return adaptee.toString();
    }
}
