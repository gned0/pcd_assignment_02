package main;

public interface SourceAnalyser {

    void getReport(final String directory,
                   final int ranges,
                   final int maxL,
                   final int numTopFiles) throws InterruptedException;

    void analyzeSources() throws InterruptedException;


}
