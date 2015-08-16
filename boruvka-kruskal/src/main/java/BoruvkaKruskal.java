import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by voho on 14.08.15.
 */
public class BoruvkaKruskal {
    public static <NV> Graph<NV, Integer> compute(Graph<NV, Integer> graph) {
        List<GraphEdge<NV, Integer>> edges = graph.getAllEdges()
                .stream()
                .sorted(Comparator.comparingInt(GraphEdge::getValue))
                .collect(Collectors.toList());

        Graph<NV, Integer> result = new Graph<>();

        for (GraphEdge<NV, Integer> edge : edges) {
            // add edge
        }

        return result;
    }
}
