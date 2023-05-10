package vertX;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import main.AbstractSourceAnalyser;
import main.view.AnalyserView;
import vertX.test.VertXDirectorySearch;
import vertX.test.VertXFileRead;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

public class VertXSourceAnalyser extends AbstractSourceAnalyser {
    Vertx vertx;
    Queue<Future> futureList;

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
        futureList.add(vertx.deployVerticle(new VertXDirectorySearchSource(futureList, directory)));
        futureList.add(vertx.deployVerticle(new VertXFileReadSource(futureList,
                view, false, intervals, topFiles, ranges, maxL, numTopFiles)));


        while(futureList.size() > 0) {
            futureList.remove().toCompletionStage().toCompletableFuture().join();
        }
        vertx.close();

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

}