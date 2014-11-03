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
package exomesuite.graphic;

import exomesuite.utils.OS;
import exomesuite.vcfreader.Variant2;
import exomesuite.vcfreader.VariantListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import org.controlsfx.dialog.Dialogs;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class VariantInfo extends VBox implements VariantListener {

    @FXML
    private TableView<String> table;

    public VariantInfo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("VariantInfo.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(VariantInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        TableColumn<String, String> column = new TableColumn<>("Exon");
        column.setCellValueFactory((TableColumn.CellDataFeatures<String, String> param)
                -> new SimpleStringProperty(param.getValue()));
        table.getColumns().add(column);
    }

    @Override
    public void variantChanged(Variant2 variant) {
        System.out.println("Hi");
        final String chr = variant.getChrom();
        final int pos = variant.getPos();
        File exons = new File(OS.getProperty("ensembl"));
        table.getItems().clear();
        try (BufferedReader br = new BufferedReader(new FileReader(exons))) {
            br.readLine();
            br.lines().forEachOrdered(line -> {
                String[] row = line.split("\t");
                String exonChr = row[0];
                int exonStart = Integer.valueOf(row[1]);
                int exonEnd = Integer.valueOf(row[2]);
                if (exonChr.equals(chr) && exonStart <= pos && pos <= exonEnd) {
                    table.getItems().add(line);
                }
            });
        } catch (Exception e) {
            Dialogs.create().showException(e);
        }
    }

}
