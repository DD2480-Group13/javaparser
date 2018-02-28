package com.github.javaparser.coveragetool;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class CoverageTool {
    private static final HashMap<String, Boolean> coverage = new HashMap<>();
    private static final String outFile = "coverage.out";

    static {
        System.out.println("Setting shutdown hook");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
                try {
                    System.out.println("Running shutdown hook");
                    writeToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void makeCovered(String id) {
        //System.out.println(id);
        coverage.put(id, true);
    }

    public static void writeToFile() throws IOException {
        SortedSet<String> keys = new TreeSet<>(coverage.keySet());
        FileWriter writer = new FileWriter(outFile);
        int i = 0;
        for(String key : keys) {
            writer.write((i+1) + ": " + key + "\n");
            i++;
        }
        writer.close();
    }
}
