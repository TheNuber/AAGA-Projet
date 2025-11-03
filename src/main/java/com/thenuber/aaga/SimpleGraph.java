package com.thenuber.aaga;

import java.util.*;

/**
 * SimpleGraph
 *  - Graphe simple, non orienté, sans multi-arêtes ni boucles, avec sommets de type Vertex.
 *  - Représentation par listes d'adjacence: Map<Sommet, Set<Voisins>>.
 *  - Invariants:
 *      * Si b ∈ adj[a], alors a ∈ adj[b] (symétrie). Les arêtes sont non orientées.
 *      * edgeCount compte le nombre d'arêtes uniques (chaque arête {a,b} vaut 1).
 *      * Pas d'arêtes multiples: (a,b) est ajoutée au plus une fois.
 *      * Pas de boucle: on interdit a = b dans addEdge.
 */
public class SimpleGraph {
    // Adjacence: chaque sommet pointe vers un set de voisins (pas de doublons, accès O(1) moyen)
    private Map<Vertex, Set<Vertex>> adj = new HashMap<>();

    // Noeuds
    private ArrayList<Vertex> vertices = new ArrayList<>();
    private Map<String, Vertex> nameToVertex = new HashMap<>();

    // Nombre d'arêtes (chaque arête non orientée compte pour 1)
    private int edgeCount = 0;

    /**
     * Nouveau graph 
     */
    public SimpleGraph() { }

    /**
     * Deepcopy du graph g
     */
    public SimpleGraph(SimpleGraph g) {
        this.vertices = new ArrayList<>(g.vertices);
        this.nameToVertex = new HashMap<>(g.nameToVertex);
        g.adj.forEach((k, v) -> this.adj.put(k, new HashSet<>(v)));
        this.edgeCount = g.edgeCount;
    }

    /**
     * Ensemble non modifiable des sommets présents dans le graphe.
     * Utiliser addVertex/addEdge pour le modifier.
     */
    public Collection<Vertex> vertices() { return Collections.unmodifiableCollection(vertices); }

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
    public Vertex addVertex(String name) { 
        if (this.nameToVertex.containsKey(name)) return this.nameToVertex.get(name);
        
        Vertex v = new Vertex(this.vertices.size(), name);
        this.vertices.add(v);
        this.nameToVertex.put(name, v);
        adj.put(v, new HashSet<>());

        return v;
    }

    /**
     * Teste la présence de l'arête {a,b}.
     */
    public boolean containsEdge(Vertex a, Vertex b) {
        Set<Vertex> s = adj.get(a);
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
        addEdge(addVertex(a), addVertex(b));
    }

    public void addEdge(Vertex v, Vertex w) {
        if (!containsEdge(v, w)) {
            adj.get(v).add(w);
            adj.get(w).add(v);
            edgeCount++;
        }
    }

    /**
     * Supprime l'arête {a,b} si elle existe, en maintenant la symétrie
     * et en décrémentant edgeCount.
     */
    public void removeEdge(Vertex v, Vertex w) {
        if (containsEdge(v,w)) {
            adj.get(v).remove(w);
            adj.get(w).remove(v);
            edgeCount--;
        }
    }

    /**
     * Retourne une vue non modifiable de l'ensemble des voisins de v (ensemble vide si v absent).
     */
    public Collection<Vertex> neighbors(Vertex v) { 
        return Collections.unmodifiableCollection(this.adj.get(v)); 
    }

    /**
     * Retourne le degree du noeud v
     */
    public int degree(Vertex v) { return neighbors(v).size(); }
    
    /**
     * Liste les arêtes uniques du graphe sous forme d'objets Edge.
     * Pour éviter les doublons, on ne retient que les paires (u,v) avec u < v (ordre lexicographique).
     */
    public List<Edge> edges() {
        List<Edge> list = new ArrayList<>();

        for (Vertex u : vertices()) {
            for (Vertex v : neighbors(u)) {
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
        for (Vertex source : vertices()) {
            Map<Vertex, Integer> distances = getDistances(source);
            for (int d : distances.values()) {
                if (d > diameter) diameter = d;
            }
        }
        return diameter;
    }


    public Vertex randomNode() {
        int n = vertexCount();
        if (n == 0) {
            throw new IllegalStateException("Cannot pick a random node from an empty graph");
        }
        Random rng = new Random();
        return vertices.get(rng.nextInt(n));
    }

    /**
     * Calculates shoretest distances in an unweighted graph
     */
    public Map<Vertex, Integer> getDistances(Vertex source) {
        Map<Vertex, Integer> distances = new HashMap<>();
        Queue<Vertex> queue = new ArrayDeque<>();
        distances.put(source, 0);
        queue.add(source);
        while (!queue.isEmpty()) {
            Vertex v = queue.remove();
            for (Vertex w : neighbors(v)) {
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
    public Map<Vertex, Integer> getConnectedComponents() {

        // Assigns an connected component id to each node, 
        Map<Vertex, Integer> cc_partition = new HashMap<>();

        int component_id = 0; 
        for (Vertex u : vertices()) {
            // Skip nodes already visited
            if (cc_partition.containsKey(u))
                continue;

            // BFS: assign current component_id to all reachable nodes from n
            Queue<Vertex> queue = new ArrayDeque<>();
            queue.add(u);
            cc_partition.put(u, component_id);
            while (!queue.isEmpty()) {
                Vertex v = queue.remove();
                for (Vertex w : neighbors(v)) {
                    if (!cc_partition.containsKey(w)) {
                        cc_partition.put(w, component_id);
                        queue.add(w);
                    }
                }
            }

            // Look for next component
            component_id++;
        }

        return cc_partition;
    }


    @Override
    public String toString() {
        String to_String = "Graph: ";
        for (Vertex v : vertices()) {
            to_String = to_String + "\n" + v;
        }
        return to_String;
    }
}
