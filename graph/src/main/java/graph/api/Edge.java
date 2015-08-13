package graph.api;

import java.util.Set;

/**
 * Created by voho on 13.08.15.
 */
public interface Edge<NV, EV> {
    EV getValue();

    Set<Node<NV>> getIncidentNodes();

    default boolean incidentWithNode(Node<NV> node) {
        return getIncidentNodes().contains(node);
    }
}
