package com.thenuber.aaga;

import java.io.FileWriter;
import java.util.*;

public class Main {
    private static void printHelp() {
        System.out.println("Usage: java -jar aaga-projet.jar -i <input> [-d <delimiterRegex>] [-o <outPrefix>] [-a <alg>]");
        System.out.println("  -i  input edge list file (two columns per line)");
        System.out.println("  -d  delimiter regex (default: \\t)");
        System.out.println("  -o  output prefix (default: out)");
        System.out.println("  -a  algorithm: gn (default: gn)");
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> opts = parseArgs(args);
        if (!opts.containsKey("i")) { printHelp(); return; }
        String input = opts.get("i");
        String delim = opts.getOrDefault("d", "\\t");
        String out = opts.getOrDefault("o", "out");
        String alg = opts.getOrDefault("a", "gn");

        SimpleGraph g = GraphLoader.loadEdgeList(input, delim);
        if ("gn".equals(alg)) {
            List<Map<String, Integer>> parts = GirvanNewman.run(g);
            if (!parts.isEmpty()) {
                Map<String, Integer> last = parts.get(parts.size() - 1);
                double q = Modularity.compute(g, last);
                try (FileWriter fw = new FileWriter(out + "_partition.txt")) {
                    for (Map.Entry<String, Integer> e : last.entrySet()) fw.write(e.getKey() + "\t" + e.getValue() + "\n");
                }
                try (FileWriter fw = new FileWriter(out + "_metrics.txt")) {
                    fw.write("modularity\t" + q + "\n");
                }
                System.out.println("Wrote partition and metrics to " + out + "_* files");
            } else {
                System.out.println("No partitions produced.");
            }
        } else {
            System.out.println("Unknown algorithm: " + alg);
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("-i") && i + 1 < args.length) m.put("i", args[++i]);
            else if (a.equals("-d") && i + 1 < args.length) m.put("d", args[++i]);
            else if (a.equals("-o") && i + 1 < args.length) m.put("o", args[++i]);
            else if (a.equals("-a") && i + 1 < args.length) m.put("a", args[++i]);
        }
        return m;
    }
}
