package com.thenuber.aaga;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModularityTest {
    @Test
    public void testSimpleTwoCommunities() {
        SimpleGraph g = new SimpleGraph();

        Vertex v1 = g.addVertex("a");
        Vertex v2 = g.addVertex("b");
        Vertex v3 = g.addVertex("c");
        Vertex v4 = g.addVertex("d");

        g.addEdge(v1, v2);
        g.addEdge(v3, v4);

        Map<Vertex,Integer> part = g.getConnectedComponents();

        double q = Modularity.compute(g, part);
        assertTrue(q >= 0.0);
    }
}
