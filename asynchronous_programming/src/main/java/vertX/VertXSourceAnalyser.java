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
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(Runtime.getRuntime().availableProcessors()));
        fs = vertx.fileSystem();
        listFutures = new LinkedBlockingQueue<>();
    }

    @Override
    public void getReport(final String directory,
                          final int ranges,
                          final int maxL,
                          final int numTopFiles) throws InterruptedException {
        Instant start = Instant.now();
        this.setParameters(directory, ranges, maxL, numTopFiles);
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
    public void analyzeSources(final String directory,
                               final int ranges,
                               final int maxL,
                               final int numTopFiles) throws InterruptedException {

        Instant start = Instant.now();
        view = new AnalyserView(this);
        view.display();
        view.changeState("Running");
        this.setParameters(directory, ranges, maxL, numTopFiles);
        listFutures.add(Future.future(promise -> {
            fileSearch(directory, true);
            promise.complete("Init Job");
        }));

        while(listFutures.size() > 0) {
            listFutures.remove().toCompletionStage().toCompletableFuture().join();
        }

        vertx.close();
        Instant end = Instant.now();
        view.changeState("Completed in " + Duration.between(start, end).toMillis() + " ms");
    }

    @Override
    public void stopExecution() {
        vertx.close();
        view.changeState("Stopped");
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