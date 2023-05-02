package main;

import task.TaskSourceAnalyser;

public class TestSourceAnalyser {

    public static void main(String[] args) throws InterruptedException {

        SourceAnalyser test = new TaskSourceAnalyser();

        test.getReport("input", 5, 150, 10);

    }

}
