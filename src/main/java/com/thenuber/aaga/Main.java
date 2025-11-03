package com.thenuber.aaga;

import java.io.FileWriter;
import java.util.*;

public class Main {
    private static void printHelp() {
        System.out.println(
                "Usage: java -jar aaga-projet.jar -i <input> [-d <delimiterRegex>] [-o <outPrefix>] [-a <alg>]");
        System.out.println("  -i  input edge list file (two columns per line)");
        System.out.println("  -d  delimiter regex (default: \\t)");
        System.out.println("  -o  output prefix (default: out)");
        System.out.println("  -a  algorithm: gn (default: gn)");
    }

    public static void main(String[] args) throws Exception {

        // Parse args

        Map<String, String> opts = parseArgs(args);
        if (!opts.containsKey("i")) {
            printHelp();
            return;
        }

        String inputFilePath = opts.get("i");
        String delim = opts.getOrDefault("d", "\\t");
        String outputFilePath = opts.getOrDefault("o", "out");
        String algorithm = opts.getOrDefault("a", "gn");

        GraphAlgorithm graphAlgorithm = null;

        if (algorithm.equals("gn")) {
            graphAlgorithm = new GirvanNewman();
        } else if (algorithm.equals("bsa")) {
            graphAlgorithm = new BetweennessSamplingAlgo();
        } else {
            System.out.println("Unknown algorithm: " + algorithm);
            return;
        }

        // Read input graph

        SimpleGraph g = GraphLoader.loadEdgeList(inputFilePath, delim);


        // Calculate betweenness

        Map<Vertex, Integer> partition = null;
        double modularity = -1.0;

        List<Map<Vertex, Integer>> parts = graphAlgorithm.run(g);
        if (!parts.isEmpty()) {
            partition = parts.get(parts.size() - 1);
            modularity = Modularity.compute(g, partition);
        }

        // Write results

        // Check that output directory exists

        if (!checkDirectory(outputFilePath)) {
            System.err.println("Warning: could not create directory for: " + outputFilePath);
        }

        
        // Create output files

        String partitionFilePath = outputFilePath + "_partition.txt";
        String metricsFilePath = outputFilePath + "_metrics.txt";

        java.io.File partitionFile = new java.io.File(partitionFilePath);
        java.io.File metricsFile = new java.io.File(metricsFilePath);


        // Write output

        try (FileWriter fw = new FileWriter(partitionFile)) {
            for (Map.Entry<Vertex, Integer> e : partition.entrySet())
                fw.write(e.getKey() + "\t" + e.getValue() + "\n");
        }
        try (FileWriter fw = new FileWriter(metricsFile)) {
            fw.write("modularity\t" + modularity + "\n");
        }

        System.out.println("Wrote partition and metrics to " + outputFilePath + "_* files");
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("-i") && i + 1 < args.length)
                m.put("i", args[++i]);
            else if (a.equals("-d") && i + 1 < args.length)
                m.put("d", args[++i]);
            else if (a.equals("-o") && i + 1 < args.length)
                m.put("o", args[++i]);
            else if (a.equals("-a") && i + 1 < args.length)
                m.put("a", args[++i]);
        }
        return m;
    }

    private static boolean checkDirectory(String filepath) {
        java.io.File outputDirectory = new java.io.File(filepath).getParentFile();
        boolean ok = outputDirectory != null && outputDirectory.exists();
        if (!ok) {
            ok = outputDirectory.mkdirs();
        }
        return ok && outputDirectory != null && outputDirectory.exists();
    }
}
