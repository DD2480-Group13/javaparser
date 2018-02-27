package com.github.javaparser.coveragetool;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

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
        String[] keys = new String[coverage.size()];
        keys = coverage.keySet().toArray(keys);
        FileWriter writer = new FileWriter(outFile);
        for(int i=0; i < keys.length; i++) {
            writer.write((i+1) + ": " + keys[i] + "\n");
        }
        writer.close();
    }
}
