package com.thenuber.aaga;

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
           double epsilon = 0.1; // accuracy parameter : smaller = more accurate
           double delta = 0.1;   // probability parameter : smaller = more reliable but increased sample size
           double c = 1.0;       // constant (can be adjusted)

           int vertexDiameter = g.getVertexDiameter();

           // Calculate sample size r
           int r = computeSampleSize(vertexDiameter, epsilon, delta, c);
           System.out.println("Sample size r: " + r);
    }
}
