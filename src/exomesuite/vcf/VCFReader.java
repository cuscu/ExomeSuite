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

import exomesuite.MainViewController;
import exomesuite.graphic.IndexCell;
import exomesuite.graphic.NaturalCell;
import exomesuite.graphic.SizableImage;
import exomesuite.lfs.LFS;
import exomesuite.utils.FileManager;
import exomesuite.vep.EnsemblRest;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
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
public class VCFReader extends SplitPane {

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
    @FXML
    private Button export;
    @FXML
    private Button vep;
    @FXML
    private Button lfs;

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

    private final TableColumn<Variant, String> lineNumber = new TableColumn();
    private final TableColumn<Variant, String> chrom = new TableColumn("Chrom");
    private final TableColumn<Variant, String> position = new TableColumn("Position");
    private final TableColumn<Variant, String> variant = new TableColumn("Variant");
    private final TableColumn<Variant, String> rsId = new TableColumn("ID");
    private final TableColumn<Variant, String> qual = new TableColumn("Qual");
    private final TableColumn<Variant, String> filter = new TableColumn("Filter");
    private final List<VariantListener> listeners = new ArrayList();
    private Set<String> infos = new TreeSet();
    private Set<String> headers = new TreeSet();
    private VCFHeader vcfHeader;

    /**
     * Creates a new VCFTable to read the vcfFile.
     *
     * @param vcfFile the VCF file
     */
    public VCFReader(File vcfFile) {
        this.vcfFile = vcfFile;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("VCFReader.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        addFilter.setGraphic(new SizableImage("/exomesuite/img/new.png", 16));
        export.setGraphic(new SizableImage("exomesuite/img/save.png", 16));
        addFilter.setOnAction(e -> addFilter());
        export.setOnAction(event -> exportOnAction(event));
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
        vep.setOnAction(event -> getVEPInfo());
        vep.setVisible(false);
        lfs.setOnAction(event -> getLfsInfo());

    }

    private void loadFile() {
        vcfHeader = new VCFHeader(vcfFile);
        table.getItems().clear();
        totalLines.set(0);
        headers.clear();
        infos.clear();
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
            Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        lines.set(totalLines.get());
        updateInfo();
    }

    /**
     * Returns the opened file.
     *
     * @return the opened file
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
     * @param listener the listener to add
     */
    public void addListener(VariantListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Nothing to do here.
     *
     * @param listener the listener to remove
     */
    public void removeListener(VariantListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Adds a
     */
    private void addFilter() {
        VCFFilterPane filterPane = new VCFFilterPane(new ArrayList(infos));
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
            Logger.getLogger(VCFReader.class.getName()).log(Level.SEVERE, null, ex);
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
        headers.add(line);
        if (line.startsWith("##INFO=<")) {
            // ##INFO=<ID=DP,...
            String[] row = line.substring(8).split(",");
            // ID=DP
            String name = row[0].split("=")[1];
            infos.add(name);
        }
    }

    private void exportOnAction(ActionEvent event) {
        File output = FileManager.saveFile("Select output file", FileManager.VCF_FILTER);
        if (output != null) {
            exportTo(output);
            File json = new File(output.getAbsolutePath().replace(".vcf", ".json"));
            // Too big files, cause header repeats for every variant
            //VCF2JSON.vcf2Json(vcfFile, json);
        }
    }

    private void exportTo(File output) {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output)))) {
            headers.forEach(writer::println);
            table.getItems().forEach(writer::println);
        } catch (IOException ex) {
            MainViewController.printException(ex);
        }
    }

    private void getVEPInfo() {
        EnsemblRest.getVepInformation(table.getItems());

    }

    private void getLfsInfo() {
        final String lfsInfo = "##INFO=<ID=LFS,Number=1,Type=Integer,Description=\"Low frequency codon substitution\">";
        if (!headers.contains(lfsInfo)) {
            addHeader(lfsInfo);
        }
        // Store filters status and deactivate all of them
        boolean[] enabled = new boolean[filtersPane.getChildren().size()];
        for (int i = 0; i < enabled.length; i++) {
            VCFFilterPane pane = (VCFFilterPane) filtersPane.getChildren().get(i);
            enabled[i] = pane.getFilter().isEnabled();
            pane.getFilter().setEnabled(false);
        }
        // Load all variants
        filter();
        // Apply LFS.
        table.getItems().parallelStream().forEach(LFS::addLFS);
        // Export to file
        exportTo(vcfFile);
        // Restore filters
        for (int i = 0; i < enabled.length; i++) {
            VCFFilterPane pane = (VCFFilterPane) filtersPane.getChildren().get(i);
            pane.getFilter().setEnabled(enabled[i]);
        }
        filter();
        lfs.setVisible(false);
        infos.add("LFS");
        vcfHeader.getInfos().add(vcfHeader.parseInfo(lfsInfo));
    }

}
