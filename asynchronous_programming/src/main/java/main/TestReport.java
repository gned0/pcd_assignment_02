package main;

import RxJava.RxSourceAnalyser;
import taskExecutor.TaskSourceAnalyser;
import vertX.VertXSourceAnalyser;
import virtualThread.VirtualSourceAnalyser;

import java.util.Scanner;

public class TestReport {

    private static String directory;
    private static int ranges;
    private static int maxL;
    private static int numTopFiles;
    private static SourceAnalyser analyser = null;
    private static int selectedOption = -1;

    public static void main(String[] args) throws InterruptedException {

        if(args.length != 4) {
            throw new IllegalArgumentException("Illegal argument number");
        }
        directory = args[0];
        ranges = Integer.parseInt(args[1]);
        maxL = Integer.parseInt(args[2]);
        numTopFiles = Integer.parseInt(args[3]);

        Scanner scanner = new Scanner(System.in);
        while (selectedOption < 1 || selectedOption > 4) {
            System.out.println("Choose analyser to test (1 to 4): ");
            System.out.println("1. Executor Service");
            System.out.println("2. Virtual Thread");
            System.out.println("3. VertX");
            System.out.println("4. RxJava");
            if (scanner.hasNextInt()) {
                selectedOption = scanner.nextInt();
            } else {
                scanner.nextLine(); // Discard invalid input
            }
        }
        scanner.close();

        switch (selectedOption) {
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

        analyser.getReport(directory, ranges, maxL, numTopFiles);

    }

}
