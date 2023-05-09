package RxJava;

import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.schedulers.Schedulers;
import main.utility.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class RxDirectoryExplorer {

    private static final int TOP_N = 10;
    static int ranges = 5;
    static int maxL = 150;
    static final List<Integer> intervals = new ArrayList<>();
    public static void main(String[] args) throws IOException, InterruptedException {
        String directoryPath = "input";
        for(int i = 0; i < ranges; i++) {
            intervals.add(i, 0);
        }
        File directory = new File(directoryPath);

        /* GIAN LUCA: I don't think one can implement a solution with side effects that can
        also update the GUI. Also this current implementation uses side effects to update the
        intervals data structure. It works fine but as far as I know it is not ideal reactive/
        streams practice.
         */


        Flowable.fromArray(directory.listFiles())
                .flatMap(file -> file.isDirectory()
                        ? Flowable.just(file.toPath()).subscribeOn(Schedulers.io())
                        .flatMap(path -> Flowable.fromIterable(listFiles(path)))
                        : Flowable.just(file.toPath()).subscribeOn(Schedulers.io()))
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(path -> Flowable.fromCallable(() -> countLines(path))
                        .subscribeOn(Schedulers.computation())
                .map(lines -> new Pair<>(path, lines)))
                .doOnNext(file -> updateIntervals(Math.toIntExact(file.second)))
                .sorted(Comparator.comparing(pair -> pair.second, Comparator.reverseOrder()))
                .take(TOP_N)
                .toList()
                .blockingSubscribe(topFiles -> {
                    System.out.println("Files with the most lines of code:");
                    topFiles.forEach(pair -> System.out.println(pair.first.getFileName() + " " +  pair.second.toString()));
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

    static private void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }

}
