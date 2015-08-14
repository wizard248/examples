package graph;

/**
 * Created by voho on 14.08.15.
 */
public class Graph<NV, EV> extends DiGraph<NV, EV> {
    @Override
    public void addEdge(NV a, NV b, EV value) {
        super.addEdge(a, b, value);
        super.addEdge(b, a, value);
    }
}
