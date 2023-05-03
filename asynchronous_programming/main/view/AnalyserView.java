package main.view;

import main.InputListener;
import main.utility.Pair;

import java.util.ArrayList;
import java.util.List;

public class AnalyserView {
    private final ViewFrame frame;

    public AnalyserView(){ this.frame = new ViewFrame(900, 400); }
    public AnalyserView(final int w, final int h){ this.frame = new ViewFrame(w, h); }
    public void display() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            this.frame.setVisible(true);
        });
    }
    public void addListener(final InputListener l){
        this.frame.addListener(l);
    }
    public void update(final List<Integer> intervals, final List<Pair<String, Integer>> topFiles){
        // updates distribution graph and top files
        this.frame.updateTopFiles(topFiles);
        this.frame.updateDistributionGraph((ArrayList<Integer>) intervals);
    }
    public void changeState(final String s){
        this.frame.updateState(s);
    }

}
