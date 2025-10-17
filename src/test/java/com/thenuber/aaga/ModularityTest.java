package com.thenuber.aaga;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModularityTest {
    @Test
    public void testSimpleTwoCommunities() {
        SimpleGraph g = new SimpleGraph();
        g.addEdge("a","b");
        g.addEdge("c","d");
        Map<String,Integer> part = new HashMap<>();
        part.put("a", 0); part.put("b", 0); part.put("c",1); part.put("d",1);
        double q = Modularity.compute(g, part);
        assertTrue(q >= 0.0);
    }
}
