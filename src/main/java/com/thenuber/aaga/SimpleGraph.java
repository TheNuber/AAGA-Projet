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
 *
 *  Méthodes principales:
 *   - addVertex(v): crée un sommet (si absent) avec un ensemble de voisins vide.
 *   - addEdge(a,b): ajoute l'arête {a,b} si elle n'existe pas, met à jour l'adjacence symétrique
 *                   et incrémente edgeCount. Ignore les boucles (a == b).
 *   - removeEdge(a,b): supprime l'arête si elle existe et décrémente edgeCount.
 *   - containsEdge(a,b): teste la présence de l'arête {a,b}.
 *   - neighbors(v): renvoie une vue non modifiable de l'ensemble des voisins de v.
 *   - edges(): retourne la liste des arêtes uniques (une seule direction) en parcourant l'adjacence
 *              et en ne gardant que les paires (u,v) avec u < v (ordre lexicographique).
 */
public class SimpleGraph {
    // Adjacence: chaque sommet pointe vers un set de voisins (pas de doublons, accès O(1) moyen)
    private final Map<String, Set<String>> adj = new HashMap<>();
    // Nombre d'arêtes (chaque arête non orientée compte pour 1)
    private int edgeCount = 0;

    /**
     * Calcule le diamètre du graphe (plus longue distance minimale entre deux sommets).
     * Retourne 0 pour un graphe vide ou à un seul sommet.
     */
    public int getVertexDiameter() {
        int diameter = 0;
        for (String source : vertices()) {
            Map<String, Integer> dist = bfsDistances(source);
            for (int d : dist.values()) {
                if (d > diameter) diameter = d;
            }
        }
        return diameter;
    }

    /**
     * BFS depuis un sommet source, retourne les distances vers tous les sommets atteignables.
     */
    private Map<String, Integer> bfsDistances(String source) {
        Map<String, Integer> dist = new HashMap<>();
        Queue<String> queue = new ArrayDeque<>();
        dist.put(source, 0);
        queue.add(source);
        while (!queue.isEmpty()) {
            String v = queue.remove();
            int d = dist.get(v);
            for (String w : neighbors(v)) {
                if (!dist.containsKey(w)) {
                    dist.put(w, d + 1);
                    queue.add(w);
                }
            }
        }
        return dist;
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
    public void addVertex(String v) { adj.computeIfAbsent(v, k -> new HashSet<>()); }

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
    public Set<String> neighbors(String v) { return Collections.unmodifiableSet(adj.getOrDefault(v, Collections.emptySet())); }

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
        for (var e : adj.entrySet()) {
            String u = e.getKey();
            for (String v : e.getValue()) {
                if (u.compareTo(v) < 0) list.add(new Edge(u,v));
            }
        }
        return list;
    }
}
