package rxJava;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import main.AbstractSourceAnalyser;
import main.utility.Pair;
import main.view.AnalyserView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RxSourceAnalyser extends AbstractSourceAnalyser {

    private boolean isCancelled = false;

    @Override
    public void getReport(String directory, int ranges, int maxL, int numTopFiles) {
        Instant start = Instant.now();
        this.setParameters(directory, ranges, maxL, numTopFiles);
        File startFile = new File(directory);

        Flowable.fromArray(startFile.listFiles())
                .flatMap(file -> file.isDirectory()
                        ? Flowable.just(file.toPath()).subscribeOn(Schedulers.io())
                        .flatMap(path -> Flowable.fromIterable(listFiles(path)))
                        : Flowable.just(file.toPath()).subscribeOn(Schedulers.io()))
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(path -> Flowable.fromCallable(() -> this.countLines(path.toFile()))
                        .subscribeOn(Schedulers.computation())
                        .map(lines -> new Pair<>(path, lines)))
                .doOnNext(file -> updateIntervals(file.second))
                .sorted(Comparator.comparing(pair -> pair.second, Comparator.reverseOrder()))
                .take(numTopFiles)
                .map(file -> new Pair<>(file.first.toString(), file.second))
                .toList()
                .blockingSubscribe(topFiles -> {
                    printTopFiles(topFiles);
                    printIntervals(intervals);
                }, Throwable::printStackTrace);

        Instant end = Instant.now();
        System.out.println("RxSourceAnalyser, completed in " + Duration.between(start, end).toMillis() + " ms");

    }

    @Override
    public void analyzeSources() {
        view = new AnalyserView(this);
        view.display();
        view.changeState("Running");

        while(true) {
            isCancelled = false;
            waitStart();

            Instant start = Instant.now();
            File startFile = new File(initialDirectory);

            Flowable.fromArray(startFile.listFiles())
                    .flatMap(file -> file.isDirectory()
                            ? Flowable.just(file.toPath()).subscribeOn(Schedulers.io())
                            .flatMap(path -> Flowable.fromIterable(listFiles(path)))
                            : Flowable.just(file.toPath()).subscribeOn(Schedulers.io()))
                    .takeUntil(f -> {
                        synchronized (this) {
                            return isCancelled;
                        }
                    })
                    .filter(path -> path.toString().endsWith(".java"))
                    .flatMap(path -> Flowable.fromCallable(() -> this.countLines(path.toFile()))
                            .subscribeOn(Schedulers.computation())
                            .map(lines -> new Pair<>(path, lines)))
                    .doOnNext(file -> {
                        updateIntervals(file.second);
                        updateTopFiles(file.first.toFile(), file.second);
                        view.update(intervals, topFiles, ranges, maxL);
                    })
                    .blockingSubscribe();
            Instant end = Instant.now();
            view.changeState("RxSourceAnalyser, completed in " + Duration.between(start, end).toMillis() + " ms");

        }
    }

    @Override
    public void stopExecution() {

        synchronized (this) {
            isCancelled = true;
        }
        view.changeState("Stopped");
    }

    @Override
    public void waitStart() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        resetTopFiles();
        resetIntervals();
    }

    @Override
    public void startPressed(final String directory,
                             final int ranges,
                             final int maxL,
                             final int numTopFile) {
        this.initialDirectory = directory;
        this.ranges = ranges;
        this.maxL = maxL;
        this.numTopFiles = numTopFile;
        synchronized (this) {
            notifyAll();
        }
    }


    private List<Path> listFiles(Path path) {
        try {
            return Files.walk(path)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files: " + e.getMessage(), e);
        }
    }


}
