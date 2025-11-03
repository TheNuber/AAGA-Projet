package com.thenuber.aaga;

public class Edge {
    public final Vertex u;
    public final Vertex v;

    public Edge(Vertex u, Vertex v) {
        if (u.compareTo(v) <= 0) { 
            this.u = u; 
            this.v = v; 
        }
        else { 
            this.u = v; 
            this.v = u;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge e = (Edge) o;
        return u.equals(e.u) && v.equals(e.v);
    }

    @Override
    public int hashCode() { return u.hashCode() * 31 + v.hashCode(); }

    @Override
    public String toString() { return u + "-" + v; }
}
