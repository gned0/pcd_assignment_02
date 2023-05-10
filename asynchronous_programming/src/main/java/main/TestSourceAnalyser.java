package main;

import RxJava.RxSourceAnalyser;
import io.vertx.core.net.impl.pool.Task;
import taskExecutor.TaskSourceAnalyser;
import virtualThread.VirtualSourceAnalyser;

public class TestSourceAnalyser {

    public static void main(String[] args) throws InterruptedException {

        SourceAnalyser test = new RxSourceAnalyser();
        test.analyzeSources("input", 5, 150, 10);

    }

}
