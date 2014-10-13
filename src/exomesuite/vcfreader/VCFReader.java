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
package exomesuite.vcfreader;

import exomesuite.graphic.TabCell;
import exomesuite.tsvreader.TSVReader;
import exomesuite.utils.OS;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class VCFReader {

    private final File file;
    private VCFReaderController controller;
    private TableView<String[]> table;
    private List<String> genotypes;
    private int totalLines;
    private Label[] statsValues;
    private TextField[] filters;
    private Map<String, Integer>[] stats;
    private final int MAX_ROWS = 100000;
    private int NUMBER_OF_COLUMNS;
    private Parent view;

    public VCFReader(File file) {
        this.file = file;
    }

    public void show() {
        Scene scene = new Scene(getView());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    }

    private void initializeTable() {
        table = (TableView<String[]>) controller.getTable();
        table.setSortPolicy((TableView<String[]> param) -> false);
        genotypes = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            // Drop headers
            String line;
            while (((line = in.readLine()) != null) && line.startsWith("##"));
            String[] header = line.substring(1).split("\t");
            NUMBER_OF_COLUMNS = header.length;
            statsValues = new Label[NUMBER_OF_COLUMNS];
            filters = new TextField[NUMBER_OF_COLUMNS];
            // Prepare stats maps.
            stats = new Map[NUMBER_OF_COLUMNS];
            for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
                stats[i] = new TreeMap<>();
            }
            for (int i = 0; i < header.length; i++) {
                final int index = i;
                Label title = new Label(header[i]);
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
                tc.setCellFactory((TableColumn<String[], String> param) -> new TabCell());
                table.getColumns().add(tc);
            }
            AtomicInteger counter = new AtomicInteger();
            in.lines().forEach((String t) -> {
                counter.incrementAndGet();
                table.getItems().add(t.split("\t"));
            });
            totalLines = counter.get();
            controller.getLines().setText(counter + "");
            final double percentage = (double) counter.get() * 100 / totalLines;
            controller.getCurrentLines().setText(String.format("%d (%.2f%%)", counter.get(),
                    percentage));
            updateTable();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void updateTable() {
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            table.getItems().clear();
            for (Map map : stats) {
                map.clear();
            }
            while (in.readLine().startsWith("##"));
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
            controller.getCurrentLines().setText(String.format("%d (%.2f%%)", valid.get(),
                    percentage));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TSVReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TSVReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean filter(String[] row) {
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            if (!filters[i].getText().isEmpty() && !row[i].matches(filters[i].getText())) {
                return false;
            }
        }
        return true;
    }

    private void updateStats(String[] row) {
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            Integer val = stats[i].get(row[i]);
            stats[i].put(row[i], (val != null) ? val + 1 : 1);
        }
    }

    public Parent getView() {
        if (view == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("VCFReader.fxml"));
                view = loader.load();
                controller = loader.getController();
                controller.getFile().setText(file.getAbsolutePath());
                controller.getSize().setText(OS.humanReadableByteCount(file.length(), false));
                initializeTable();
            } catch (IOException ex) {
                Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        return view;
    }
}
