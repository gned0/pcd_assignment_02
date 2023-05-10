package vertX;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.util.List;
import java.util.Queue;

public class VertXDirectorySearchSource extends AbstractVerticle {

    private String directoryPath;
    private Queue<Future> listFutures;
    private FileSystem fs;
    private EventBus eb;

    public VertXDirectorySearchSource(final Queue<Future> listFutures, final String directoryPath) {
        this.listFutures = listFutures;
        this.directoryPath = directoryPath;
    }

    @Override
    public void start() throws Exception {
        super.start();
//        log("DirectorySearch Start " + vertx.sharedData().getLocalCounter("workers").result().incrementAndGet().result().toString());
        fs = this.getVertx().fileSystem();
        eb = this.getVertx().eventBus();
        Future<List<String>> directoryFuture = fs.readDir(directoryPath);
        listFutures.add(directoryFuture);
        directoryFuture.onComplete((AsyncResult<List<String>> res) -> directorySearch(res.result()));
//        log("DirectorySearch Stop " + vertx.sharedData().getLocalCounter("workers").result().decrementAndGet().result().toString());
    }

    private void directorySearch(final List<String> listDirectories) {

        listDirectories.forEach(filePath -> {
            File tmpFile = new File(filePath);
            if (tmpFile.isDirectory()) {
                Future<List<String>> directoryFutureTmp = fs.readDir(filePath);
                listFutures.add(fs.readDir(filePath));
                directoryFutureTmp.onComplete((AsyncResult<List<String>> res) -> directorySearch(res.result()));
            } else if (tmpFile.getAbsolutePath().endsWith(".java")) {
                eb.publish("readFile", filePath);
//                log("Sent Java File " + filePath);
            }
        });
    }

    private static void log(String msg) {
        System.out.println("" + Thread.currentThread() + " " + msg);
    }
}
