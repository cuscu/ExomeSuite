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

import exomesuite.graphic.IndexCell;
import exomesuite.graphic.NaturalCell;
import exomesuite.graphic.SizableImage;
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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 * Main scenario for the TSV Reader View.
 *
 * @author Pascual Lorente Arencibia
 */
public class VCFTable extends SplitPane {

    @FXML
    private TableView<Variant> table;
//    @FXML
//    private VariantGenotype formatBox;
    @FXML
    private VariantInfoTable variantInfo;
    @FXML
    private VBox filtersPane;
    @FXML
    private Button addFilter;
    @FXML
    private Label infoLabel;

    /**
     * The VCF file.
     */
    private final File vcfFile;
    /**
     * Total number of lines.
     */
    private final AtomicInteger totalLines = new AtomicInteger();
    /**
     * Current unfiltered lines.
     */
    private final AtomicInteger lines = new AtomicInteger();

    private final TableColumn<Variant, String> lineNumber = new TableColumn<>();
    private final TableColumn<Variant, String> chrom = new TableColumn<>("Chrom");
    private final TableColumn<Variant, String> position = new TableColumn<>("Position");
    private final TableColumn<Variant, String> variant = new TableColumn<>("Variant");
    private final TableColumn<Variant, String> rsId = new TableColumn<>("ID");
    private final TableColumn<Variant, String> qual = new TableColumn<>("Qual");
    private final TableColumn<Variant, String> filter = new TableColumn<>("Filter");
    private final List<VariantListener> listeners = new ArrayList<>();
    private List<String> infos = new ArrayList<>();
    private VCFHeader vcfHeader;

    public VCFTable(File vcfFile) {
        this.vcfFile = vcfFile;
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
        addFilter.setGraphic(new SizableImage("/exomesuite/img/new.png", 16));
        addFilter.setOnAction(e -> addFilter());
        table.setSortPolicy(view -> false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(lineNumber, chrom, position, rsId, variant, qual, filter);
        table.setEditable(true);
        chrom.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getChrom()));
        position.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPos() + ""));
        variant.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getRef()
                + "->" + param.getValue().getAlt()));
        rsId.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getId()));
        filter.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilter()));
        qual.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getQual() + ""));
        lineNumber.setCellFactory(param -> new IndexCell());
        chrom.setCellFactory(column -> new NaturalCell());
        position.setCellFactory(column -> new NaturalCell());
        variant.setCellFactory(column -> new NaturalCell());
        rsId.setCellFactory(column -> new NaturalCell());
        filter.setCellFactory(column -> new NaturalCell());
        qual.setCellFactory(column -> new NaturalCell());

        table.getSelectionModel().selectedItemProperty().addListener(e -> updateVariant());
        addListener(variantInfo);
//        addListener(formatBox);
        loadFile();
    }

    private void loadFile() {
        vcfHeader = new VCFHeader(vcfFile);
        table.getItems().clear();
        totalLines.set(0);
        try (BufferedReader in = new BufferedReader(new FileReader(vcfFile))) {
            in.lines().forEachOrdered(line -> {
                if (!line.startsWith("#")) {
                    table.getItems().add(toVariant(line));
                    totalLines.incrementAndGet();
                } else {
                    addHeader(line);
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(VCFTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        lines.set(totalLines.get());
        updateInfo();
    }

    /**
     * Returns the opened file.
     *
     * @return
     */
    public File getFile() {
        return vcfFile;
    }

    private Variant toVariant(String t) {
        return new Variant(t);
    }

    /**
     * Fills the bottom table.
     */
    private void updateVariant() {
        Variant v = table.getSelectionModel().getSelectedItem();
        // Call listeners
        listeners.forEach(t -> t.variantChanged(v, vcfHeader));
    }

    /**
     * Adds a listener that will listen for changes in the variant selected.
     *
     * @param listener
     */
    public void addListener(VariantListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Nothing to do here.
     *
     * @param listener
     */
    public void removeListener(VariantListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Adds a
     */
    private void addFilter() {
        VCFFilterPane filterPane = new VCFFilterPane(infos);
        filterPane.setOnUpdate(e -> filter());
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
                    final Variant v = new Variant(line);
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
    private boolean filter(Variant variant) {
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
        infoLabel.setText(String.format("%,d / %,d (%.2f%%)", lines.get(), totalLines.get(),
                percentage));

    }

    private void addHeader(String line) {
        if (line.startsWith("##INFO=<")) {
            // ##INFO=<ID=DP,...
            String[] row = line.substring(8).split(",");
            // ID=DP
            String name = row[0].split("=")[1];
            infos.add(name);
        }
    }

}
