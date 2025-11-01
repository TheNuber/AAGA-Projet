package com.thenuber.aaga;

import java.util.*;

/**
 * SimpleGraph
 *  - Graphe simple, non orienté, sans multi-arêtes ni boucles, avec sommets de type String.
 *  - Représentation par listes d'adjacence: Map<Sommet, Set<Voisins>>.
 *  - Invariants:
 *      * Si b ∈ adj[a], alors a ∈ adj[b] (symétrie). Les arêtes sont non orientées.
 *      * edgeCount compte le nombre d'arêtes uniques (chaque arête {a,b} vaut 1).
 *      * Pas d'arêtes multiples: (a,b) est ajoutée au plus une fois.
 *      * Pas de boucle: on interdit a = b dans addEdge.
 */
public class SimpleGraph {
    // Adjacence: chaque sommet pointe vers un set de voisins (pas de doublons, accès O(1) moyen)
    private final Map<String, Set<String>> adj;
    // Nombre d'arêtes (chaque arête non orientée compte pour 1)
    private int edgeCount;


    /**
     * Nouveau graph 
     */
    public SimpleGraph() {
        this.adj = new HashMap<>();
        this.edgeCount = 0;
    }

    /**
     * Deepcopy du graph g
     */
    public SimpleGraph(SimpleGraph g) {
        this.adj = new HashMap<>();
        g.adj.forEach((k, v) -> this.adj.put(k, new HashSet<>(v)));
        this.edgeCount = g.edgeCount;
    }



    /**
     * Ensemble non modifiable des sommets présents dans le graphe.
     * Utiliser addVertex/addEdge pour le modifier.
     */
    public Set<String> vertices() { return Collections.unmodifiableSet(adj.keySet()); }

    /**
     * Nombre de sommets.
     */
    public int vertexCount() { return adj.size(); }

    /**
     * Nombre d'arêtes (non orientées, uniques).
     */
    public int edgeCount() { return edgeCount; }

    /**
     * Ajoute un sommet s'il n'existe pas déjà.
     */
    public void addVertex(String v) { adj.putIfAbsent(v, new HashSet<>()); }

    /**
     * Teste la présence de l'arête {a,b}.
     */
    public boolean containsEdge(String a, String b) {
        Set<String> s = adj.get(a);
        return s != null && s.contains(b);
    }

    /**
     * Ajoute l'arête non orientée {a,b} si elle n'existe pas.
     * - Crée les sommets au besoin.
     * - Ignore les boucles (a == b).
     * - Maintient la symétrie et incrémente edgeCount une seule fois par arête ajoutée.
     */
    public void addEdge(String a, String b) {
        if (a.equals(b)) return; // pas de boucle
        addVertex(a); addVertex(b);
        if (!adj.get(a).contains(b)) {
            adj.get(a).add(b);
            adj.get(b).add(a);
            edgeCount++;
        }
    }

    /**
     * Supprime l'arête {a,b} si elle existe, en maintenant la symétrie
     * et en décrémentant edgeCount.
     */
    public void removeEdge(String a, String b) {
        if (containsEdge(a,b)) {
            adj.get(a).remove(b);
            adj.get(b).remove(a);
            edgeCount--;
        }
    }

    /**
     * Retourne une vue non modifiable de l'ensemble des voisins de v (ensemble vide si v absent).
     */
    public Set<String> neighbors(String v) { 
        return Collections.unmodifiableSet(adj.getOrDefault(v, Collections.emptySet())); 
    }

    /**
     * Retourne le degree du noeud v
     */
    public int degree(String v) { return neighbors(v).size(); }
    
    /**
     * Liste les arêtes uniques du graphe sous forme d'objets Edge.
     * Pour éviter les doublons, on ne retient que les paires (u,v) avec u < v (ordre lexicographique).
     */
    public List<Edge> edges() {
        List<Edge> list = new ArrayList<>();

        for (String u : adj.keySet()) {
            for (String v : adj.get(u)) {
                if (u.compareTo(v) < 0) {
                    list.add(new Edge(u,v));
                }
            }
        }
        return list;
    }


    /**
     * Calcule le diamètre du graphe (plus longue distance minimale entre deux sommets).
     * Retourne 0 pour un graphe vide ou à un seul sommet.
     */
    public int getVertexDiameter() {
        int diameter = 0;
        for (String source : vertices()) {
            Map<String, Integer> distances = getDistances(source);
            for (int d : distances.values()) {
                if (d > diameter) diameter = d;
            }
        }
        return diameter;
    }

    private static final int VD_SAMPLES = 10;
    /**
     * Approximate the vertex diameter through the mean of repeated samples 
     * Each sample is the length of the concatenation of the two longest shortest paths from a random vertex
     */
    public int getVertexDiameterApproximation(int n_samples) {

        int VD = 0;
        for (int i = 0; i < n_samples; i++) {
            String source = randomNode();
            ArrayList<Integer> distanceValues = new ArrayList<>(getDistances(source).values());
            distanceValues.sort((x1, x2) -> - Integer.compare(x1,x2));
            
            // Sometimes the random node is a leaf, so there is only one possible path
            int diameterSample = distanceValues.size() < 2 ? distanceValues.get(0) : distanceValues.get(0) + distanceValues.get(1);
            VD += diameterSample;
        }
        // Ceil out the integer division (for example 0.555 -> 1)
        return (VD+n_samples) / n_samples;
    }
    public int getVertexDiameterApproximation() { return getVertexDiameterApproximation(VD_SAMPLES); }

    public String randomNode() {
        Random rng = new Random();
        return (String) this.adj.keySet().toArray()[rng.nextInt(vertexCount())];
    }

    /**
     * Calculates shoretest distances in an unweighted graph
     */
    private Map<String, Integer> getDistances(String source) {
        Map<String, Integer> distances = new HashMap<>();
        Queue<String> queue = new ArrayDeque<>();
        distances.put(source, 0);
        queue.add(source);
        while (!queue.isEmpty()) {
            String v = queue.remove();
            for (String w : neighbors(v)) {
                if (!distances.containsKey(w)) {
                    distances.put(w, distances.get(v) + 1);
                    queue.add(w);
                }
            }
        }
        return distances;
    }


    /**
     * Returns a map that assigns a connected component id to each node
     */
    public Map<String, Integer> getConnectedComponents() {

        // Assigns an connected component id to each node, 
        Map<String, Integer> cc_partition = new HashMap<>();

        int component_id = 0; 
        for (String n : vertices()) {
            // Skip nodes already visited
            if (cc_partition.containsKey(n))
                continue;

            // BFS: assign current component_id to all reachable nodes from n
            Queue<String> queue = new ArrayDeque<>();
            queue.add(n);
            cc_partition.put(n, component_id);
            while (!queue.isEmpty()) {
                String u = queue.remove();
                for (String v : neighbors(u)) {
                    if (!cc_partition.containsKey(v)) {
                        cc_partition.put(v, component_id);
                        queue.add(v);
                    }
                }
            }

            // Look for next component
            component_id++;
        }

        return cc_partition;
    }
}
