package main;

import io.vertx.core.net.impl.pool.Task;
import taskExecutor.TaskSourceAnalyser;
import virtualThread.VirtualSourceAnalyser;

public class TestSourceAnalyser {

    public static void main(String[] args) throws InterruptedException {

        SourceAnalyser test = new TaskSourceAnalyser();
        test.analyzeSources("input", 5, 150, 10);

    }

}
