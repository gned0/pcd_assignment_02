package vertX;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;


public class VertXDirectoryExplorer {

    public static void main(String[] args) {
        String directoryPath = "input";
        int topN = 10;

        Vertx vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(Runtime.getRuntime().availableProcessors()));
        List<FileLength> fileLengths = new ArrayList<>();
        Queue<Future> listFutures = new LinkedBlockingQueue<>();

        listFutures.add(Future.future(promise -> {
            directorySearch(vertx, directoryPath, listFutures, fileLengths);
            promise.complete();
        }));

        while(listFutures.size() > 0) {
            listFutures.remove().toCompletionStage().toCompletableFuture().join();
        }

        List<FileLength> longestFiles = fileLengths.stream()
                .sorted(Comparator.comparingLong(FileLength::getLength).reversed())
                .limit(topN)
                .collect(Collectors.toList());


        System.out.printf("Top %d longest files in %s:%n", topN, directoryPath);
        longestFiles.forEach(System.out::println);

        vertx.close();
    }

    private static void directorySearch(final Vertx vertx,
                                        final String directoryPath,
                                        final Queue<Future> listFutures,
                                        final List<FileLength> fileLengths) {
        FileSystem fs = vertx.fileSystem();
        Future<List<String>> directoryFuture = fs.readDir(directoryPath);
        listFutures.add(directoryFuture);
        directoryFuture.onComplete((AsyncResult<List<String>> res) -> res.result().forEach(filePath -> {
            File tmpFile = new File(filePath);
            if (tmpFile.isDirectory()) {
//                    System.out.println("Found Directory " + filePath);
                listFutures.add(Future.future(promise -> {
                    directorySearch(vertx, filePath, listFutures, fileLengths);
                    promise.complete();
                }));
            } else if (tmpFile.getAbsolutePath().endsWith(".java")) {
                listFutures.add(Future.future(promise -> {
                    readFile(vertx, filePath, listFutures, fileLengths);
                    promise.complete();
                }));
            }
        }));
    }

    private static void readFile(final Vertx vertx,
                          final String filePath,
                          final Queue<Future> listFutures,
                          final List<FileLength> fileLengths) {
        FileSystem fs = vertx.fileSystem();
        Future<Buffer> fileReadFuture = fs.readFile(filePath);
        listFutures.add(fileReadFuture);
        fileReadFuture.onComplete((AsyncResult<Buffer> res) -> {
            synchronized (fileLengths) {
                fileLengths.add(new FileLength(filePath, res.result().length()));
            }
        });

    }

    private static class FileLength {
        private final String fileName;
        private final long length;

        public FileLength(String fileName, long length) {
            this.fileName = fileName;
            this.length = length;
        }

        public String getFileName() {
            return fileName;
        }

        public long getLength() {
            return length;
        }

        @Override
        public String toString() {
            return String.format("%s (%d bytes)", fileName, length);
        }
    }

}

