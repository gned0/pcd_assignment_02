package main.view;

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

    private final JTextField directory;
    private final JTextField intervals;
    private final JTextField maxL;
    private final JTextField state;
    private final JTextField nTopFiles;
    private JFileChooser directoryChooser;
    private final JButton directoryButton;
    private static int numRanges;
    private static int numMaxL;
    private final ExplorationPanel expPanel;
    private final ArrayList<InputListener> listeners;

    public ViewFrame(int w, int h){
        super("Directory Explorer");
        setSize(w,h);
        listeners = new ArrayList<>();
        directory = new JTextField(5);
        intervals = new JTextField(5);
        maxL = new JTextField(5);
        nTopFiles = new JTextField(5);

        JButton startButton = new JButton("start");
        JButton stopButton = new JButton("stop");
        JPanel controlPanel = new JPanel();
        controlPanel.add(new JLabel("Directory: "));
        controlPanel.add(directory);
        directoryButton = new JButton("Browse");
        controlPanel.add(directoryButton);

        controlPanel.add(new JLabel("N Top Files"));
        controlPanel.add(nTopFiles);

        controlPanel.add(new JLabel("Number of intervals: "));
        controlPanel.add(intervals);
        controlPanel.add(new JLabel("Max lines: "));
        controlPanel.add(maxL);
        controlPanel.add(startButton);
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

        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        directoryButton.addActionListener(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void updateState(final String s) {
        SwingUtilities.invokeLater(() -> state.setText(s));
    }

    public void actionPerformed(final ActionEvent ev){
        String cmd = ev.getActionCommand();
        if (cmd.equals("start")){
            notifyStarted();
        } else if (cmd.equals("stop")){
            notifyStopped();
        } else if(cmd.equals("Browse")){
            directoryChooser = new JFileChooser(".");
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int status = directoryChooser.showSaveDialog(null);

            if(status == JFileChooser.APPROVE_OPTION){
                String path = directoryChooser.getSelectedFile().getAbsolutePath();
                directory.setText(path);
            }
        }
    }

    public void addListener(final InputListener l){
        listeners.add(l);
    }

    private void notifyStarted(){
        String d = directory.getText();
        int i = Integer.parseInt(intervals.getText());
        int ml = Integer.parseInt(maxL.getText());
        int numTopFiles = Integer.parseInt(nTopFiles.getText());
        this.numRanges = i;
        this.numMaxL = ml;

        for (InputListener l: listeners){
            l.started(d, i , ml, numTopFiles);
        }
    }

    private void notifyStopped(){
        for (InputListener l: listeners){
            l.stopped();
        }
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
