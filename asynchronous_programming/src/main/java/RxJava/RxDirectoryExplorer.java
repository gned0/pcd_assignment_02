package RxJava;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RxDirectoryExplorer {

    private static final int TOP_N = 10;
    static int ranges = 5;
    static int maxL = 150;
    static List<Integer> intervals = new ArrayList<>();
    public static void main(String[] args) throws IOException, InterruptedException {
        String directoryPath = "input";
        for(int i = 0; i < ranges; i++) {
            intervals.add(i, 0);
        }
        File directory = new File(directoryPath);

        Map<Path, Long> fileLines = new HashMap<>();


        Flowable<Path> paths = Flowable.fromArray(directory.listFiles())
                .flatMap(file -> file.isDirectory()
                        ? Flowable.just(file.toPath()).subscribeOn(Schedulers.io())
                        .flatMap(path -> Flowable.fromIterable(listFiles(path)))
                        : Flowable.just(file.toPath()).subscribeOn(Schedulers.io()))
                .filter(path -> path.toString().endsWith(".java"))
                .doOnNext(path -> fileLines.put(path, countLines(path)))
                .doOnNext(path -> updateIntervals((int) countLines(path)));

        paths
                .sorted((path1, path2) -> Long.compare(fileLines.get(path2), fileLines.get(path1)))
                .take(TOP_N)
                .toList()
                .blockingSubscribe(pathsList -> {
                    System.out.println("Files with the most lines of code:");
                    pathsList.stream().forEach(p -> System.out.println(p.toString() + " " + fileLines.get(p)));
                    System.out.println();
                }, Throwable::printStackTrace);

        printIntervals(intervals);

    }





    private static List<Path> listFiles(Path path) {
        try {
            return Files.walk(path)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
        }
    }

    private static long countLines(Path path) {
        try {
            return Files.lines(path).count();
        } catch (IOException e) {
            throw new RuntimeException("Failed to count lines: " + e.getMessage(), e);
        }
    }

    public static void updateIntervals(final int numLines) {
        synchronized (intervals) {
            for (int i = ranges - 1; i >= 0; i--) {
                if (numLines >= maxL) {
                    intervals.set(ranges - 1, intervals.get(ranges - 1) + 1);
                    break;
                } else if (numLines >= (maxL / (ranges - 1)) * i && numLines <= ((maxL / (ranges - 1)) * (i + 1)) - 1) {
                    intervals.set(i, intervals.get(i) + 1);
                    break;
                }
            }
        }
    }

    public static void printIntervals(List<Integer> intervals) {
        for (int i = 0; i < ranges; i++) {
            if (i == 0) {
                System.out.println("Range [0, " + String.valueOf((maxL / (ranges - 1)) - 1) + "]: " + intervals.get(i));
            } else if (i == ranges - 1) {
                System.out.println("Range [" + String.valueOf(maxL) + ", Infinity]: " + intervals.get(i));
            } else {
                System.out.println("Range [" + String.valueOf((maxL / (ranges - 1)) * i) + ", " + String.valueOf(((maxL / (ranges - 1)) * (i + 1)) - 1) + "]: " + intervals.get(i));
            }
        }
    }
}
