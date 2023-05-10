package vertX;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import main.utility.Pair;
import main.view.AnalyserView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Queue;

public class VertXFileReadSource extends AbstractVerticle {

    private Queue<Future> listFutures;
    private FileSystem fs;
    private EventBus eb;
    private AnalyserView view;
    private boolean updateGUI;
    private List<Integer> intervals;
    private List<Pair<String, Integer>> topFiles;
    private int ranges;
    private int maxL;
    private int numTopFiles;

    public VertXFileReadSource(final Queue<Future> listFutures,
                               final AnalyserView view,
                               final boolean updateGUI,
                               final List<Integer> intervals,
                               final List<Pair<String, Integer>> topFiles,
                               final int ranges,
                               final int maxL,
                               final int numTopFiles) {
        this.listFutures = listFutures;
        this.view = view;
        this.updateGUI = updateGUI;
        this.intervals = intervals;
        this.topFiles = topFiles;
        this.ranges = ranges;
        this.maxL = maxL;
        this.numTopFiles = numTopFiles;
    }

    @Override
    public void start() throws Exception {
        super.start();
        fs = this.getVertx().fileSystem();
        eb = this.getVertx().eventBus();
        readFile();
    }

    private void readFile() {
        eb.consumer("readFile", data -> {
//            log("Got File " + data.body());
            Future<Buffer> fileReadFuture = fs.readFile((String) data.body());
            listFutures.add(fileReadFuture);
            fileReadFuture.onComplete((AsyncResult<Buffer> res) -> {
//                fileLengths.add(new FileLength((String) data.body(), res.result().length()));
                int numLines = countLines(new File((String) data.body()));
                this.updateIntervals(numLines);
                this.updateTopFiles(new File((String) data.body()), numLines);
                if(updateGUI)
                    view.update(intervals, topFiles, ranges, maxL);
            });
        });

    }

    protected void updateIntervals(final int numLines) {
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

    protected void updateTopFiles(final File file, final int numLines) {
        synchronized (topFiles){
            topFiles.add(new Pair<>(file.getAbsolutePath(), numLines));
            topFiles.sort((pair1, pair2) ->
                    -pair1.second.compareTo(pair2.second)
            );
            if(topFiles.size() > this.numTopFiles){
                topFiles.remove(topFiles.size()-1);
            }
        }
    }

    protected int countLines(File file) {
        int numLines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                numLines = numLines + 1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numLines;
    }

    private static void log(String msg) {
        System.out.println("" + Thread.currentThread() + " " + msg);
    }
}
