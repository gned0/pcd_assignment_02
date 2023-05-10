package vertX;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import vertX.test.VertXDirectoryExplorer;

import java.io.File;
import java.util.List;
import java.util.Queue;

public class VertXFileRead extends AbstractVerticle {

    private List<FileLength> fileLengths;
    private Queue<Future> listFutures;
    private FileSystem fs;
    private EventBus eb;

    public VertXFileRead(final List<FileLength> fileLengths, final Queue<Future> listFutures) {
        this.fileLengths = fileLengths;
        this.listFutures = listFutures;
    }

    @Override
    public void start() throws Exception {
        super.start();
        fs = this.getVertx().fileSystem();
        eb = this.getVertx().eventBus();
        readFile(fileLengths);
    }

    private void readFile(final List<FileLength> fileLengths) {
        eb.consumer("readFile", data -> {
//            log("Got File " + data.body());
            Future<Buffer> fileReadFuture = fs.readFile((String) data.body());
            listFutures.add(fileReadFuture);
            fileReadFuture.onComplete((AsyncResult<Buffer> res) -> {
                synchronized (fileLengths) {
                    fileLengths.add(new FileLength((String) data.body(), res.result().length()));
//                System.out.println(new FileLength(filePath, res.result().length()).toString());
                }
            });
        });

    }

    private static void log(String msg) {
        System.out.println("" + Thread.currentThread() + " " + msg);
    }
}
