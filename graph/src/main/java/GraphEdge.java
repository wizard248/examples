import java.util.HashSet;
import java.util.Set;

/**
 * Created by voho on 16.08.15.
 */
public class GraphEdge<NV, EV> {
    private Set<NV> nodes;
    private EV value;

    public GraphEdge(NV n1, NV n2, EV value) {
        this.nodes = new HashSet<>(2);
        nodes.add(n1);
        nodes.add(n2);
        this.value = value;
    }

    public boolean incidesWithNode(NV node) {
        return nodes.contains(node);
    }

    public EV getValue() {
        return value;
    }
}
