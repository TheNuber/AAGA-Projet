package com.thenuber.aaga;

import java.util.*;

public class BetweennessSamplingAlgo implements GraphAlgorithm {

    private int vertexDiameter = -1;
    private int vdSamples = 10;
    private double epsilon = 0.2; // accuracy parameter : smaller = more accurate
    private double delta = 0.3; // probability parameter : smaller = more reliable but increased sample size
    private double c = 1.0; // constant (can be adjusted)

    public BetweennessSamplingAlgo() { }

    public void setVertexDiameter(int vertexDiameter) { this.vertexDiameter = vertexDiameter; }
    public void setVDSamples(int vdSamples) { this.vdSamples = vdSamples; }
    public void setEpsilon(double epsilon) { this.epsilon = epsilon; }
    public void setDelta(double delta) { this.delta = delta; }
    public void setC(double c) { this.c = c; }


    public List<Map<Vertex, Integer>> run(SimpleGraph input) {

        // Deepcopy of input graph
        SimpleGraph g = new SimpleGraph(input);

        // List of all connected components partitions obtained with the algorithm
        List<Map<Vertex, Integer>> partitions = new ArrayList<>();

        while (g.edgeCount() > 0) {
            // 1. Calculate edge betweenness for all edgess
            Map<Edge, Double> eb = sampledEdgeBetweenness(g);

            // 2. Get all edges with maximum edge betweenness
            double max = eb.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            List<Edge> toRemove = new ArrayList<>();
            for (Map.Entry<Edge, Double> e : eb.entrySet()) {
                if (Double.compare(e.getValue(), max) == 0) {
                    toRemove.add(e.getKey());
                }
            }

            // 3. Remove all those edges
            for (Edge e : toRemove) {
                g.removeEdge(e.u, e.v);
            }

            // 4. Calculate and save the current connected components partition
            partitions.add(g.getConnectedComponents());
        }
        return partitions;
    }


    
    public Map<Edge,Double> sampledEdgeBetweenness(SimpleGraph input) {

        // Calculate vertex diameter if not specified
        if (vertexDiameter == -1) {
            vertexDiameter = getVertexDiameterApproximation(input);
        }
        System.out.println("Vertex diameter VD(G): " + vertexDiameter);

        // Calculate sample size r
        int r = computeSampleSize();
        System.out.println("Sample size r: " + r);

        Map<Edge, Double> bc = new HashMap<>(); // betweenness par arête

        // Repeating r times
        for (int k = 0; k < r; k++) {

            // 1. Select random distinct nodes
            Vertex u = (Vertex) input.randomNode();
            Vertex v = (Vertex) input.randomNode();
            while (u == v) {
                v = input.randomNode();
            }

            // 2. Compute a random shortest path between them
            List<Edge> randomShortestPath = computeRandomShortestPath(input, u, v);

            // 3. For every edge in the random shortest path, increase its approximate betweenness centrality
            for (Edge e : randomShortestPath) {
                double value = bc.getOrDefault(e, 0.0);
                bc.put(e, value+1/r);
            }
        }

        return bc;

    }



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


    /**
     * Approximate the vertex diameter through the mean of repeated samples 
     * Each sample is the length of the concatenation of the two longest shortest paths from a random vertex
     */
    public int getVertexDiameterApproximation(SimpleGraph g) {
        int VD = 0;
        for (int i = 0; i < vdSamples; i++) {
            Vertex source = g.randomNode();
            ArrayList<Integer> distanceValues = new ArrayList<>(g.getDistances(source).values());
            distanceValues.sort((x1, x2) -> - Integer.compare(x1,x2));
            
            // Sometimes the random node is a leaf, so there is only one possible path
            int diameterSample = distanceValues.size() < 2 ? distanceValues.get(0) : distanceValues.get(0) + distanceValues.get(1);
            VD += diameterSample;
        }
        // Ceil out the integer division (for example 0.555 -> 1)
        return (VD+vdSamples) / vdSamples;
    }

    public List<Edge> computeRandomShortestPath(SimpleGraph g, Vertex source, Vertex target) {

        // First part: compute values for predecessors and number of shortest paths from source
        // Very similar to first part of Girvan Newman algorithm
        // Complexity: O(V+E)

        Map<Vertex, List<Vertex>> preds = new HashMap<>();
        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Integer> sigma = new HashMap<>();

        sigma.put(source, 1);
        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(source);
        while(!queue.isEmpty()) {
            Vertex v = queue.remove();
            for (Vertex w : g.neighbors(v)) {
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
        Vertex v = target;
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

            Iterator<Vertex> it = preds.get(v).iterator();
            Vertex u = null;
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
