package vertX;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class VertXDirectoryExplorer {

    public static void main(String[] args) {
        String directoryPath = "input";
        int topN = 10;

        Vertx vertx = Vertx.vertx();
        FileSystem fs = vertx.fileSystem();
        List<FileLength> fileLengths = new ArrayList<>();

        directorySearch(fs, directoryPath, fileLengths);





        List<FileLength> longestFiles = fileLengths.stream()
                .sorted(Comparator.comparingLong(FileLength::getLength).reversed())
                .limit(topN)
                .collect(Collectors.toList());

        System.out.printf("Top %d longest files in %s:%n", topN, directoryPath);
        longestFiles.forEach(System.out::println);
    }

    private static void directorySearch(final FileSystem fs, final String initDirectory, final List<FileLength> fileLengths) {
        Future<List<String>> directoryFuture = fs.readDir(initDirectory);
        directoryFuture.onComplete((AsyncResult<List<String>> res) -> {
            res.result().forEach(filePath -> {
                File tmpFile = new File(filePath);
                if (tmpFile.isDirectory()) {
                    directorySearch(fs, filePath, fileLengths);
                } else if (tmpFile.getAbsolutePath().endsWith(".java")) {
                    readFile(fs, filePath, fileLengths);
                }
            });
        });
    }

    private static void readFile(final FileSystem fs, final String filePath, final List<FileLength> fileLengths) {
        Future<Buffer> fileRead = fs.readFile(filePath);
        fileRead.onComplete((AsyncResult<Buffer> res) -> {
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