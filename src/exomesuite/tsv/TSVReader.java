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
package exomesuite.tsv;

import exomesuite.MainViewController;
import exomesuite.graphic.SizableImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class TSVReader extends SplitPane {

    @FXML
    private TableView<String[]> table;
    @FXML
    private VBox filtersPane;
    @FXML
    private Button addFilter;
    @FXML
    private Label infoLabel;

    private File file;
    private String[] headers;

    private AtomicInteger totalLines = new AtomicInteger();
    private AtomicInteger currentLines = new AtomicInteger();

    public TSVReader() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("TSVReader.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            MainViewController.showException(e);
        }
    }

    @FXML
    private void initialize() {
        addFilter.setGraphic(new SizableImage("exomesuite/img/new.png", 16));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        addFilter.setOnAction(event -> {
            TSVFilterPane filterPane = new TSVFilterPane(Arrays.asList(headers));
            filterPane.setOnAccept(e -> filter());
            filterPane.setOnDelete(e -> {
                filtersPane.getChildren().remove(filterPane);
                filter();
            });
            filtersPane.getChildren().add(filterPane);
        });
    }

    public void setFile(File file) {
        this.file = file;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            headers = reader.readLine().split("\t");
            generateColumns();
            reader.lines().forEachOrdered(line -> table.getItems().add(line.split("\t")));
            totalLines.set(table.getItems().size());
            currentLines.set(totalLines.get());
        } catch (Exception e) {
            MainViewController.showException(e);
        }
        setInfo();
    }

    private void generateColumns() {
        for (int i = 0; i < headers.length; i++) {
            final int index = i;
            TableColumn<String[], String> tc = new TableColumn<>(headers[i]);
            tc.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> param)
                    -> new SimpleStringProperty(param.getValue()[index]));

            table.getColumns().add(tc);
        }
    }

    private void filter() {
        table.getItems().clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.readLine();
            reader.lines().forEachOrdered(line -> {
                String[] row = line.split("\t");
                boolean accepted = true;
                for (Node node : filtersPane.getChildren()) {
                    TSVFilterPane pane = (TSVFilterPane) node;
                    if (!pane.getFilter().filter(row)) {
                        accepted = false;
                        break;
                    }
                }
                if (accepted) {
                    table.getItems().add(row);
                }
            });
        } catch (Exception e) {
            MainViewController.showException(e);
        }
        currentLines.set(table.getItems().size());
        setInfo();
    }

    private void setInfo() {
        double percentage = 100.0 * currentLines.get() / totalLines.get();
        infoLabel.setText(String.format("%,d / %,d (%.2f%%)", currentLines.get(), totalLines.get(),
                percentage));
        Set<String>[] uniques = new Set[headers.length];
        for (int i = 0; i < uniques.length; i++) {
            uniques[i] = new TreeSet<>();
        }
        table.getItems().stream().forEach(line -> {
            for (int i = 0; i < line.length; i++) {
                uniques[i].add(line[i]);
            }
        });
        for (int i = 0; i < headers.length; i++) {
            table.getColumns().get(i).setText(headers[i] + "\n" + uniques[i].size());
        }
    }

}
