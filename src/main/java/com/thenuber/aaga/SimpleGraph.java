package com.thenuber.aaga;

import java.util.*;

// Undirected simple graph with String vertices and unique edges
public class SimpleGraph {
    private final Map<String, Set<String>> adj = new HashMap<>();
    private int edgeCount = 0;

    public Set<String> vertices() { return Collections.unmodifiableSet(adj.keySet()); }
    public int vertexCount() { return adj.size(); }
    public int edgeCount() { return edgeCount; }

    public void addVertex(String v) { adj.computeIfAbsent(v, k -> new HashSet<>()); }

    public boolean containsEdge(String a, String b) {
        Set<String> s = adj.get(a);
        return s != null && s.contains(b);
    }

    public void addEdge(String a, String b) {
        if (a.equals(b)) return; // no self-loops
        addVertex(a); addVertex(b);
        if (!adj.get(a).contains(b)) {
            adj.get(a).add(b);
            adj.get(b).add(a);
            edgeCount++;
        }
    }

    public void removeEdge(String a, String b) {
        if (containsEdge(a,b)) {
            adj.get(a).remove(b);
            adj.get(b).remove(a);
            edgeCount--;
        }
    }

    public Set<String> neighbors(String v) { return Collections.unmodifiableSet(adj.getOrDefault(v, Collections.emptySet())); }

    public List<Edge> edges() {
        List<Edge> list = new ArrayList<>();
        for (var e : adj.entrySet()) {
            String u = e.getKey();
            for (String v : e.getValue()) {
                if (u.compareTo(v) < 0) list.add(new Edge(u,v));
            }
        }
        return list;
    }
}
