import java.util.List;
import java.util.Optional;

public interface Input<NODE> {
    List<NODE> getNodes();

    Optional<Integer> getDistance(NODE a, NODE b);
}
