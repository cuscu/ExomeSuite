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
package exomesuite.vcf;

import exomesuite.vcf.VCFFilter;
import exomesuite.graphic.FlatButton;
import exomesuite.graphic.VCFFilterPane;
import exomesuite.graphic.VariantExons;
import exomesuite.graphic.VariantGenotype;
import exomesuite.graphic.VariantInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class VCFTable extends VBox {

    @FXML
    private TableView<Variant2> table;
    @FXML
    private VariantGenotype formatBox;
    @FXML
    private VariantExons variantExons;
    @FXML
    private VariantInfo variantInfo;
    @FXML
    private VBox filtersPane;
    @FXML
    private FlatButton addFilter;
    @FXML
    private Label infoLabel;

    private File vcfFile;
    private final AtomicInteger totalLines = new AtomicInteger();
    private final AtomicInteger lines = new AtomicInteger();

    private final TableColumn<Variant2, String> lineNumber = new TableColumn<>();
    private final TableColumn<Variant2, String> chrom = new TableColumn<>("Chrom");
    private final TableColumn<Variant2, String> position = new TableColumn<>("Position");
    private final TableColumn<Variant2, String> variant = new TableColumn<>("Variant");
    private final TableColumn<Variant2, String> rsId = new TableColumn<>("ID");
    private final TableColumn<Variant2, String> qual = new TableColumn<>("Qual");
    private final TableColumn<Variant2, String> filter = new TableColumn<>("Filter");
    private final List<VariantListener> listeners = new ArrayList<>();

    public VCFTable() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("VCFTable.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(VCFTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        addFilter.setGraphic(new ImageView("/exomesuite/img/more.png"));
        addFilter.setOnAction(e -> addFilter());
        table.setSortPolicy((TableView<Variant2> param) -> false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(lineNumber, chrom, position, rsId, variant, qual, filter);

        chrom.setCellValueFactory((TableColumn.CellDataFeatures<Variant2, String> param)
                -> new SimpleStringProperty(param.getValue().getChrom()));
        position.setCellValueFactory((TableColumn.CellDataFeatures<Variant2, String> param)
                -> new SimpleStringProperty(param.getValue().getPos() + ""));
        variant.setCellValueFactory((TableColumn.CellDataFeatures<Variant2, String> param)
                -> new SimpleStringProperty(param.getValue().getRef() + "->"
                        + param.getValue().getAlt()));
        rsId.setCellValueFactory((TableColumn.CellDataFeatures<Variant2, String> param)
                -> new SimpleStringProperty(param.getValue().getId()));
        filter.setCellValueFactory((TableColumn.CellDataFeatures<Variant2, String> param)
                -> new SimpleStringProperty(param.getValue().getFilter()));
//        info.setCellValueFactory((TableColumn.CellDataFeatures<Variant2, String> param)
//                -> new SimpleStringProperty(param.getValue().getInfo()));
        qual.setCellValueFactory((TableColumn.CellDataFeatures<Variant2, String> param)
                -> new SimpleStringProperty(param.getValue().getQual() + ""));
        lineNumber.setCellFactory(param -> new IndexCell());
        chrom.setCellFactory(new StyledCell());
        position.setCellFactory(new StyledCell());
        variant.setCellFactory(new StyledCell());
        rsId.setCellFactory(new StyledCell());
        filter.setCellFactory(new StyledCell());
//        info.setCellFactory(new StyledCell());
        qual.setCellFactory(new StyledCell());
        table.getSelectionModel().selectedItemProperty().addListener(e -> updateVariant());
        addListener(variantInfo);
        addListener(variantExons);
        addListener(formatBox);

    }

    public void setFile(File vcfFile) {
        this.vcfFile = vcfFile;
        table.getItems().clear();
        totalLines.set(0);
        try (BufferedReader in = new BufferedReader(new FileReader(vcfFile))) {
            in.lines().forEachOrdered(line -> {
                if (!line.startsWith("#")) {
                    table.getItems().add(toVariant(line));
                    totalLines.incrementAndGet();
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(VCFTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        lines.set(totalLines.get());
        updateInfo();
    }

    public File getFile() {
        return vcfFile;
    }

    private Variant2 toVariant(String t) {
        return new Variant2(t);
    }

    /**
     * Fills the bottom table.
     */
    private void updateVariant() {
        Variant2 v = table.getSelectionModel().getSelectedItem();
        // Call listeners
        listeners.forEach(t -> t.variantChanged(v));
    }

    public void addListener(VariantListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(VariantListener listener) {
        this.listeners.remove(listener);
    }

    private void addFilter() {
        VCFFilterPane filterPane = new VCFFilterPane();
        filterPane.setOnAccept(e -> filter());
        filterPane.setOnDelete(e -> {
            filtersPane.getChildren().remove(filterPane);
            filter();
        });
        filtersPane.getChildren().add(filterPane);

    }

    /**
     * Runs across the variants filtering them.
     */
    private void filter() {
        table.getItems().clear();
        lines.set(0);
        try (BufferedReader in = new BufferedReader(new FileReader(vcfFile))) {
            in.lines().forEachOrdered(line -> {
                if (!line.startsWith("#")) {
                    final Variant2 v = toVariant(line);
                    if (filter(v)) {
                        table.getItems().add(v);
                        lines.incrementAndGet();
                    }
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(VCFTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateInfo();
    }

    /**
     *
     * @param variant
     * @return true if variant pass all filters.
     */
    private boolean filter(Variant2 variant) {
        boolean pass = true;
        for (Node pane : filtersPane.getChildren()) {
            VCFFilter f = ((VCFFilterPane) pane).getFilter();
            if (!f.filter(variant)) {
                pass = false;
                break;
            }
        }
        return pass;
    }

    private void updateInfo() {
        final double percentage = lines.get() * 100.0 / totalLines.get();
        infoLabel.setText(String.format("%,d/%,d (%.2f%%)", lines.get(), totalLines.get(),
                percentage));

    }

    private static class StyledCell implements
            Callback<TableColumn<Variant2, String>, TableCell<Variant2, String>> {

        private final static String PASS = "pass";
        private final static String NO_PASS = "no-pass";

        FilterClass filter;

        @Override
        public TableCell<Variant2, String> call(TableColumn<Variant2, String> param) {
            return new TableCell<Variant2, String>() {

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                    } else {
                        getStyleClass().remove(PASS);
                        getStyleClass().remove(NO_PASS);
//                        switch (filter) {
//                            case PASS:
//                                getStyleClass().add(PASS);
//                                break;
//                            case NO_PASS:
//                                getStyleClass().add(NO_PASS);
//                        }
                        setAlignment(Pos.CENTER_RIGHT);
                        setText(item);
                    }
                }

            };
        }
    }

    /**
     * The first column shows line index
     */
    private static class IndexCell extends TableCell<Variant2, String> {

        private final static String PASS = "pass";
        private final static String NO_PASS = "no-pass";

        FilterClass filter;

        public IndexCell() {
            getStyleClass().add("index-cell");
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
            } else {
                getStyleClass().remove(PASS);
                getStyleClass().remove(NO_PASS);
//                switch (filter) {
//                    case PASS:
//                        getStyleClass().add(PASS);
//                        break;
//                    case NO_PASS:
//                        getStyleClass().add(NO_PASS);
//                }
                setAlignment(Pos.CENTER_RIGHT);
                setText((1 + getIndex()) + "");
            }
        }

    }

    private enum FilterClass {

        PASS, NO_PASS
    }

}
