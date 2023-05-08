package taskExecutor;

import main.AbstractSourceAnalyser;
import main.view.AnalyserView;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.*;

public class TaskSourceAnalyser extends AbstractSourceAnalyser {
    ExecutorService executor;
    Queue<Future<?>> futureList;

    public TaskSourceAnalyser() {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public void getReport(final String directory,
                          final int ranges,
                          final int maxL,
                          final int numTopFiles) throws InterruptedException {
        this.setParameters(directory, ranges, maxL, numTopFiles);
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

    }

    @Override
    public void analyzeSources(final String directory,
                               final int ranges,
                               final int maxL,
                               final int numTopFiles) throws InterruptedException {

        view = new AnalyserView(this);
        view.display();
        this.setParameters(directory, ranges, maxL, numTopFiles);
        futureList = new LinkedBlockingQueue<>();
        futureList.add(executor.submit(() -> fileSearch(directory, true)));


        while(futureList.size() > 0) {
            try {
                futureList.remove().get(); // get will block until the future is done
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executor.shutdown();
    }

    @Override
    protected void stopExecution() {
        executor.shutdownNow();
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
