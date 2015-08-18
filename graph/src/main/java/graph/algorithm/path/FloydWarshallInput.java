package graph.algorithm.path;

import java.util.List;
import java.util.Optional;

public interface FloydWarshallInput<NODE> {
    List<NODE> getNodes();

    Optional<Integer> getDistance(NODE a, NODE b);
}
