package graph.algorithm.path;

import java.util.List;
import java.util.Optional;

public interface FloydWarshallOutput<NODE> {
    Optional<Integer> getMinimalDistance(NODE a, NODE b);

    Optional<List<NODE>> getShortestPath(NODE a, NODE b);
}
