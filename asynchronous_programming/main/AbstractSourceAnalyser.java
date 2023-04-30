package main;


import main.SourceAnalyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSourceAnalyser implements SourceAnalyser {

    private final List<Integer> intervals;
    private final Map<String, Integer> topFiles;
    private final String initialDirectory;
    private final int ranges;
    private final int maxL;
    private final int numTopFiles;

    public AbstractSourceAnalyser(final String initialDirectory,
                       final int ranges,
                       final int maxL,
                       final int numTopFiles) {
        this.initialDirectory = initialDirectory;
        this.ranges = ranges;
        this.maxL = maxL;
        this.intervals = new ArrayList<>();
        for(int i = 0; i < ranges; i++) {
            this.intervals.add(i, 0);
        }
        this.numTopFiles = numTopFiles;
        this.topFiles = new HashMap<>();
    }

    abstract public void getReport(String directory);

    abstract public void analyzeSources(String directory);
}
