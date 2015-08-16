/**
 * Created by voho on 16.08.15.
 */
public class DiGraphEdge<NV, EV> {
    private NV source;
    private NV target;
    private EV value;

    public DiGraphEdge(NV source, NV target, EV value) {
        this.source = source;
        this.target = target;
        this.value = value;
    }

    public GraphEdge<NV, EV> toGraphEdge() {
        return new GraphEdge<>(source, target, value);
    }

    public NV getSource() {
        return source;
    }

    public NV getTarget() {
        return target;
    }

    public EV getValue() {
        return value;
    }
}
