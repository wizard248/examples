package graph.model;

import com.google.common.base.Objects;

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

    public boolean incidentWithNode(NV node) {
        return node.equals(source) || node.equals(target);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final DiGraphEdge<?, ?> that = (DiGraphEdge<?, ?>) o;
        return Objects.equal(source, that.source) &&
                Objects.equal(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(source, target);
    }
}
