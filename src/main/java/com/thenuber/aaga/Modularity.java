package com.thenuber.aaga;

import java.util.HashMap;
import java.util.Map;

public class Modularity {
    // Compute modularity Q for a partition given as map vertex->community id
    public static double compute(SimpleGraph g, Map<String, Integer> partition) {
        double m = g.edgeCount();
        if (m == 0) return 0.0;
        Map<String, Integer> deg = new HashMap<>();
        for (String v : g.vertices()) deg.put(v, g.neighbors(v).size());
        double q = 0.0;
        for (String i : g.vertices()) {
            for (String j : g.vertices()) {
                if (!partition.get(i).equals(partition.get(j))) continue;
                double Aij = g.containsEdge(i, j) ? 1.0 : 0.0;
                q += (Aij - (deg.get(i) * deg.get(j)) / (2.0 * m));
            }
        }
        return q / (2.0 * m);
    }
}
