package main;

import rxJava.RxSourceAnalyser;

public class TestSourceAnalyser {

    public static void main(String[] args) throws InterruptedException {

        SourceAnalyser test = new RxSourceAnalyser();
        test.analyzeSources();
        // test.getReport("input", 5, 150, 10);

    }

}
