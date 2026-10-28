package org.example.generator.classes;

import java.util.Map;

public class MapExample {
    private Map<String, Integer> entries;

    public MapExample(Map<String, Integer> entries) {
        this.entries = entries;
    }

    public Map<String, Integer> getEntries() {
        return entries;
    }
}
