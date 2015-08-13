package graph.api;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by voho on 13.08.15.
 */
public interface Graph<NV, EV> {
    Set<Node<NV>> getNodes();

    Set<Edge<NV, EV>> getEdges();

    Set<Edge<NV, EV>> getIncidentEdges(Node<NV> node);

    default Set<Node<NV>> getIncidentNodes(Edge<NV, EV> edge) {
        return edge.getIncidentNodes();
    }

    default Set<Node<NV>> getNeighbourNodes(Node<NV> node) {
        Set<Node<NV>> result = new LinkedHashSet<>();
        getEdges()
                .stream()
                .map(this::getIncidentNodes)
                .filter(nodes -> nodes.contains(node))
                .forEach(result::addAll);
        result.remove(node);
        return result;
    }
}
