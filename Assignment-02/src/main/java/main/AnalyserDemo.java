package main;

import rxJava.RxSourceAnalyser;
import taskExecutor.TaskSourceAnalyser;
import vertX.VertXSourceAnalyser;
import virtualThread.VirtualSourceAnalyser;

import java.util.Scanner;

public class AnalyserDemo {

    private static SourceAnalyser analyser = null;
    private static int selectedAnalyser = -1;
    private static int selectedMethod = -1;

    public static void main(String[] args) throws InterruptedException {



        Scanner scanner = new Scanner(System.in);
        while (selectedAnalyser < 1 || selectedAnalyser > 4) {
            System.out.println("Choose analyser to test (1 to 4): ");
            System.out.println("1. Executor Service");
            System.out.println("2. Virtual Thread");
            System.out.println("3. VertX");
            System.out.println("4. RxJava");
            if (scanner.hasNextInt()) {
                selectedAnalyser = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine(); // Discard invalid input
            }
        }

        switch (selectedAnalyser) {
            case 1 -> analyser = new TaskSourceAnalyser();
            case 2 -> analyser = new VirtualSourceAnalyser();
            case 3 -> analyser = new VertXSourceAnalyser();
            case 4 -> analyser = new RxSourceAnalyser();
        }

        while (selectedMethod < 1 || selectedMethod > 2) {
            System.out.println("Choose method to test (1 to 2): ");
            System.out.println("1. getReport()");
            System.out.println("2. analyzeSources()");
            if (scanner.hasNextInt()) {
                selectedMethod = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine(); // Discard invalid input
            }
        }


        switch (selectedMethod) {
            case 1 -> {
                scanner = new Scanner(System.in);
                String input = null;
                System.out.println("Enter parameters directory path, number of intervals, max lines and number of top files separated by spaces: ");
                if (scanner.hasNextLine()) {
                    input = scanner.nextLine();
                }
                assert input != null;
                String[] params = input.split(" ");
                scanner.close();
                if (params.length == 4) {
                    String directory = params[0];
                    int ranges = Integer.parseInt(params[1]);
                    int maxL = Integer.parseInt(params[2]);
                    int numTopFiles = Integer.parseInt(params[3]);
                    analyser.getReport(directory, ranges, maxL, numTopFiles);

                } else {
                    System.out.println("Invalid input. Please enter four parameters separated by spaces.");
                }
            }
            case 2 -> analyser.analyzeSources();
        }
        scanner.close();

    }

}
