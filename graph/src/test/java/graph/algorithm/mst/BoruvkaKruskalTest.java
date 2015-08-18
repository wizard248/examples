package graph.algorithm.mst;

import graph.algorithm.mst.BoruvkaKruskal;
import graph.model.Graph;
import graph.model.GraphEdge;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test suite for Boruvka-Kruskal`s algorithm.
 * Legend: g = original graph, mst = minimum spanning tree
 */
public class BoruvkaKruskalTest {
    @Test
    public void testEmpty() {
        final Graph<String, Integer> g = new Graph<>();

        final Graph<String, Integer> mst = BoruvkaKruskal.compute(g);
        assertEquals(0, mst.getAllNodes().size());
        assertEquals(0, mst.getAllEdges().size());
    }

    @Test
    public void testSingleNode() {
        final Graph<String, Integer> g = new Graph<>();
        g.addNode("a");

        final Graph<String, Integer> mst = BoruvkaKruskal.compute(g);
        assertEquals(1, mst.getAllNodes().size());
        assertEquals(0, mst.getAllEdges().size());
    }

    @Test
    public void testSingleEdge() {
        final Graph<String, Integer> g = new Graph<>();
        g.addNode("a");
        g.addNode("b");
        g.addEdge("a", "b", 10);

        final Graph<String, Integer> mst = BoruvkaKruskal.compute(g);
        assertEquals(2, mst.getAllNodes().size());
        assertEquals(1, mst.getAllEdges().size());
        assertTrue(mst.hasEdge("a", "b"));
        assertEquals(10, mst.getAllEdges().stream().mapToInt(GraphEdge::getValue).sum());
    }

    @Test
    public void testRegularCase() {
        final Graph<String, Integer> g = new Graph<>();
        Arrays.asList("a", "b", "c", "d", "e", "f").forEach(g::addNode);
        g.addEdge("a", "b", 5);
        g.addEdge("b", "d", 2);
        g.addEdge("b", "f", 3);
        g.addEdge("b", "c", 1);
        g.addEdge("b", "e", 3);
        g.addEdge("d", "f", 9);
        g.addEdge("d", "e", 1);

        final Graph<String, Integer> mst = BoruvkaKruskal.compute(g);
        assertEquals(6, mst.getAllNodes().size());
        assertEquals(5, mst.getAllEdges().size());
        assertTrue(mst.getEdge("a", "b").isPresent());
        assertTrue(mst.getEdge("b", "c").isPresent());
        assertTrue(mst.getEdge("b", "d").isPresent());
        assertTrue(mst.getEdge("b", "f").isPresent());
        assertTrue(mst.getEdge("d", "e").isPresent());
        assertEquals(12, mst.getAllEdges().stream().mapToInt(GraphEdge::getValue).sum());
    }

    @Test
    public void testComplexCase() {
        final Graph<String, Integer> g = new Graph<>();
        Arrays.asList("a", "b", "c", "d", "e", "f", "g").forEach(g::addNode);
        g.addEdge("a", "b", 7);
        g.addEdge("a", "d", 5);
        g.addEdge("b", "d", 9);
        g.addEdge("b", "c", 8);
        g.addEdge("b", "e", 7);
        g.addEdge("c", "e", 5);
        g.addEdge("d", "e", 15);
        g.addEdge("d", "f", 6);
        g.addEdge("f", "e", 8);
        g.addEdge("f", "g", 11);
        g.addEdge("g", "e", 9);

        final Graph<String, Integer> mst = BoruvkaKruskal.compute(g);
        assertEquals(7, mst.getAllNodes().size());
        assertEquals(6, mst.getAllEdges().size());
        assertTrue(mst.getEdge("f", "d").isPresent());
        assertTrue(mst.getEdge("d", "a").isPresent());
        assertTrue(mst.getEdge("a", "b").isPresent());
        assertTrue(mst.getEdge("b", "e").isPresent());
        assertTrue(mst.getEdge("e", "c").isPresent());
        assertTrue(mst.getEdge("e", "g").isPresent());
        assertEquals(39, mst.getAllEdges().stream().mapToInt(GraphEdge::getValue).sum());
    }
}