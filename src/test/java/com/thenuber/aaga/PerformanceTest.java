package com.thenuber.aaga;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;

import java.util.*;
import java.util.stream.Stream;


import static org.junit.jupiter.api.Assertions.assertTrue;



public class PerformanceTest {
    // Define your algorithms as a stream
    static Stream<GraphAlgorithm> algorithms() {
        return Stream.of(
            new GirvanNewman(),
            new GirvanNewmanRevised(),
            new BetweennessSamplingAlgo()
        );
    }

    // Define your datasets
    static Stream<String> datasets() {
        return Stream.of(
            ""
        );
    }
    
    @TestTemplate
    void testAlgorithmPerformance(TestCase testCase) {
        long startTime = System.nanoTime();
        
        testCase.algorithm.process(testCase.dataset);
        
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        
        System.out.printf("Algorithm: %s, Dataset: %s, Time: %d ms%n",
            testCase.algorithm.getName(),
            testCase.dataset.getName(),
            duration);
    }
    
    // Combine algorithms and datasets
    static Stream<TestCase> algorithmDatasetCombinations() {
        return algorithms().flatMap(algo ->
            datasets().map(data -> new TestCase(algo, data))
        );
    }
    
    static class TestCase {
        GraphAlgorithm algorithm;
        Path dataset;
        
        TestCase(Algorithm algorithm, Dataset dataset) {
            this.algorithm = algorithm;
            this.dataset = dataset;
        }
        
        @Override
        public String toString() {
            return algorithm.getName() + " on " + dataset.getName();
        }
    }

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
