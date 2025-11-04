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
        SimpleGraph g = new SimpleGraph(input);
        List<Map<Vertex, Integer>> partitions = new ArrayList<>();

        while (g.edgeCount() > 0) {
            Map<Edge, Double> eb = sampledEdgeBetweenness(g);

            double max = eb.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            List<Edge> toRemove = new ArrayList<>();
            for (Map.Entry<Edge, Double> e : eb.entrySet()) {
                if (Double.compare(e.getValue(), max) == 0) {
                    toRemove.add(e.getKey());
                }
            }

            for (Edge e : toRemove) {
                g.removeEdge(e.u, e.v);
            }

            partitions.add(g.getConnectedComponents());
        }

        return partitions;
    }

    public Map<Edge, Double> sampledEdgeBetweenness(SimpleGraph input) {
        if (vertexDiameter == -1) {
            vertexDiameter = getVertexDiameterApproximation(input);
        }
        //System.out.println("Vertex diameter VD(G): " + vertexDiameter);

        int r = computeSampleSize();
        //System.out.println("Sample size r: " + r);

        Map<Edge, Double> bc = new HashMap<>();
        Map<Vertex, Integer> ccPartition = input.getConnectedComponents();
        Random rng = new Random();

        for (int k = 0; k < r; k++) {
            Vertex u = input.randomNode();
            Vertex v = input.randomNode();
            int safety = 0;

            // Ensure distinct and connected vertices
            while ((u.equals(v) || !Objects.equals(ccPartition.get(u), ccPartition.get(v))) && safety < 1000) {
                v = input.randomNode();
                safety++;
            }

            if (safety >= 1000) continue; // avoid infinite loops if disconnected

            List<Edge> randomShortestPath = computeRandomShortestPath(input, u, v);

            if (randomShortestPath.isEmpty()) continue; // skip unreachable pairs

            for (Edge e : randomShortestPath) {
                double value = bc.getOrDefault(e, 0.0);
                bc.put(e, value + 1.0 / r);
            }
        }

        return bc;
    }

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

        int d = (int) (Math.log(vertexDiameter - 2) / Math.log(2)) + 1;
        double lnInvDelta = Math.log(1.0 / delta);
        double r = c / (epsilon * epsilon) * (d + lnInvDelta);
        return (int) Math.ceil(r);
    }

    public int getVertexDiameterApproximation(SimpleGraph g) {
        int VD = 0;
        for (int i = 0; i < vdSamples; i++) {
            Vertex source = g.randomNode();
            ArrayList<Integer> distanceValues = new ArrayList<>(g.getDistances(source).values());
            if (distanceValues.isEmpty()) continue;
            distanceValues.sort((x1, x2) -> -Integer.compare(x1, x2));

            int diameterSample = distanceValues.size() < 2
                    ? distanceValues.get(0)
                    : distanceValues.get(0) + distanceValues.get(1);

            VD += diameterSample;
        }
        return (VD + vdSamples) / vdSamples;
    }

    public List<Edge> computeRandomShortestPath(SimpleGraph g, Vertex source, Vertex target) {
        Map<Vertex, List<Vertex>> preds = new HashMap<>();
        Map<Vertex, Integer> distances = new HashMap<>();
        Map<Vertex, Integer> sigma = new HashMap<>();

        sigma.put(source, 1);
        distances.put(source, 0);

        Queue<Vertex> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            Vertex v = queue.remove();
            Collection<Vertex> neighbors = g.neighbors(v);

            if (neighbors == null || neighbors.isEmpty()) continue;

            for (Vertex w : neighbors) {
                if (!distances.containsKey(w)) {
                    distances.put(w, distances.getOrDefault(v, 0) + 1);
                    queue.add(w);
                }

                if (distances.get(w) == distances.get(v) + 1) {
                    sigma.put(w, sigma.getOrDefault(w, 0) + sigma.getOrDefault(v, 0));
                    preds.putIfAbsent(w, new ArrayList<>());
                    preds.get(w).add(v);
                }
            }
        }

        // If target not reached, no path
        if (!distances.containsKey(target) || !preds.containsKey(target)) {
            return Collections.emptyList();
        }

        List<Edge> randomShortestPath = new ArrayList<>();
        Random rng = new Random();
        Vertex v = target;

        while (!v.equals(source)) {
            List<Vertex> vPreds = preds.get(v);
            if (vPreds == null || vPreds.isEmpty()) return Collections.emptyList();

            int sigmaV = sigma.getOrDefault(v, 1);
            int winner = rng.nextInt(sigmaV);
            int tickets = 0;
            Vertex u = null;

            for (Vertex p : vPreds) {
                tickets += sigma.getOrDefault(p, 1);
                if (tickets > winner) {
                    u = p;
                    break;
                }
            }

            if (u == null) return Collections.emptyList();

            randomShortestPath.add(new Edge(u, v));
            v = u;
        }

        return randomShortestPath;
    }
}
