package vertX;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystem;
import main.AbstractSourceAnalyser;
import main.view.AnalyserView;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class VertXSourceAnalyser extends AbstractSourceAnalyser {
    Vertx vertx;
    FileSystem fs;
    Queue<Future> listFutures;

    public VertXSourceAnalyser() {
        listFutures = new LinkedBlockingQueue<>();
    }

    @Override
    public void getReport(final String directory,
                          final int ranges,
                          final int maxL,
                          final int numTopFiles) {
        Instant start = Instant.now();
        this.setParameters(directory, ranges, maxL, numTopFiles);

        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(Runtime.getRuntime().availableProcessors()));
        fs = vertx.fileSystem();

        listFutures.add(Future.future(promise -> {
            fileSearch(directory, false);
            promise.complete();
        }));

        while(listFutures.size() > 0) {
            listFutures.remove().toCompletionStage().toCompletableFuture().join();
        }

        this.printTopFiles(topFiles);
        this.printIntervals(intervals);
        vertx.close();
        Instant end = Instant.now();
        System.out.println("Completed in " + Duration.between(start, end).toMillis() + " ms");    }

    @Override
    public void analyzeSources() {
        view = new AnalyserView(this);
        view.display();



        while(true) {
            vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(Runtime.getRuntime().availableProcessors()));
            fs = vertx.fileSystem();

            waitStart();

            Instant start = Instant.now();
            view.changeState("Running");
            listFutures.add(Future.future(promise -> {
                fileSearch(initialDirectory, true);
                promise.complete("Init Job");
            }));

            while(listFutures.size() > 0) {
                listFutures.remove().toCompletionStage().toCompletableFuture().join();
            }

            vertx.close();
            Instant end = Instant.now();
            view.changeState("Completed in " + Duration.between(start, end).toMillis() + " ms");
        }

    }

    @Override
    public void stopExecution() {
        vertx.close();
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
        Future<List<String>> directoryFuture = fs.readDir(directory);
        listFutures.add(directoryFuture);
        directoryFuture.onComplete((AsyncResult<List<String>> res) -> res.result().forEach(filePath -> {
            File file = new File(filePath);
            if (file.isFile() && file.getName().endsWith(".java")) {
                listFutures.add(Future.future(promise -> {
                    int numLines = this.countLines(file);
                    this.updateIntervals(numLines);
                    this.updateTopFiles(file, numLines);
                    if(updateGUI) {
                        listFutures.add(Future.future(promiseGUI -> {
                            view.update(intervals, topFiles, ranges, maxL);
                            promiseGUI.complete("GUI Updated");
                        }));
                    }
                    promise.complete("File Read Job");
                }));
            } else if (file.isDirectory()) {
                listFutures.add(Future.future(promise -> {
                    this.fileSearch(file.getAbsolutePath(), updateGUI);
                    promise.complete("Recursive Directory Search");
                }));
            }
        }));

    }


}