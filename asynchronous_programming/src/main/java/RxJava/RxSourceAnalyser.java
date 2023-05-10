package RxJava;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import main.AbstractSourceAnalyser;
import main.utility.Pair;
import main.view.AnalyserView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RxSourceAnalyser extends AbstractSourceAnalyser {

    @Override
    public void getReport(String directory, int ranges, int maxL, int numTopFiles) {
        this.setParameters(directory, ranges, maxL, numTopFiles);
        File startFile = new File(directory);
        Flowable.fromArray(startFile.listFiles())
                .flatMap(file -> file.isDirectory()
                        ? Flowable.just(file.toPath()).subscribeOn(Schedulers.io())
                        .flatMap(path -> Flowable.fromIterable(listFiles(path)))
                        : Flowable.just(file.toPath()).subscribeOn(Schedulers.io()))
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(path -> Flowable.fromCallable(() -> countLines(path))
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

    }

    @Override
    public void analyzeSources(String directory, int ranges, int maxL, int numTopFiles) {

        view = new AnalyserView(this);
        view.display();
        this.setParameters(directory, ranges, maxL, numTopFiles);
        File startFile = new File(directory);

        Flowable.fromArray(startFile.listFiles())
                .flatMap(file -> file.isDirectory()
                        ? Flowable.just(file.toPath()).subscribeOn(Schedulers.io())
                        .flatMap(path -> Flowable.fromIterable(listFiles(path)))
                        : Flowable.just(file.toPath()).subscribeOn(Schedulers.io()))
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(path -> Flowable.fromCallable(() -> countLines(path))
                        .subscribeOn(Schedulers.computation())
                        .map(lines -> new Pair<>(path, lines)))
                .doOnNext(file -> {
                    updateIntervals(file.second);
                    updateTopFiles(file.first.toFile(), file.second);
                    view.update(intervals, topFiles, ranges, maxL);
                })
                .blockingSubscribe();



    }

    @Override
    protected void stopExecution() {

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

    private int countLines(Path path) {
        try {
            return (int)Files.lines(path).count();
        } catch (IOException e) {
            throw new RuntimeException("Failed to count lines: " + e.getMessage(), e);
        }
    }
}
