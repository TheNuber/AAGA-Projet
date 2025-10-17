package com.thenuber.aaga;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GraphLoader {
    public static SimpleGraph loadEdgeList(String path, String delimiter) throws IOException {
        SimpleGraph g = new SimpleGraph();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(delimiter);
                if (parts.length < 2) continue;
                String a = parts[0].trim();
                String b = parts[1].trim();
                g.addEdge(a, b);
            }
        }
        return g;
    }
}
