package graph.model;

import com.google.common.base.Objects;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by voho on 16.08.15.
 */
public class GraphEdge<NV, EV> {
    private NV either;
    private NV another;
    private EV value;

    public GraphEdge(NV either, NV another, EV value) {
        this.either = either;
        this.another = another;
        this.value = value;
    }

    public NV getEither() {
        return either;
    }

    public NV getAnother() {
        return another;
    }

    public EV getValue() {
        return value;
    }

    public Set<NV> getNodes() {
        return Stream.of(either, another).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final GraphEdge<?, ?> graphEdge = (GraphEdge<?, ?>) o;
        return Objects.equal(getNodes(), graphEdge.getNodes());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getNodes());
    }
}
