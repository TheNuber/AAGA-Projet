package com.thenuber.aaga;

import java.util.*;
import java.util.stream.Collector;

/**
 * GirvanNewman
 * - Implémentation standard de l'algorithme de Girvan–Newman (GN) pour la
 * détection de communautés.
 * - Stratégie: supprimer itérativement les arêtes les plus "centrales"
 * (betweenness élevée),
 * et enregistrer la partition (composantes connexes) après chaque suppression.
 *
 * Fonctions clés:
 * - run(g): exécute GN, renvoie la liste des partitions successives.
 * - edgeBetweenness(g): calcule l'intermédiarité des arêtes via Brandes (adapté
 * aux arêtes).
 * - componentPartition(g): calcule la partition (composantes connexes)
 * courante.
 */
public class GirvanNewmanRevised extends GirvanNewman {
    /**
     * - Entrée: un graphe SimpleGraph (copié en interne pour ne pas modifier
     * l'original).
     * - Boucle:
     * 1) Calculer la betweenness des arêtes.
     * 2) Trouver la valeur maximale et l'ensemble des arêtes ex-aequo.
     * 3) Supprimer ces arêtes.
     * 4) Enregistrer la partition (composantes connexes) après suppression.
     * - Sortie: liste ordonnée des partitions (du graphe initial jusqu'au graphe
     * sans arêtes).
     */
    public List<Map<Vertex, Integer>> run(SimpleGraph input) {

        // Deepcopy of input graph
        SimpleGraph g = new SimpleGraph(input);

        // List of all connected components partitions obtained with the algorithm
        List<Map<Vertex, Integer>> partitions = new ArrayList<>();

        // First iteration: similar to the one in standard Girvan-Newmann

        // 1. Calculate edge betweenness for all edges
        Map<Edge, Double> eb = edgeBetweenness(g);

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
        Map<Vertex,Integer> nextCCPartition = g.getConnectedComponents();
        partitions.add(nextCCPartition);

        // Next iterations: recalculate betweenness only in the affected connected
        // components
        while (g.edgeCount() > 0) {

            // Obtain all the vertices whose shortest paths may have been affected
            // That is, the vertices in the connected components of the removed edges

            Set<Integer> affectedComponents = new HashSet<>();
            for (Edge e : toRemove) {
                affectedComponents.add(nextCCPartition.get(e.u));
                affectedComponents.add(nextCCPartition.get(e.v));
            }

            List<Vertex> affectedVertices = new ArrayList<>(); 
            for (Vertex v : g.vertices()) {
                if (affectedComponents.contains(nextCCPartition.get(v))) {
                    affectedVertices.add(v);
                }
            }

            // 1. Recalculate edge betweenness
            eb = recalculateEdgeBetweenness(g, eb, affectedVertices);

            // 2. Get all edges with maximum edge betweenness
            max = eb.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
            toRemove = new ArrayList<>();
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
            nextCCPartition = g.getConnectedComponents();
            partitions.add(nextCCPartition);
        }

        return partitions;

    }

    
    public Map<Edge, Double> recalculateEdgeBetweenness(SimpleGraph g, Map<Edge,Double> edge_betweenness, Collection<Vertex> affectedVertices) {

        
        for (Vertex s : affectedVertices) {

            // Phase BFS (plus courts chemins depuis s)
            Deque<Vertex> stack = new ArrayDeque<>(); // ordre de visite pour l'accumulation
            Map<Vertex, List<Vertex>> pred = new HashMap<>(); // prédécesseurs de w sur les plus courts chemins
            Map<Vertex, Integer> distances = new HashMap<>(); // distances depuis s
            Map<Vertex, Integer> sigma = new HashMap<>(); // nb. de plus courts chemins de s vers v
            for (Vertex v : affectedVertices) {
                pred.put(v, new ArrayList<>());
                distances.put(v, -1);
                sigma.put(v, 0);
            }

            distances.put(s, 0);
            sigma.put(s, 1);
            Queue<Vertex> queue = new ArrayDeque<>();
            queue.add(s);
            while (!queue.isEmpty()) {
                Vertex v = queue.remove();
                stack.push(v);
                for (Vertex w : g.neighbors(v)) {
                    // Reset edge_betweenness to zero
                    edge_betweenness.put(new Edge(v,w), 0.0);

                    // Découverte de w
                    if (distances.get(w) < 0) {
                        distances.put(w, distances.get(v) + 1);
                        queue.add(w);
                    }
                    // V est un prédécesseur valide de w si w est à une distance +1
                    if (distances.get(w) == distances.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        pred.get(w).add(v);
                    }
                }
            }

            // Phase d'accumulation: contributions en remontant depuis les plus éloignés
            Map<Vertex, Double> delta = new HashMap<>();
            for (Vertex v : g.vertices())
                delta.put(v, 0.0);

            while (!stack.isEmpty()) {
                Vertex w = stack.pop();
                for (Vertex v : pred.get(w)) {
                    if (sigma.get(w) != 0) {
                        double sv_sw = (double) sigma.get(v) / (double) sigma.get(w);
                        double dw = delta.get(w);
                        delta.put(v, delta.get(v) + sv_sw * (1.0 + dw));
                        Edge e = new Edge(v, w);
                        edge_betweenness.put(e, edge_betweenness.get(e) + sv_sw * dw);
                    }
                }
            }
        }
        // Graphe non orienté: chaque chemin contribue deux fois → division par 2
        for (Edge e : new ArrayList<>(edge_betweenness.keySet())) {
            edge_betweenness.put(e, edge_betweenness.get(e) / 2.0);
        }
        return edge_betweenness;
    }
}
