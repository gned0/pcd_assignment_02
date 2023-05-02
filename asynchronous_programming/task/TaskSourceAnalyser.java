package task;

import main.AbstractSourceAnalyser;
import main.utility.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TaskSourceAnalyser extends AbstractSourceAnalyser {
    ExecutorService executor;
    CountDownLatch latch;

    public TaskSourceAnalyser() {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        latch = new CountDownLatch(1);
    }

    @Override
    public void getReport(final String directory,
                          final int ranges,
                          final int maxL,
                          final int numTopFiles) throws InterruptedException {
        this.setParameters(directory, ranges, maxL, numTopFiles);
        fileSearch(directory);
        if (latch.getCount() > 0 && executor.submit(() -> latch.countDown()).isDone()) {
            latch.await();
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("Top Files By Line Count: ");
        topFiles.forEach(pair -> {
            System.out.println("First: " + pair.first + " - Second: " + pair.second);
        });

        for (int i = 0; i < ranges; i++) {
            if (i == 0) {
                System.out.println("Range [0," + String.valueOf((maxL / (ranges - 1)) - 1) + "]: " + this.intervals.get(i));
            } else if (i == ranges - 1) {
                System.out.println("Range [" + String.valueOf(maxL) + ",Infinito]: " + this.intervals.get(i));
            } else {
                System.out.println("Range [" + String.valueOf((maxL / (ranges - 1)) * i) + "," + String.valueOf(((maxL / (ranges - 1)) * (i + 1)) - 1) + "]: " + this.intervals.get(i));
            }
        }
    }

    @Override
    public void analyzeSources(final String directory,
                               final int ranges,
                               final int maxL,
                               final int numTopFiles) {

    }

    private void fileSearch(String directory) {
        File[] files = new File(directory).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    executor.execute(() -> {
                        int numLines = this.countLines(file);
                        this.updateIntervals(numLines);
                        this.updateTopFiles(file, numLines);
                    });
                } else if (file.isDirectory()) {
                    /*executor.submit(() -> {
                        try {
                         fileSearch(file.getAbsolutePath());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });*/

                }
            }
        }
    }
}
