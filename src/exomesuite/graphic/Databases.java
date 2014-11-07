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

import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class. Manages the databases windows. These databases are: the reference genomes,
 * the Ensembl exons and some important VCFs with 1000G or other sites variants.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class Databases extends VBox {

    @FXML
    private Parameter mills;
    @FXML
    private Parameter omni;
    @FXML
    private Parameter phase1;
    @FXML
    private Parameter dbsnp;
    @FXML
    private Parameter hapmap;
    @FXML
    private Parameter ensembl;
    @FXML
    private Parameter grch37;
    @FXML
    private Parameter grch38;

    private final static String MILLS = "mills";
    private final static String DBSNP = "dbsnp";
    private final static String OMNI = "omni";
    private final static String HAPMAP = "hapmap";
    private final static String PHASE1 = "phase1";
    private final static String ENSMEBL = "ensembl";
    private final static String GRCH37 = "GRCh37";
    private final static String GRCH38 = "GRCh38";

    public Databases() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Databases.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            getStylesheets().add("/exomesuite/main.css");
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(Databases.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        // MILLS
        if (OS.containsKey(MILLS)) {
            mills.setValue(OS.getProperty(MILLS));
        }
        mills.addExtensionFilter(FileManager.VCF_FILTER);
        mills.setOnValueChanged(e -> OS.setProperty(MILLS, mills.getValue()));
        // OMNI
        if (OS.containsKey(OMNI)) {
            omni.setValue(OS.getProperty(OMNI));
        }
        omni.addExtensionFilter(FileManager.VCF_FILTER);
        omni.setOnValueChanged(e -> OS.setProperty(OMNI, omni.getValue()));
        // dbSNP
        if (OS.containsKey(DBSNP)) {
            dbsnp.setValue(OS.getProperty(DBSNP));
        }
        dbsnp.addExtensionFilter(FileManager.VCF_FILTER);
        dbsnp.setOnValueChanged(e -> OS.setProperty(DBSNP, dbsnp.getValue()));
        // Hapmap
        if (OS.containsKey(HAPMAP)) {
            hapmap.setValue(OS.getProperty(HAPMAP));
        }
        hapmap.addExtensionFilter(FileManager.VCF_FILTER);
        hapmap.setOnValueChanged(e -> OS.setProperty(HAPMAP, hapmap.getValue()));
        // Phase 1
        if (OS.containsKey(PHASE1)) {
            phase1.setValue(OS.getProperty(PHASE1));
        }
        phase1.addExtensionFilter(FileManager.VCF_FILTER);
        phase1.setOnValueChanged(e -> OS.setProperty(PHASE1, phase1.getValue()));
        // Ensembl
        if (OS.containsKey(ENSMEBL)) {
            ensembl.setValue(OS.getProperty(ENSMEBL));
        }
        ensembl.addExtensionFilter(FileManager.TSV_FILTER);
        ensembl.addExtensionFilter(FileManager.ALL_FILTER);
        ensembl.setOnValueChanged(e -> OS.setProperty(ENSMEBL, ensembl.getValue()));
        // Human genome GRCHv37
        if (OS.containsKey(GRCH37)) {
            grch37.setValue(OS.getProperty(GRCH37));
        }
        grch37.addExtensionFilter(FileManager.FASTA_FILTER);
        grch37.setOnValueChanged(e -> OS.setProperty(GRCH37, grch37.getValue()));
        // Human genome GRCH38
        if (OS.containsKey(GRCH38)) {
            grch38.setValue(OS.getProperty(GRCH38));
        }
        grch38.addExtensionFilter(FileManager.FASTA_FILTER);
        grch38.setOnValueChanged(e -> OS.setProperty(GRCH38, grch38.getValue()));
    }

}
