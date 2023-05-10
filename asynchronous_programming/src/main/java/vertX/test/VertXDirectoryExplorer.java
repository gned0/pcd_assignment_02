package vertX.test;

;
import io.vertx.core.*;
import vertX.FileLength;

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
        listFutures.add(vertx.deployVerticle(new VertXDirectorySearch(listFutures, directoryPath)));
        listFutures.add(vertx.deployVerticle(new VertXFileRead(fileLengths, listFutures)));

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }

        while(listFutures.size() > 0) {
            listFutures.remove().toCompletionStage().toCompletableFuture().join();
        }


        List<FileLength> longestFiles = fileLengths.stream()
                .sorted(Comparator.comparingLong(FileLength::getLength).reversed())
                .limit(topN)
                .collect(Collectors.toList());


        System.out.printf("Top %d longest files in %s:%n", topN, directoryPath);
        longestFiles.forEach(System.out::println);

//        vertx.close();
    }
}

