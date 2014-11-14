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

import exomesuite.graphic.Param;
import exomesuite.graphic.VariantExons;
import exomesuite.graphic.VariantInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
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
    private HBox formatBox;
    @FXML
    private VariantExons variantExons;
    @FXML
    private VariantInfo variantInfo;

    private File vcfFile;

    private final TableColumn<Variant2, String> lineNumber = new TableColumn<>();
    private final TableColumn<Variant2, String> chrom = new TableColumn<>("Chrom");
    private final TableColumn<Variant2, String> position = new TableColumn<>("Position");
    private final TableColumn<Variant2, String> variant = new TableColumn<>("Variant");
    private final TableColumn<Variant2, String> rsId = new TableColumn<>("ID");
    private final TableColumn<Variant2, String> qual = new TableColumn<>("Qual");
    private final TableColumn<Variant2, String> filter = new TableColumn<>("Filter");
//    private final TableColumn<Variant2, String> info = new TableColumn<>("Info");
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
    }

    public void setFile(File vcfFile) {
        this.vcfFile = vcfFile;
        table.getItems().clear();
        try (BufferedReader in = new BufferedReader(new FileReader(vcfFile))) {
            in.lines().forEachOrdered(t -> {
                if (!t.startsWith("#")) {
                    table.getItems().add(toVariant(t));
                }
            });
        } catch (Exception ex) {
            Logger.getLogger(VCFTable.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        // Second column (INFO)
//        infoBox.getChildren().clear();
//        String[] infos = v.getInfo().split(";");
//        for (String inf : infos) {
//            Param p = new Param();
//            String[] pair = inf.split("=");
//            p.setTitle(pair[0]);
//            if (pair.length > 1) {
//                p.setValue(pair[1]);
//            }
//            infoBox.getChildren().add(p);
//        }
        //Third column (FORMAT)
        if (v.getFormat() != null) {
            formatBox.getChildren().clear();
            String[] formats = v.getFormat().split(":");
            String[] values = v.getSamples()[0].split(":");
            for (int i = 0; i < formats.length; i++) {
                Param p = new Param();
                p.setTitle(formats[i]);
                p.setValue(values[i]);
                formatBox.getChildren().add(p);
            }
        }
        // Call listeners
        listeners.forEach(t -> t.variantChanged(v));
    }

    /**
     * The first column shows line index
     */
    private static class IndexCell extends TableCell<Variant2, String> {

        private final static String PASS = "pass";
        private final static String NO_PASS = "no-pass";

        public IndexCell() {
            getStyleClass().add("index-cell");
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
            } else {
                String style;
                getStyleClass().remove(PASS);
                getStyleClass().remove(NO_PASS);
                if (getTableRow() == null) {
                    return;
                }
                Variant2 v = (Variant2) getTableRow().getItem();
                if (v == null) {
                    return;
                }
                if (v.getQual() < 20) {
                    style = NO_PASS;
                } else {
                    style = PASS;
                }
                getStyleClass().add(style);
                setAlignment(Pos.CENTER_RIGHT);
                setText((1 + getIndex()) + "");
            }
        }

    }

    public void addListener(VariantListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(VariantListener listener) {
        this.listeners.remove(listener);
    }

    private static class StyledCell implements
            Callback<TableColumn<Variant2, String>, TableCell<Variant2, String>> {

        public StyledCell() {
        }

        @Override
        public TableCell<Variant2, String> call(TableColumn<Variant2, String> param) {
            return new TableCell<Variant2, String>() {

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                }

            };
        }
    }

}
