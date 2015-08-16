import java.util.Set;

/**
 * Created by voho on 16.08.15.
 */
public class SimpleDiGraph<NV> {
    private final Object SOMETHING = true;
    private final DiGraph<NV, Object> adaptee;

    public SimpleDiGraph() {
        this.adaptee = new DiGraph<>();
    }

    public void addNode(NV value) {
        adaptee.addNode(value);
    }

    public void addEdge(NV source, NV target) {
        adaptee.addEdge(source, target, SOMETHING);
    }

    public boolean hasEdge(NV source, NV target) {
        return adaptee.getEdge(source, target).isPresent();
    }

    public Set<NV> getNeighbours(NV source) {
        return adaptee.getNeighbours(source);
    }

    @Override
    public String toString() {
        return adaptee.toString();
    }
}
