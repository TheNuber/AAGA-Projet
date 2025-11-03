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


        // Read input graph

        SimpleGraph g = GraphLoader.loadEdgeList(inputFilePath, delim);


        // Calculate betweenness

        Map<Vertex, Integer> partition = null;
        double modularity = -1.0;

        if (algorithm.equals("gn")) {
            // Girvan-Newmann algorithm
            List<Map<Vertex, Integer>> parts = GirvanNewman.run(g);

            if (!parts.isEmpty()) {
                // Select the partition with the highest modularity (best cut),
                // instead of taking the last partition which is usually trivial
                double bestQ = Double.NEGATIVE_INFINITY;
                Map<Vertex, Integer> bestPartition = null;
                for (Map<Vertex, Integer> p : parts) {
                    double q = Modularity.compute(g, p);
                    if (q > bestQ) {
                        bestQ = q;
                        bestPartition = p;
                    }
                }
                if (bestPartition != null) {
                    partition = bestPartition;
                    modularity = bestQ;
                } else {
                    System.out.println("No partitions produced.");
                }
            } else {
                System.out.println("No partitions produced.");
            }
        } else if (algorithm.equals("bsa")) {
            // Betweenness sampling algorithm
            // TODO

            // Example of running Betweenness Sampling Algorithm
            BetweennessSamplingAlgo bsa = new BetweennessSamplingAlgo();
            bsa.run(g);

        } else {
            System.out.println("Unknown algorithm: " + algorithm);
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
