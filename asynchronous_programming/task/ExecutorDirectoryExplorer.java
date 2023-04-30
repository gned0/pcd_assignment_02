package task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ExecutorDirectoryExplorer {

    public static void main(String[] args) throws IOException, InterruptedException {

        String directoryPath = "pcd";
        int topN = 10;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<JavaFileLength> fileLengths = new ArrayList<>();
        searchJavaFiles(directoryPath, fileLengths, executor);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        List<JavaFileLength> longestFiles = fileLengths.stream()
                .sorted(Comparator.comparingInt(JavaFileLength::getLength).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        System.out.printf("Top %d longest .java files in %s:%n", topN, directoryPath);
        longestFiles.forEach(System.out::println);
    }

    private static void searchJavaFiles(String directoryPath, List<JavaFileLength> fileLengths, ExecutorService executor) {
        executor.submit(() -> {
            File directory = new File(directoryPath);
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    System.out.println(file);
                    if (file.isFile() && file.getAbsoluteFile().getAbsolutePath().endsWith("java")) {
                        executor.submit(() -> {
                            try {
                                System.out.println("|| Found file " + file.getAbsolutePath() + " ||");
                                int length = Files.readAllLines(file.toPath()).stream().mapToInt(String::length).sum();
                                fileLengths.add(new JavaFileLength(file.getName(), length));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else if (file.isDirectory()) {
                        System.out.println("|| Found Directory " + file.getAbsolutePath() + " ||");
                        searchJavaFiles(file.getPath(), fileLengths, executor);
                    }
                }
            }
        });
    }


    private static class JavaFileLength {
        private final String fileName;
        private final int length;

        public JavaFileLength(String fileName, int length) {
            this.fileName = fileName;
            this.length = length;
        }

        public String getFileName() {
            return fileName;
        }

        public int getLength() {
            return length;
        }

        @Override
        public String toString() {
            return String.format("%s (%d bytes)", fileName, length);
        }
    }
}

