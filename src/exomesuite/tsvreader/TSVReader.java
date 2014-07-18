/*
 * Copyright (C) 2014 UICHUIMI
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package exomesuite.tsvreader;

import exomesuite.ExomeSuite;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class TSVReader {

    private final File file;

    private final static int MAX_ROWS = 150;

    private List<String> headers;

    private Map<String, Integer>[] stats;

    private Label[] statsValues;

    private TextField[] filters;

    private int NUMBER_OF_COLUMNS;

    private ReaderViewController viewController;

    private VBox view;

    private TableView<String[]> table;

    private Stage stage;

    private int totalLines;

    public TSVReader(File file) {
        this.file = file;
        loadView();
    }

    /**
     * Create table.
     */
    public void initializeTable() {
        table.setSortPolicy((TableView<String[]> param) -> false);
        viewController.getFile().setText(file.getAbsolutePath());
        viewController.getSize().setText(humanReadableByteCount(file.length(), false));
        viewController.getExport().setOnAction((ActionEvent event) -> exportResults());
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            // Store headers in a list.
            headers = Arrays.asList(in.readLine().split("\t"));
            NUMBER_OF_COLUMNS = headers.size();
            statsValues = new Label[NUMBER_OF_COLUMNS];
            filters = new TextField[NUMBER_OF_COLUMNS];
            // Create columns.
            for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                final int index = i;
                Label title = new Label(headers.get(index));
                Label st = new Label();
                TextField filter = new TextField();
                filter.setBackground(new Background(new BackgroundFill(Color.WHITE,
                        CornerRadii.EMPTY, new Insets(2))));
                filter.setPromptText("Filter me");
                filter.setOnAction((ActionEvent event) -> updateTable());
                statsValues[index] = st;
                filters[index] = filter;
                VBox head = new VBox(2, title, st, filter);
                head.setAlignment(Pos.TOP_CENTER);
                TableColumn<String[], String> tc = new TableColumn<>();
                tc.setGraphic(head);
                tc.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> param)
                        -> new SimpleStringProperty(param.getValue()[index]));
                tc.setCellFactory((TableColumn<String[], String> param) -> new MyCell());
                table.getColumns().add(tc);
            }
            // Prepare stats maps.
            stats = new Map[NUMBER_OF_COLUMNS];
            for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                stats[i] = new TreeMap<>();
            }
            // Calculate the length of the file. This will consume the rest of the lines in the file.
            AtomicInteger total = new AtomicInteger(0);
            in.lines().forEach((String t) -> total.incrementAndGet());
            viewController.getLines().setText(total.toString());
            totalLines = total.get();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TSVReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TSVReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateTable();
        stage = new Stage();
        Scene scene = new Scene(view);
        scene.getStylesheets().add(ExomeSuite.class.getResource("main.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * Filters the rows, updates stats and prints only MAX_ROWS rows.
     */
    public void updateTable() {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            table.getItems().clear();
            for (Map map : stats) {
                map.clear();
            }
            in.readLine(); // Skip header.
            AtomicInteger valid = new AtomicInteger(0);
            in.lines().forEachOrdered((String t) -> {
                String[] row = t.split("\t");
                if (filter(row)) {
                    updateStats(row);
                    if (valid.getAndIncrement() < MAX_ROWS) {
                        table.getItems().add(row);
                    }
                }
            });
            for (int i = 0; i < statsValues.length; i++) {
                statsValues[i].setText(stats[i].size() + "");
            }
            final double percentage = (double) valid.get() * 100 / totalLines;
            viewController.getCurrentLines().setText(String.format("%d (%.2f%%)", valid.get(),
                    percentage));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TSVReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TSVReader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     */
    public void show() {
        initializeTable();
    }

    /**
     * Loads view for first time.
     */
    private void loadView() {
        if (view == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ReaderView.fxml"));
                view = loader.load();
                viewController = loader.getController();
                table = viewController.getTable();
            } catch (IOException ex) {
                Logger.getLogger(TSVReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Takes a byte value and convert it to the corresponding human readable unit.
     *
     * @param bytes
     * @param si
     * @return
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Adds the values of the fields to the corresponding column stat counter.
     *
     * @param row
     */
    private void updateStats(String[] row) {
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            Integer val = stats[i].get(row[i]);
            stats[i].put(row[i], (val != null) ? val + 1 : 1);
        }
    }

    /**
     *
     * @param row
     * @return true if passed all the filters, false if any filter do not match.
     */
    private boolean filter(String[] row) {
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            if (!filters[i].getText().isEmpty() && !row[i].matches(filters[i].getText())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Asks the user to select a file. If the FileChooser returns a file, the filtered lines will be
     * stored in the new file.
     */
    private void exportResults() {
        String[] fts = {".mist"};
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("MIST file", fts));
        File f = chooser.showSaveDialog(stage);
        if (f != null) {
            if (!f.getAbsolutePath().endsWith(".mist")) {
                f = new File(f.getAbsolutePath().concat(".mist"));
            }
            saveData(f);
        }

    }

    /**
     * Stores all the lines filtered from input file to f.
     *
     * @param f
     */
    private void saveData(File f) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(f));
                BufferedReader in = new BufferedReader(new FileReader(file))) {
            // Store header
            writeArray(out, in.readLine().split("\t"));
            String line;
            while ((line = in.readLine()) != null) {
                String[] row = line.split("\t");
                if (filter(row)) {
                    writeArray(out, row);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TSVReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Writes an array as a tab separated line in a output BufferedWriter.
     *
     * @param out
     * @param array
     * @throws IOException
     */
    private void writeArray(BufferedWriter out, String[] array) throws IOException {
        int i = 0;
        while (i < array.length - 1) {
            out.write(array[i++] + "\t");
        }
        out.write(array[i]);
        out.newLine();
    }

    private static class MyCell extends TableCell<String[], String> {

        TextField textField;

        public MyCell() {
            textField = new TextField();
            textField.setEditable(false);
            textField.setBackground(Background.EMPTY);
            textField.setPadding(new Insets(1));
        }

        @Override
        protected void updateItem(String t, boolean bln) {
            super.updateItem(t, bln);
            textField.setText(getItem());
            setGraphic(textField);
        }

    }
}
