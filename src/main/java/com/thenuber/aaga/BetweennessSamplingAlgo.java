package com.thenuber.aaga;

import java.util.*;
import com.thenuber.aaga.GirvanNewman;

public class BetweennessSamplingAlgo {
    /*
     * Computes the sample size r for betweenness approximation.
     * vertexDiameter : VD(G) - the vertex-diameter of the graph
     * epsilon : ε - accuracy parameter (0 < ε < 1)
     * delta : δ - probability parameter (0 < δ < 1)
     * c : constant (theoretical or empirical)
     * return sample size r
     */
    public static int computeSampleSize(int vertexDiameter, double epsilon, double delta, double c) {
        if (vertexDiameter <= 2) {
            throw new IllegalArgumentException("vertexDiameter must be greater than 2");
        }
        if (epsilon <= 0 || epsilon >= 1) {
            throw new IllegalArgumentException("epsilon must be in (0,1)");
        }
        if (delta <= 0 || delta >= 1) {
            throw new IllegalArgumentException("delta must be in (0,1)");
        }
        int d = (int) (Math.log(vertexDiameter - 2) / Math.log(2));
        d = d + 1;
        double lnInvDelta = Math.log(1.0 / delta);
        double r = c / (epsilon * epsilon) * (d + lnInvDelta);
        return (int) Math.ceil(r);
    }

    public void run(SimpleGraph g) {
        // Example parameters (set as needed)
        double epsilon = 0.2; // accuracy parameter : smaller = more accurate
        double delta = 0.3; // probability parameter : smaller = more reliable but increased sample size
        double c = 1.0; // constant (can be adjusted)

        int vertexDiameter = g.getVertexDiameter();
        System.out.println("Vertex diameter VD(G): " + vertexDiameter);

        // 0. Calculate sample size r
        int r = computeSampleSize(vertexDiameter, epsilon, delta, c);
        System.out.println("Sample size r: " + r);

        Set<String> verticies = g.vertices();
        Random rnd = new Random();

        int i = 0;
        int j = 0;

        Map<Edge, Double> bc = new HashMap<>(); // betweenness par arête

        // 1. Repeating r times
        for (int k = 0; k < r; k++) {
            i = rnd.nextInt(verticies.size());
            j = rnd.nextInt(verticies.size());
            while (i == j) {
                j = rnd.nextInt(verticies.size());
            }
            String u = (String) verticies.toArray()[i];
            String v = (String) verticies.toArray()[j];

            // 2. Calculate shortest paths between u and v (all shortest paths S_uv)
            List<List<String>> suv = computeAllShortestPaths(g, u, v);
            System.out.println("Sampled pair: u=" + u + " v=" + v + " -> |S_uv| = " + suv.size());
            // optional: print first few paths
            if (!suv.isEmpty()) {
                int limit = Math.min(3, suv.size());
                for (int p = 0; p < limit; p++) {
                    System.out.println(" path[" + p + "] = " + suv.get(p));
                }
            }
            // 3. Select a path p from Suv uniformly at random
            if (suv.isEmpty()) { 
                continue;
            }
            
            List<String> p = suv.get(rnd.nextInt(suv.size()));
            System.out.println("Selected path p: " + p);

            // 4. For every edge e in p, increment bc[e] by 1/r
            String der = "";
            for (String n : p) {
                if (der != "") {
                    Edge e = new Edge(der, n);
                    double d = bc.getOrDefault(e, 0.0);
                    bc.put(e, d+1/r);
                };
                der = n;
            }
        }

    }

    /**
     * Compute all shortest paths between source and target in an unweighted SimpleGraph.
     * Uses BFS to compute distances and predecessor lists, then backtracks to enumerate
     * all shortest paths. Returns an empty list if source/target missing or unreachable.
     */
    public static List<List<String>> computeAllShortestPaths(SimpleGraph g, String source, String target) {
        List<List<String>> result = new ArrayList<>();
        if (source == null || target == null) return result;
        // quick presence check
        if (!g.vertices().contains(source) || !g.vertices().contains(target)) return result;

        // BFS: distance map and predecessor lists
        Map<String, Integer> dist = new HashMap<>();
        Map<String, List<String>> preds = new HashMap<>();
        Queue<String> queue = new ArrayDeque<>();

        dist.put(source, 0);
        queue.add(source);

        while (!queue.isEmpty()) {
            String v = queue.remove();
            int dv = dist.get(v);
            for (String w : g.neighbors(v)) {
                // if w not seen yet
                if (!dist.containsKey(w)) {
                    dist.put(w, dv + 1);
                    preds.put(w, new ArrayList<>());
                    preds.get(w).add(v);
                    queue.add(w);
                } else {
                    // if we found another shortest predecessor
                    if (dist.get(w) == dv + 1) {
                        preds.get(w).add(v);
                    }
                }
            }
        }

        // if target unreachable, return empty
        if (!dist.containsKey(target)) return result;

        // Backtrack from target to source building all shortest paths using preds
        LinkedList<String> path = new LinkedList<>();
        path.addFirst(target);

        // recursive DFS via stack/explicit recursion
        Deque<Iterator<String>> stack = new ArrayDeque<>();
        Map<String, Iterator<String>> iterMap = new HashMap<>();

        String cur = target;
        while (true) {
            if (cur.equals(source)) {
                // record current path (from source .. target)
                result.add(new ArrayList<>(path));
            }

            List<String> curPreds = preds.getOrDefault(cur, Collections.emptyList());
            Iterator<String> it = iterMap.computeIfAbsent(cur, k -> curPreds.iterator());

            if (it.hasNext()) {
                String p = it.next();
                // push iterator for cur and move to predecessor
                stack.push(it);
                cur = p;
                path.addFirst(cur);
            } else {
                // backtrack
                if (stack.isEmpty()) break;
                // pop current iterator and move back one step in path
                stack.pop();
                // remove the head (which corresponds to current node)
                path.removeFirst();
                // set cur to the new head (if any) or source
                if (!path.isEmpty()) {
                    cur = path.getFirst();
                } else break;
            }
        }

        // The paths were built from source->...->target because we used addFirst when backtracking,
        // but ensure each path starts at source and ends at target (current construction does that).
        return result;
    }
}
