package graph.algorithm.mst;

import graph.model.Graph;
import graph.model.GraphEdge;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Boruvka-Kruskal`s algorithm.
 */
public final class BoruvkaKruskal {
    /**
     * Finds the minimum spanning tree of the given graph.
     *
     * @param originalGraph original graph
     * @param <NV> node value type
     * @return minimum spanning tree
     */
    public static <NV> Graph<NV, Integer> compute(final Graph<NV, Integer> originalGraph) {
        // STEP 1
        // Sort edges by weight (ascending).

        final List<GraphEdge<NV, Integer>> sortedEdges = originalGraph.getAllEdges()
                .stream()
                .sorted(Comparator.comparingInt(GraphEdge::getValue))
                .collect(Collectors.toList());

        // STEP 2
        // Create empty graph with all the nodes of the original graph.
        // But note there are no edges added.

        final Graph<NV, Integer> minimumSpanningTree = new Graph<>();
        originalGraph.getAllNodes().forEach(minimumSpanningTree::addNode);

        // STEP 3
        // Iterate over all sorted edges and add each, unless they cause a cycle. Skip the rest.

        sortedEdges
                .stream()
                .filter(edge -> !minimumSpanningTree.hasPath(edge.getEither(), edge.getAnother()))
                .forEach(edge -> minimumSpanningTree.addEdge(edge.getEither(), edge.getAnother(), edge.getValue()));

        return minimumSpanningTree;
    }
}
