package main.utility;

public class Latch {
    private final int nWorkers;
    private int nCompletionsNotified;

    public Latch(final int nWorkers){
        this.nWorkers = nWorkers;
        this.nCompletionsNotified = nWorkers;
        System.out.println("WORKERS ARE " + nWorkers);
    }
    public synchronized void reset() {
        nCompletionsNotified = 0;
    }

    public synchronized void waitCompletion() throws InterruptedException {
        while (nCompletionsNotified < nWorkers) {
            wait();
        }
    }

    public synchronized void dec() {
        if(nCompletionsNotified > 0) nCompletionsNotified--;
//        System.out.println("NUM IS " + nCompletionsNotified);
    }

    public synchronized void notifyCompletion() {
        nCompletionsNotified++;
//        System.out.println("NUM IS " + nCompletionsNotified);
        notifyAll();
    }
}
