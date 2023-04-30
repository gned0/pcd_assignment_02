package main;


import main.utility.Flag;
import main.utility.StartSynch;

public class Controller implements InputListener{
    private final StartSynch synch;
    private final Flag stopFlag;

    public Controller(final StartSynch synch, final Flag stopFlag){
        this.synch = synch;
        this.stopFlag = stopFlag;
    }

    @Override
    public void started(final String d, final int i, final int ml, final int nTopFiles) {
        synchronized (stopFlag){
            stopFlag.reset();
        }
        synch.notifyStarted(d, i, ml, nTopFiles);
    }

    @Override
    public void stopped() {
        stopFlag.set();
    }
}
