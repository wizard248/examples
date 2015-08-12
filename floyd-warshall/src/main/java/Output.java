import java.util.List;
import java.util.Optional;

public interface Output<NODE> {
    Optional<Integer> getMinimalDistance(NODE a, NODE b);

    Optional<List<NODE>> getShortestPath(NODE a, NODE b);
}
