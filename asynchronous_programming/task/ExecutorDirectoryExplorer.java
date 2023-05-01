package task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ExecutorDirectoryExplorer {

    public static void main(String[] args) throws IOException, InterruptedException {
        String directoryPath = "input";
        int topN = 10;

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<FileLength> fileLengths = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        searchForFiles(new File(directoryPath), fileLengths, executor, latch);

        if (latch.getCount() > 0 && executor.submit(() -> latch.countDown()).isDone()) {
            latch.await();
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        List<FileLength> longestFiles = fileLengths.stream()
                .sorted(Comparator.comparingLong(FileLength::getLength).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        System.out.printf("Top %d longest files in %s:%n", topN, directoryPath);
        longestFiles.forEach(System.out::println);
    }

    private static void searchForFiles(File directory, List<FileLength> fileLengths, ExecutorService executor, CountDownLatch latch) throws InterruptedException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    executor.execute(() -> {
                        try {
                            long length = Files.size(file.toPath());
                            synchronized (fileLengths) {
                                fileLengths.add(new FileLength(file.getName(), length));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else if (file.isDirectory()) {
                    /*executor.submit(() -> {
                        try {
                            searchForFiles(file, fileLengths, executor, latch);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });*/
                    searchForFiles(file, fileLengths, executor, latch);
                }
            }
        }


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