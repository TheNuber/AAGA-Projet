package com.thenuber.aaga;

public class Vertex implements Comparable<Vertex> {
	
	private final int id;
	private final String name;

	public Vertex(int id, String name) {
		this.id = id;
		this.name = name;
	}

    public int id() { return id; }
    public String name() { return name; }

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false; 
        Vertex node = (Vertex) o;
        return id != node.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
    return "Vertex {" +
            "id=" + id +
            ", name=" + name +
            '}';
    }

    @Override
    public int compareTo(Vertex other) {
        return ((Integer)this.id).compareTo(other.id);
    }

}
