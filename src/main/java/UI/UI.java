package UI;

import IO.GtfsReader;
import IO.JsonReader;
import IO.JsonWriter;
import Model.Connection;
import Model.Stop;
import Search.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

public class UI extends JFrame {
    private final GtfsReader gtfsReader;
    private JComboBox<String> startStopComboBox;
    private JComboBox<String> endStopComboBox;
    private JTextField startStopSearchField;
    private JTextField endStopSearchField;
    private LocalDateTime selectedDateTime;
    private JPanel resultPanel;
    private JScrollPane resultScrollPane;
    private JFrame frame;
    private JRadioButton departureTimeRadio;

    public UI(GtfsReader gR) {
        gtfsReader = gR;
        UIWindow();
    }

    public void UIWindow() {
        // Frame inicializálása
        frame = new JFrame("Menetrend kereső");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Menü
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        LocalDateTime now = LocalDateTime.now();
        JMenu timeMenu = new JMenu(writeDateTime(now));
        menuBar.add(timeMenu);
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        fileMenu.add(exitMenuItem);
        exitMenuItem.addActionListener(e -> System.exit(0));
        JMenuItem openMenuItem = new JMenuItem("Open");
        fileMenu.add(openMenuItem);
        openMenuItem.addActionListener(e -> {
           String selectedFolder=selectFolder();
            try {
                gtfsReader.loadData(selectedFolder);
            } catch (IOException | InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        });
        JMenuItem exportMenuItem = new JMenuItem("Export");
        fileMenu.add(exportMenuItem);
        String path = Paths.get("src", "main", "resources","Connections.json").toString();
        exportMenuItem.addActionListener(e -> {
            JsonWriter.writeConnectionsToFile(gtfsReader.getConnections(),path);
        });
        JMenuItem importMenuItem = new JMenuItem("Import");
        fileMenu.add(importMenuItem);
        importMenuItem.addActionListener(e -> {
            List<Connection>connections=JsonReader.loadConnectionsFromFile(path);
        });

        // Panelek
        JPanel searchPanel = new JPanel();
        searchPanel.setPreferredSize(new Dimension(300, 200));
        searchPanel.setLayout(new GridLayout(12, 2, 20, 20));

        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultScrollPane = new JScrollPane(resultPanel);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Frame-hez panelek hozzáadása
        frame.setLayout(new BorderLayout());
        frame.add(searchPanel, BorderLayout.WEST);
        frame.add(resultScrollPane, BorderLayout.CENTER);

        // Kezdő állomás kereső
        searchPanel.add(new JLabel("Indulás:"));
        startStopSearchField = new JTextField("Honnan:");
        startStopSearchField.setToolTipText("Újbuda-központ M");
        searchPanel.add(startStopSearchField);
        startStopComboBox = new JComboBox<>(gtfsReader.getStopNames().toArray(new String[0]));
        startStopComboBox.setEditable(false);
        searchPanel.add(new JLabel());
        searchPanel.add(startStopComboBox);
        startStopSearchField.addCaretListener(e -> filterComboBox(startStopComboBox, startStopSearchField));
        addFocusListener(startStopSearchField, "Honnan:");

        // Érkező állomás kereső
        searchPanel.add(new JLabel("Érkezés:"));
        endStopSearchField = new JTextField("Hova:");
        endStopSearchField.setToolTipText("Örs vezér tere M+H");
        searchPanel.add(endStopSearchField);
        endStopComboBox = new JComboBox<>(gtfsReader.getStopNames().toArray(new String[0]));
        endStopComboBox.setEditable(false);
        searchPanel.add(new JLabel());
        searchPanel.add(endStopComboBox);
        endStopSearchField.addCaretListener(e -> filterComboBox(endStopComboBox, endStopSearchField));
        addFocusListener(endStopSearchField, "Hova:");

        //keresési mód választó
        ButtonGroup timeCriteriaGroup = new ButtonGroup();
        departureTimeRadio = new JRadioButton("Indulási idő", true);
        JRadioButton arrivalTimeRadio = new JRadioButton("Érkezési idő");
        timeCriteriaGroup.add(departureTimeRadio);
        timeCriteriaGroup.add(arrivalTimeRadio);
        searchPanel.add(departureTimeRadio);
        searchPanel.add(arrivalTimeRadio);


        JButton dateTimeButton = new JButton("Indulás most");
        ActionListener radioListener = e -> {
            if (departureTimeRadio.isSelected()) {
                dateTimeButton.setText("Indulás most"); // Indulási idő kiválasztva
            } else {
                dateTimeButton.setText("Érkezés most"); // Érkezési idő kiválasztva
            }
        };
        departureTimeRadio.addActionListener(radioListener);
        arrivalTimeRadio.addActionListener(radioListener);

        // Dátum és idő választó
        searchPanel.add(new JLabel("Dátum és idő:"));
        selectedDateTime = LocalDateTime.now();
        dateTimeButton.addActionListener(e -> {
            selectedDateTime = pickDateTime();
            if (selectedDateTime != null) {
                dateTimeButton.setText(writeDateTime(selectedDateTime));
            }
        });
        searchPanel.add(dateTimeButton);

        // Keresés gomb
        JButton searchButton = new JButton("Keresés");
        searchButton.addActionListener(new SearchButton());
        searchPanel.add(new JLabel());
        searchPanel.add(searchButton);

        // Üres hely
        for (int i = 0; i < 6; i++) {
            searchPanel.add(new JLabel());
        }

        frame.setVisible(true);
    }

    private void filterComboBox(JComboBox<String> stopComboBox, JTextField textField) {
        if(textField.getText().isEmpty() || ("Honnan:").toLowerCase().contains(textField.getText().toLowerCase()) || ("Hova:").toLowerCase().contains(textField.getText().toLowerCase())) {
            for (String stopName:gtfsReader.getStopNames()){
                stopComboBox.addItem(stopName);
            }
            return;
        }
        stopComboBox.removeAllItems();
        for (String stopName: gtfsReader.getStopNames()) {
            if (stopName.toLowerCase().contains(textField.getText().toLowerCase())) {
                stopComboBox.addItem(stopName);
            }
        }
        if (stopComboBox.getItemCount() > 0) {
            stopComboBox.setSelectedIndex(0);
        }
    }
    private void addFocusListener(JTextField textField, String defaultText) {
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(defaultText)) {
                    textField.setText(null);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(defaultText);

                }
            }
        });
    }

    private LocalDateTime pickDateTime() {
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm");
        dateSpinner.setEditor(timeEditor);

        int result = JOptionPane.showConfirmDialog(this, dateSpinner, "Válassz dátumot és időt", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            return LocalDateTime.parse(timeEditor.getFormat().format(dateSpinner.getValue()).replace(" ", "T"));
        }
        return null;
    }

    private class SearchButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String startStopString = (String) startStopComboBox.getSelectedItem();
            String endStopString = (String) endStopComboBox.getSelectedItem();
            Stop startStop = null;
            Stop endStop = null;

            for (Stop stop : gtfsReader.getStopsMap().values()) {
                if (stop.getStopName().equals(startStopString)) {
                    startStop = stop;
                }
                if (stop.getStopName().equals(endStopString)) {
                    endStop = stop;
                }
                if (startStop != null && endStop != null) {
                    break;
                }
            }

            if (startStop == null || endStop == null || startStop.equals(endStop)) {
                JOptionPane.showMessageDialog(UI.this, "Helyes adatokat adjon meg.", "Hiba", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (selectedDateTime == null) {
                JOptionPane.showMessageDialog(UI.this, "Válassz dátumot és időt!", "Hiba", JOptionPane.ERROR_MESSAGE);
                return;
            }
            List<List<Connection>> routes;
            if(departureTimeRadio.isSelected()) {
                ForwardSearch forwardSearch=new ForwardSearch(gtfsReader.getConnections());
                routes = forwardSearch.findShortestRoutes(startStop, endStop, selectedDateTime);
            }else{
                BackwardSearch backwardSearch=new BackwardSearch(gtfsReader.getConnections());
                routes=backwardSearch.findShortestRoutes(startStop, endStop, selectedDateTime);
            }

            displayResults(routes);
        }

        private void displayResults(List<List<Connection>> routes) {
            resultPanel.removeAll();

            if (routes.isEmpty()) {
                resultPanel.add(new JLabel("Nincs találat."));
            } else {
                for (List<Connection> route : routes) {
                    for (Connection connection : route) {
                        JLabel routePartLabel = new JLabel(connection.toString());
                        routePartLabel.setForeground(connection.getRoute().getRouteColor());
                        resultPanel.add(routePartLabel);
                    }
                    resultPanel.add(Box.createVerticalStrut(10));
                }
            }

            resultPanel.revalidate();
            resultPanel.repaint();
        }
    }



    public String writeDateTime(LocalDateTime selectedDateTime) {
        return String.format("%d-%02d-%02d %02d:%02d",
                selectedDateTime.getYear(),
                selectedDateTime.getMonthValue(),
                selectedDateTime.getDayOfMonth(),
                selectedDateTime.getHour(),
                selectedDateTime.getMinute());
    }
    private static String selectFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = folderChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = folderChooser.getSelectedFile();
            return selectedFolder.getAbsolutePath();
        }

        return null;
    }
}
