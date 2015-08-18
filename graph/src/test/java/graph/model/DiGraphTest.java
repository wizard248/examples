package graph.model;

import graph.model.DiGraph;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by voho on 16.08.15.
 */
public class DiGraphTest {
    @Test
    public void testConnected() {
        DiGraph<String, Integer> g = new DiGraph<>();
        g.addNode("a");
        g.addNode("b");
        g.addNode("c");
        g.addNode("d");

        assertFalse(g.hasPath("a", "b"));

        g.addEdge("a", "b", 0);

        assertTrue(g.hasPath("a", "b"));

        g.addEdge("b", "c", 0);

        assertTrue(g.hasPath("b", "c"));

        g.addEdge("b", "d", 0);

        assertTrue(g.hasPath("b", "d"));

        assertTrue(g.hasPath("a", "c"));
        assertTrue(g.hasPath("a", "d"));
        assertFalse(g.hasPath("c", "a"));
        assertFalse(g.hasPath("d", "a"));
    }

    @Test
    public void testConnectedLongTree() {
        DiGraph<String, Integer> g = new DiGraph<>();
        g.addNode("a");
        g.addNode("b");
        g.addNode("c");
        g.addNode("d");

        assertFalse(g.hasPath("a", "d"));

        g.addEdge("c", "d", 0);
        g.addEdge("b", "c", 0);
        g.addEdge("a", "b", 0);

        assertTrue(g.hasPath("a", "d"));
    }
}