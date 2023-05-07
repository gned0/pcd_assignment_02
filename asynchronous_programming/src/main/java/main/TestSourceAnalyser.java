package main;

import virtualThread.VirtualSourceAnalyser;

public class TestSourceAnalyser {

    public static void main(String[] args) throws InterruptedException {

        SourceAnalyser test = new VirtualSourceAnalyser();
        test.analyzeSources("input", 5, 150, 10);

    }

}
