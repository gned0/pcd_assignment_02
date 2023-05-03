package main;

import main.utility.Pair;
import main.view.AnalyserView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSourceAnalyser implements SourceAnalyser {

    protected final List<Integer> intervals;
    protected final List<Pair<String, Integer>> topFiles;
    protected String initialDirectory;
    private int ranges;
    private int maxL;
    private int numTopFiles;
    private AnalyserView view;

    public AbstractSourceAnalyser() {
        this.intervals = new ArrayList<>();
        this.topFiles = new ArrayList<>();
    }

    abstract public void getReport(final String directory,
                                   final int ranges,
                                   final int maxL,
                                   final int numTopFiles) throws InterruptedException;

    abstract public void analyzeSources(final String directory,
                                        final int ranges,
                                        final int maxL,
                                        final int numTopFile);

    private void startGUI(int width, int height, InputListener listener){
        this.view = new AnalyserView(width, height);
        this.view.addListener(listener);
        this.view.display();
    }

    protected void setParameters(final String directory,
                                 final int ranges,
                                 final int maxL,
                                 final int numTopFile) {
        this.initialDirectory = directory;
        this.ranges = ranges;
        this.maxL = maxL;
        this.numTopFiles = numTopFile;
        for(int i = 0; i < ranges; i++) {
            this.intervals.add(i, 0);
        }
    }

    protected int countLines(File file) {
        int numLines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                numLines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numLines;
    }

    protected void updateTopFiles(final File file, final int numLines) {
        synchronized (topFiles){
            topFiles.add(new Pair<>(file.getAbsolutePath(), numLines));
            topFiles.sort((pair1, pair2) ->
                    -pair1.second.compareTo(pair2.second)
            );
            if(topFiles.size() > this.numTopFiles){
                topFiles.remove(topFiles.size()-1);
            }
        }
    }

    protected void updateIntervals(final int numLines) {
        synchronized (intervals) {
            for (int i = ranges - 1; i >= 0; i--) {
                if (numLines >= maxL) {
                    intervals.set(ranges - 1, intervals.get(ranges - 1) + 1);
                    break;
                } else if (numLines >= (maxL / (ranges - 1)) * i && numLines <= ((maxL / (ranges - 1)) * (i + 1)) - 1) {
                    intervals.set(i, intervals.get(i) + 1);
                    break;
                }
            }
        }
    }

    protected void printTopFiles(List<Pair<String, Integer>> topFiles) {
        System.out.println("Top Files By Line Count: ");
        topFiles.forEach(pair -> {
            System.out.println("File Path: " + pair.first + ", lines: " + pair.second);
        });
    }

    protected void printIntervals(List<Integer> intervals) {
        for (int i = 0; i < ranges; i++) {
            if (i == 0) {
                System.out.println("Range [0, " + String.valueOf((maxL / (ranges - 1)) - 1) + "]: " + intervals.get(i));
            } else if (i == ranges - 1) {
                System.out.println("Range [" + String.valueOf(maxL) + ", Infinity]: " + intervals.get(i));
            } else {
                System.out.println("Range [" + String.valueOf((maxL / (ranges - 1)) * i) + ", " + String.valueOf(((maxL / (ranges - 1)) * (i + 1)) - 1) + "]: " + intervals.get(i));
            }
        }
    }

}
