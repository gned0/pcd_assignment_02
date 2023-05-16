package main.view;

import main.AbstractSourceAnalyser;
import main.InputListener;
import main.utility.Pair;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ViewFrame extends JFrame implements ActionListener {

    private final AbstractSourceAnalyser analyser;
    private final JTextField state;
    private JFileChooser directoryChooser;
    private static int numRanges;
    private static int numMaxL;
    private final ExplorationPanel expPanel;
    private final ArrayList<InputListener> listeners;

    public ViewFrame(int w, int h, AbstractSourceAnalyser analyser){
        super("Directory Explorer");
        this.analyser = analyser;
        setSize(w,h);
        listeners = new ArrayList<>();

        JButton stopButton = new JButton("stop");
        JPanel controlPanel = new JPanel();
        controlPanel.add(stopButton);

        expPanel = new ExplorationPanel();
        expPanel.setSize(w,h);

        JPanel infoPanel = new JPanel();
        state = new JTextField(20);
        state.setText("Idle");
        state.setEditable(false);
        infoPanel.add(new JLabel("State"));
        infoPanel.add(state);
        JPanel cp = new JPanel();
        LayoutManager layout = new BorderLayout();
        cp.setLayout(layout);
        cp.add(BorderLayout.NORTH,controlPanel);
        cp.add(BorderLayout.CENTER, expPanel);
        cp.add(BorderLayout.SOUTH, infoPanel);
        setContentPane(cp);

        stopButton.addActionListener(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void updateState(final String s) {
        SwingUtilities.invokeLater(() -> state.setText(s));
    }

    public void actionPerformed(final ActionEvent ev){
        String cmd = ev.getActionCommand();
        if (cmd.equals("stop")){
            notifyStopped();
        }
    }

    public void addListener(final InputListener l){
        listeners.add(l);
    }



    private void notifyStopped(){
        this.analyser.stopExecution();
    }

    public void updateTopFiles(final List<Pair<String, Integer>> topFiles){
        SwingUtilities.invokeLater(() -> expPanel.updateTopFiles(topFiles));
    }
    public void updateDistributionGraph(final List<Integer> intervals, final int ranges, final int maxL){
        SwingUtilities.invokeLater(() -> expPanel.updateDistributionGraph(intervals, ranges, maxL));
    }

    public static class ExplorationPanel extends JPanel {

        private DefaultTableModel intervalsTable;
        private DefaultTableModel topFilesTable;

        public ExplorationPanel() {
            this.initTopFilesTable();
            this.initDistributionGraph();
        }

        private void initTopFilesTable() {
            topFilesTable = new DefaultTableModel();
            topFilesTable.addColumn("File");
            topFilesTable.addColumn("Number of lines");
            JTable topSourceTable = new JTable(topFilesTable);
            JScrollPane topSourceTableScrollPane = new JScrollPane(topSourceTable);
            topSourceTableScrollPane.setPreferredSize(new Dimension(350, 200));
            topSourceTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.add(topSourceTableScrollPane, BorderLayout.EAST);
        }

        public void updateTopFiles(final List<Pair<String, Integer>> topFiles) {
            topFilesTable.setRowCount(0);
            synchronized (topFiles) {
                topFiles.forEach(f -> this.topFilesTable.addRow(new Object[]{f.first, f.second}));
            }
        }

        private void initDistributionGraph() {
            intervalsTable = new DefaultTableModel();
            intervalsTable.addColumn("Interval");
            intervalsTable.addColumn("Number of files");
            JTable topSourceTable = new JTable(intervalsTable);
            JScrollPane topSourceTableScrollPane = new JScrollPane(topSourceTable);
            topSourceTableScrollPane.setPreferredSize(new Dimension(350, 200));
            topSourceTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            this.add(topSourceTableScrollPane, BorderLayout.WEST);
        }


        public void updateDistributionGraph(final List<Integer> intervals, final int ranges, final int maxL) {
            intervalsTable.setRowCount(0);

            synchronized (intervals) {
                for (int i = 0; i < intervals.size(); i++) {
                    String tmp;
                    if (i == 0) {
                        tmp = "Range [0, " + ((maxL / (ranges - 1)) - 1) + "]: ";
                    } else if (i == ranges - 1) {
                        tmp = "Range [" + maxL + ", infinity]: ";
                    } else {
                        tmp = "Range [" + (maxL / (ranges - 1)) * i + "," + (((maxL / (ranges - 1)) * (i + 1)) - 1) + "]: ";
                    }
                    this.intervalsTable.addRow(new Object[]{tmp, intervals.get(i)});
                }
            }
        }
    }
}
