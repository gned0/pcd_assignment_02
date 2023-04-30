/*package main;

import main.utility.Flag;
import main.utility.StartSynch;
import main.view.AnalyserView;

public class DirectoryExplorer {
    public static void main(String[] args) {

        int w = 900;
        int h = 400;

        AnalyserView view = new AnalyserView(w, h);
        StartSynch synch = new StartSynch();
        Flag stopFlag = new Flag();

        new MasterAgent(synch, stopFlag, view).start();
        Controller controller = new Controller(synch, stopFlag);
        view.addListener(controller);
        view.display();
    }
}*/
