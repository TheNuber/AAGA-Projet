package com.thenuber.aaga;

import java.util.*;

public class BetweennessSamplingAlgo {

    private int vertexDiameter = -1;
    private double epsilon = 0.2; // accuracy parameter : smaller = more accurate
    private double delta = 0.3; // probability parameter : smaller = more reliable but increased sample size
    private double c = 1.0; // constant (can be adjusted)

    public BetweennessSamplingAlgo() { }

    public void setVertexDiameter(int vertexDiameter) { this.vertexDiameter = vertexDiameter; }
    public void setEpsilon(double epsilon) { this.epsilon = epsilon; }
    public void setDelta(double delta) { this.delta = delta; }
    public void setC(double c) { this.c = c; }



    /*
     * Computes the sample size r for betweenness approximation.
     * vertexDiameter : VD(G) - the vertex-diameter of the graph
     * epsilon : ε - accuracy parameter (0 < ε < 1)
     * delta : δ - probability parameter (0 < δ < 1)
     * c : constant (theoretical or empirical)
     * return sample size r
     */
    public int computeSampleSize() {
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

        if (vertexDiameter == -1) {
            vertexDiameter = g.getVertexDiameterApproximation();
        }
        System.out.println("Vertex diameter VD(G): " + vertexDiameter);

        // 0. Calculate sample size r
        int r = computeSampleSize();
        System.out.println("Sample size r: " + r);

        Set<String> vertices = g.vertices();
        Random rnd = new Random();

        Map<Edge, Double> bc = new HashMap<>(); // betweenness par arête

        // Repeating r times
        for (int k = 0; k < r; k++) {

            // 1. Select random distinct nodes
            int i = rnd.nextInt(g.vertexCount());
            int j = rnd.nextInt(g.vertexCount());
            while (i == j) {
                j = rnd.nextInt(g.vertexCount());
            }
            String u = (String) vertices.toArray()[i];
            String v = (String) vertices.toArray()[j];

            // 2. Compute a random shortest path between them
            List<Edge> randomShortestPath = computeRandomShortestPath(g, u, v);

            // 3. For every edge in the random shortest path, increase its approximate betweenness centrality
            for (Edge e : randomShortestPath) {
                double value = bc.getOrDefault(e, 0.0);
                bc.put(e, value+1/r);
            }
        }

    }

    public static List<Edge> computeRandomShortestPath(SimpleGraph g, String source, String target) {

        // First part: compute values for predecessors and number of shortest paths from source
        // Very similar to first part of Girvan Newman algorithm
        // Complexity: O(V+E)

        Map<String, List<String>> preds = new HashMap<>();
        Map<String, Integer> distances = new HashMap<>();
        Map<String, Integer> sigma = new HashMap<>();

        sigma.put(source, 1);
        Queue<String> queue = new ArrayDeque<>();
        queue.add(source);
        while(!queue.isEmpty()) {
            String v = queue.remove();
            for (String w : g.neighbors(v)) {
                // Calculate distance
                if (!distances.containsKey(w)) {
                    distances.put(w, distances.get(v) + 1);
                    queue.add(w);
                }

                // Check predecessor
                if (distances.get(w) == distances.get(v)+1) {
                    sigma.put(w, sigma.get(w) + sigma.get(v));
                    preds.putIfAbsent(w, new ArrayList<>());
                    preds.get(w).add(v);
                }
            }
        }

        // Second part: obtain the random path from source to target
        // Complexity: O(V)

        Random rng = new Random();

        List<Edge> randomShortestPath = new ArrayList<>();
        String v = target;
        while(!v.equals(source)) {
            /**
             * Select a predecessor randomly
             * Probability of choosing predecesor u is sigma.get(u) / sigma.get(v)
             * 
             * 1. Generate a random winner ticket in [0, sigma.get(v))
             * 2. Each predecesor "u" holds the next "sigma.get(u)" tickets, starting from 0
             * 3. The predecessor with the winner ticket wins
             */
            int winner = rng.nextInt(sigma.get(v));
            int tickets = 0;

            Iterator<String> it = preds.get(v).iterator();
            String u = null;
            do {
                u = it.next();
                tickets += sigma.get(u);
            } while (tickets < winner);

            // Add edge to random shortest path
            randomShortestPath.add(new Edge(u,v));

            // Now look predecessors of u
            v = u;
        }

        return randomShortestPath;
    }

}
