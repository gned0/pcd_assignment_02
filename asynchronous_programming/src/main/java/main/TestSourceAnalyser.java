package main;

import RxJava.RxSourceAnalyser;
import io.vertx.core.net.impl.pool.Task;
import taskExecutor.TaskSourceAnalyser;
import vertX.VertXSourceAnalyser;
import virtualThread.VirtualSourceAnalyser;

public class TestSourceAnalyser {

    public static void main(String[] args) throws InterruptedException {

        SourceAnalyser test = new VertXSourceAnalyser();
//        test.analyzeSources("input", 5, 150, 10);
        test.getReport("input", 5, 150, 10);

    }

}
