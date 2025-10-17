package com.thenuber.aaga;

import java.util.*;

public class GirvanNewman {
    // Standard GN: compute edge betweenness, remove highest, record partition after each removal step
    public static List<Map<String, Integer>> run(SimpleGraph input) {
        SimpleGraph g = copyOf(input);
        List<Map<String, Integer>> partitions = new ArrayList<>();
        while (g.edgeCount() > 0) {
            Map<Edge, Double> eb = edgeBetweenness(g);
            double max = eb.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            List<Edge> toRemove = new ArrayList<>();
            for (Map.Entry<Edge, Double> e : eb.entrySet()) {
                if (Double.compare(e.getValue(), max) == 0) toRemove.add(e.getKey());
            }
            for (Edge e : toRemove) g.removeEdge(e.u, e.v);
            partitions.add(componentPartition(g));
        }
        return partitions;
    }

    static SimpleGraph copyOf(SimpleGraph g) {
        SimpleGraph c = new SimpleGraph();
        for (String v : g.vertices()) c.addVertex(v);
    for (Edge e : g.edges()) c.addEdge(e.u, e.v);
        return c;
    }

    // Brandes for edge betweenness
    public static Map<Edge, Double> edgeBetweenness(SimpleGraph g) {
        Map<Edge, Double> bc = new HashMap<>();
        for (Edge e : g.edges()) bc.put(e, 0.0);
        for (String s : g.vertices()) {
            Deque<String> stack = new ArrayDeque<>();
            Map<String, List<String>> pred = new HashMap<>();
            Map<String, Integer> dist = new HashMap<>();
            Map<String, Integer> sigma = new HashMap<>();
            for (String v : g.vertices()) { pred.put(v, new ArrayList<>()); dist.put(v, -1); sigma.put(v, 0); }
            dist.put(s, 0); sigma.put(s, 1);
            Queue<String> q = new ArrayDeque<>(); q.add(s);
            while (!q.isEmpty()) {
                String v = q.remove();
                stack.push(v);
                for (String w : g.neighbors(v)) {
                    if (dist.get(w) < 0) { dist.put(w, dist.get(v) + 1); q.add(w); }
                    if (dist.get(w) == dist.get(v) + 1) { sigma.put(w, sigma.get(w) + sigma.get(v)); pred.get(w).add(v); }
                }
            }
            Map<String, Double> delta = new HashMap<>();
            for (String v : g.vertices()) delta.put(v, 0.0);
            while (!stack.isEmpty()) {
                String w = stack.pop();
                for (String v : pred.get(w)) {
                    double c = (sigma.get(w) == 0 ? 0.0 : ((double) sigma.get(v) / (double) sigma.get(w)) * (1.0 + delta.get(w)));
                    Edge e = new Edge(v, w);
                    bc.put(e, bc.getOrDefault(e, 0.0) + c);
                    delta.put(v, delta.get(v) + c);
                }
            }
        }
        // undirected graph: divide by 2
        for (Edge e : new ArrayList<>(bc.keySet())) bc.put(e, bc.get(e) / 2.0);
        return bc;
    }

    public static Map<String, Integer> componentPartition(SimpleGraph g) {
        Map<String, Integer> part = new HashMap<>();
        int cid = 0;
        Set<String> seen = new HashSet<>();
        for (String v : g.vertices()) {
            if (seen.contains(v)) continue;
            Queue<String> q = new ArrayDeque<>(); q.add(v); seen.add(v);
            while (!q.isEmpty()) {
                String u = q.remove();
                part.put(u, cid);
                for (String w : g.neighbors(u)) if (!seen.contains(w)) { seen.add(w); q.add(w); }
            }
            cid++;
        }
        return part;
    }
}
