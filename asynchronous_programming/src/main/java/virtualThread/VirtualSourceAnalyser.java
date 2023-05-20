package virtualThread;

import main.AbstractSourceAnalyser;
import main.view.AnalyserView;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.*;

public class VirtualSourceAnalyser extends AbstractSourceAnalyser {
    ExecutorService executor;
    Queue<Future<?>> futureList;

    public VirtualSourceAnalyser() {
    }

    @Override
    public void getReport(final String directory,
                          final int ranges,
                          final int maxL,
                          final int numTopFiles) throws InterruptedException {
        Instant start = Instant.now();
        this.setParameters(directory, ranges, maxL, numTopFiles);

        executor = Executors.newVirtualThreadPerTaskExecutor();

        futureList = new LinkedBlockingQueue<>();
        futureList.add(executor.submit(() -> {
            fileSearch(directory, false);
        }));


        while(futureList.size() > 0) {
            try {
                futureList.remove().get(); // get will block until the future is done
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();

        this.printTopFiles(topFiles);
        this.printIntervals(intervals);
        Instant end = Instant.now();
        System.out.println("Completed in " + Duration.between(start, end).toMillis() + " ms");
    }

    @Override
    public void analyzeSources() throws InterruptedException {
        view = new AnalyserView(this);
        view.display();

        while(true) {
            executor = Executors.newVirtualThreadPerTaskExecutor();

            waitStart();

            Instant start = Instant.now();
            view.changeState("Running");
            futureList = new LinkedBlockingQueue<>();
            futureList.add(executor.submit(() -> fileSearch(initialDirectory, true)));


            while (futureList.size() > 0) {
                try {
                    futureList.remove().get(); // get will block until the future is done
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
            executor.shutdown();
            Instant end = Instant.now();
            view.changeState("Completed in: " + Duration.between(start, end).toMillis() + "ms");
        }
    }

    @Override
    public void stopExecution() {
        executor.shutdownNow();
        view.changeState("Stopped");
    }

    @Override
    public void waitStart() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        resetTopFiles();
        resetIntervals();
    }

    @Override
    public void startPressed(final String directory,
                             final int ranges,
                             final int maxL,
                             final int numTopFile) {
        this.initialDirectory = directory;
        this.ranges = ranges;
        this.maxL = maxL;
        this.numTopFiles = numTopFile;
        synchronized (this) {
            notifyAll();
        }
    }

    private void fileSearch(String directory, boolean updateGUI) {
        File[] files = new File(directory).listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    futureList.add(executor.submit(() -> {
                        int numLines = this.countLines(file);
                        this.updateIntervals(numLines);
                        this.updateTopFiles(file, numLines);
                        if(updateGUI) {
                            futureList.add(executor.submit(() -> view.update(intervals, topFiles, ranges, maxL)));
                        }

                    }));
                } else if (file.isDirectory()) {
                    futureList.add(executor.submit(() -> {
                        this.fileSearch(file.getAbsolutePath(), updateGUI);
                    }));
                }
            }
        }
    }
}
