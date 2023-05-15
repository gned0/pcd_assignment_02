package vertX;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystem;
import main.AbstractSourceAnalyser;
import main.view.AnalyserView;
import vertX.test.VertXDirectorySearch;
import vertX.test.VertXFileRead;

import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

public class VertXSourceAnalyser extends AbstractSourceAnalyser {
    Vertx vertx;
    FileSystem fs;
    Queue<Future> listFutures;

    public VertXSourceAnalyser() {
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(Runtime.getRuntime().availableProcessors()));
        futureList = new LinkedBlockingQueue<>();
    }

    @Override
    public void getReport(final String directory,
                          final int ranges,
                          final int maxL,
                          final int numTopFiles) throws InterruptedException {
        this.setParameters(directory, ranges, maxL, numTopFiles);

        vertx = Vertx.vertx();
        fs = vertx.fileSystem();

        listFutures.add(Future.future(promise -> {
            fileSearch(directoryPath, listFutures, fileLengths);
            promise.complete();
        }));

        while(listFutures.size() > 0) {
            listFutures.remove().toCompletionStage().toCompletableFuture().join();
        }

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
        futureList.add(vertx.deployVerticle(new VertXDirectorySearchSource(futureList, directory)));
        futureList.add(vertx.deployVerticle(new VertXFileReadSource(futureList,
                view, true, intervals, topFiles, ranges, maxL, numTopFiles)));


        while(futureList.size() > 0) {
            futureList.remove().toCompletionStage().toCompletableFuture().join();
        }

        vertx.close();
        view.changeState("Done");
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
                            promiseGUI.complete();
                        }));
                    }

                }));
            } else if (file.isDirectory()) {
                listFutures.add(Future.future(promise -> {
                    this.fileSearch(file.getAbsolutePath(), updateGUI);
                    promise.complete();
                }));
            }
        }));

    }


}