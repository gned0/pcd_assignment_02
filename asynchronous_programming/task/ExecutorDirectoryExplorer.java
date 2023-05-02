package task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ExecutorDirectoryExplorer {

    public static void main(String[] args) throws IOException, InterruptedException {
        String directoryPath = "input";
        int topN = 10;

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<FileLength> fileLengths = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        Queue<Future<?>> futureList = new LinkedBlockingQueue<>();

        futureList.add(executor.submit(() -> {
            searchForFiles(new File(directoryPath), fileLengths, executor, futureList);
        }));


        int i = 0;
        while(i < futureList.size()) {
            try {
                futureList.remove().get(); // get will block until the future is done
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

//        if (latch.getCount() > 0 && executor.submit(() -> latch.countDown()).isDone()) {
//            latch.await();
//        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        List<FileLength> longestFiles = fileLengths.stream()
                .sorted(Comparator.comparingLong(FileLength::getLength).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        System.out.printf("Top %d longest files in %s:%n", topN, directoryPath);
        longestFiles.forEach(System.out::println);
    }

    private static void searchForFiles(File directory, List<FileLength> fileLengths, ExecutorService executor, Queue<Future<?>> futureList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    System.out.println("\\\\ File Java found " + file.getAbsolutePath());
                    futureList.add(executor.submit(() -> {
                        try {
                            long length = Files.size(file.toPath());
                            synchronized (fileLengths) {
                                fileLengths.add(new FileLength(file.getName(), length));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }));
                } else if (file.isDirectory()) {
                    System.out.println("// Directory found " + file.getAbsolutePath());
                    futureList.add(executor.submit(() -> {
                        searchForFiles(file, fileLengths, executor, futureList);
                    }));
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