package com.thenuber.aaga;

import java.util.*;

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
public class GirvanNewman {
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
    public static List<Map<String, Integer>> run(SimpleGraph input) {

        // Deepcopy of input graph
        SimpleGraph g = new SimpleGraph(input);

        // List of all connected components partitions obtained with the algorithm
        List<Map<String, Integer>> partitions = new ArrayList<>();

        while (g.edgeCount() > 0) {
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
            partitions.add(g.getConnectedComponents());
        }
        return partitions;
    }

    /**
     * edgeBetweenness
     * - Implémentation de Brandes (graphe non pondéré) adaptée aux ARÊTES.
     * - Principe:
     * * Pour chaque source s, on fait un BFS pour obtenir dist[], sigma[], preds[]:
     * - dist[x]: distance de s à x.
     * - sigma[x]: nb. de plus courts chemins de s vers x.
     * - preds[x]: prédécesseurs de x sur des plus courts chemins depuis s.
     * * Puis on accumule les dépendances delta[] en dépilant un stack (ordre
     * topologique
     * des distances décroissantes), ce qui permet de calculer les contributions sur
     * chaque arête (v,w) appartenant à au moins un plus court chemin de s vers w.
     * - Complexité: O(VE) par passe (toutes sources s).
     * - Pour graphe non orienté, division finale par 2.
     */
    public static Map<Edge, Double> edgeBetweenness(SimpleGraph g) {
        Map<Edge, Double> bc = new HashMap<>(); // betweenness par arête
        for (Edge e : g.edges())
            bc.put(e, 0.0);
        for (String s : g.vertices()) {
            // Phase BFS (plus courts chemins depuis s)
            Deque<String> stack = new ArrayDeque<>(); // ordre de visite pour l'accumulation
            Map<String, List<String>> pred = new HashMap<>(); // prédécesseurs de w sur les plus courts chemins
            Map<String, Integer> dist = new HashMap<>(); // distances depuis s
            Map<String, Integer> sigma = new HashMap<>(); // nb. de plus courts chemins de s vers v
            for (String v : g.vertices()) {
                pred.put(v, new ArrayList<>());
                dist.put(v, -1);
                sigma.put(v, 0);
            }
            dist.put(s, 0);
            sigma.put(s, 1);
            Queue<String> q = new ArrayDeque<>();
            q.add(s);
            while (!q.isEmpty()) {
                String v = q.remove();
                stack.push(v);
                for (String w : g.neighbors(v)) {
                    // Découverte de w
                    if (dist.get(w) < 0) {
                        dist.put(w, dist.get(v) + 1);
                        q.add(w);
                    }
                    // V est un prédécesseur valide de w si w est à une distance +1
                    if (dist.get(w) == dist.get(v) + 1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        pred.get(w).add(v);
                    }
                }
            }
            // Phase d'accumulation: contributions en remontant depuis les plus éloignés
            Map<String, Double> delta = new HashMap<>();
            for (String v : g.vertices())
                delta.put(v, 0.0);
            while (!stack.isEmpty()) {
                String w = stack.pop();
                for (String v : pred.get(w)) {
                    // Contribution d'une arête (v,w) sur les plus courts chemins via w
                    double c = (sigma.get(w) == 0 ? 0.0 : ((double) sigma.get(v) / (double) sigma.get(w)) * (1.0 + delta.get(w)));
                    Edge e = new Edge(v, w);
                    bc.put(e, bc.getOrDefault(e, 0.0) + c);
                    delta.put(v, delta.get(v) + c);
                }
            }
        }
        // Graphe non orienté: chaque chemin contribue deux fois → division par 2
        for (Edge e : new ArrayList<>(bc.keySet())) {
            bc.put(e, bc.get(e) / 2.0);
        }
        return bc;
    }
}
