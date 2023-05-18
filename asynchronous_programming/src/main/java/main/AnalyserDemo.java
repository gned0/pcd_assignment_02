package main;

import RxJava.RxSourceAnalyser;
import taskExecutor.TaskSourceAnalyser;
import vertX.VertXSourceAnalyser;
import virtualThread.VirtualSourceAnalyser;

import java.util.Scanner;

public class AnalyserDemo {

    private static String directory;
    private static int ranges;
    private static int maxL;
    private static int numTopFiles;
    private static SourceAnalyser analyser = null;
    private static int selectedAnalyser = -1;
    private static int selectedMethod = -1;

    public static void main(String[] args) throws InterruptedException {

        if(args.length != 4) {
            throw new IllegalArgumentException("Illegal argument number");
        }
        directory = args[0];
        ranges = Integer.parseInt(args[1]);
        maxL = Integer.parseInt(args[2]);
        numTopFiles = Integer.parseInt(args[3]);

        Scanner scanner = new Scanner(System.in);
        while (selectedAnalyser < 1 || selectedAnalyser > 4) {
            System.out.println("Choose analyser to test (1 to 4): ");
            System.out.println("1. Executor Service");
            System.out.println("2. Virtual Thread");
            System.out.println("3. VertX");
            System.out.println("4. RxJava");
            if (scanner.hasNextInt()) {
                selectedAnalyser = scanner.nextInt();
            } else {
                scanner.nextLine(); // Discard invalid input
            }
        }
        scanner.close();

        switch (selectedAnalyser) {
            case 1:
                analyser = new TaskSourceAnalyser();
                break;
            case 2:
                analyser = new VirtualSourceAnalyser();
                break;
            case 3:
                analyser = new VertXSourceAnalyser();
                break;
            case 4:
                analyser = new RxSourceAnalyser();
                break;
        }

        scanner = new Scanner(System.in);
        while (selectedMethod < 1 || selectedMethod > 2) {
            System.out.println("Choose method to test (1 to 2): ");
            System.out.println("1. getReport()");
            System.out.println("2. analyzeSources()");
            if (scanner.hasNextInt()) {
                selectedMethod = scanner.nextInt();
            } else {
                scanner.nextLine(); // Discard invalid input
            }
        }
        scanner.close();

        switch (selectedAnalyser) {
            case 1:
                analyser.getReport(directory, ranges, maxL, numTopFiles);
                break;
            case 2:
                analyser.analyzeSources(directory, ranges, maxL, numTopFiles);
                break;
        }


    }

}
