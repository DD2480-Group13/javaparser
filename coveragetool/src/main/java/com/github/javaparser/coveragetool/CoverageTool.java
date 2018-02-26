package com.github.javaparser.coveragetool;

import java.util.HashMap;

public class CoverageTool {
    private static final HashMap<String, Boolean> coverage = new HashMap<>();

    public void makeCovered(String id) {
        coverage.put(id, true);
    }
}
