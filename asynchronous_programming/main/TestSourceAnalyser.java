package main;

import task.TaskSourceAnalyser;

public class TestSourceAnalyser {

    public static void main(String[] args) throws InterruptedException {

        SourceAnalyser test = new TaskSourceAnalyser();
        test.analyzeSources("input", 5, 150, 10);

    }

}
