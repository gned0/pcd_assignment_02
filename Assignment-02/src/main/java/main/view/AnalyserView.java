package main.view;

import main.AbstractSourceAnalyser;
import main.utility.Pair;

import java.util.List;

public class AnalyserView {
    private final ViewFrame frame;

    public AnalyserView(AbstractSourceAnalyser analyser){ this.frame = new ViewFrame(900, 400, analyser); }
    public AnalyserView(final int w, final int h, AbstractSourceAnalyser analyser){ this.frame = new ViewFrame(w, h, analyser); }
    public void display() {
        javax.swing.SwingUtilities.invokeLater(() -> this.frame.setVisible(true));
    }

    public void update(final List<Integer> intervals,
                       final List<Pair<String, Integer>> topFiles,
                       final int ranges,
                       final int maxL){
        // updates distribution graph and top files
        this.frame.updateTopFiles(topFiles);
        this.frame.updateDistributionGraph(intervals, ranges, maxL);
    }
    public void changeState(final String s){
        this.frame.updateState(s);
    }

}
