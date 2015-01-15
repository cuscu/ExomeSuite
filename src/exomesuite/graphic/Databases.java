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

import exomesuite.ExomeSuite;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
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
    private FileParam mills;
    @FXML
    private FileParam omni;
    @FXML
    private FileParam phase1;
    @FXML
    private FileParam dbsnp;
    @FXML
    private FileParam hapmap;
    @FXML
    private FileParam ensembl;
    @FXML
    private FileParam grch37;
    @FXML
    private FileParam grch38;

    private final static String MILLS = "mills";
    private final static String DBSNP = "dbsnp";
    private final static String OMNI = "omni";
    private final static String HAPMAP = "hapmap";
    private final static String PHASE1 = "phase1";
    private final static String ENSMEBL = "ensembl";
    private final static String GRCH37 = "GRCh37";
    private final static String GRCH38 = "GRCh38";

    /**
     * Creates and shows the panel for selecting the databases.
     */
    public Databases() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Databases.fxml"), ExomeSuite.getResources());
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
        if (OS.getProperties().containsProperty(MILLS)) {
            mills.setValue(new File(OS.getProperties().getProperty(MILLS)));
        }
        mills.addFilter(FileManager.VCF_FILTER);
        mills.setOnValueChanged(e -> OS.getProperties().setProperty(MILLS, mills.getValue().getAbsolutePath()));
        // OMNI
        if (OS.getProperties().containsProperty(OMNI)) {
            omni.setValue(new File(OS.getProperties().getProperty(OMNI)));
        }
        omni.addFilter(FileManager.VCF_FILTER);
        omni.setOnValueChanged(e -> OS.getProperties().setProperty(OMNI, omni.getValue().getAbsolutePath()));
        // dbSNP
        if (OS.getProperties().containsProperty(DBSNP)) {
            dbsnp.setValue(new File(OS.getProperties().getProperty(DBSNP)));
        }
        dbsnp.addFilter(FileManager.VCF_FILTER);
        dbsnp.setOnValueChanged(e -> OS.getProperties().setProperty(DBSNP, dbsnp.getValue().getAbsolutePath()));
        // Hapmap
        if (OS.getProperties().containsProperty(HAPMAP)) {
            hapmap.setValue(new File(OS.getProperties().getProperty(HAPMAP)));
        }
        hapmap.addFilter(FileManager.VCF_FILTER);
        hapmap.setOnValueChanged(e -> OS.getProperties().setProperty(HAPMAP, hapmap.getValue().getAbsolutePath()));
        // Phase 1
        if (OS.getProperties().containsProperty(PHASE1)) {
            phase1.setValue(new File(OS.getProperties().getProperty(PHASE1)));
        }
        phase1.addFilter(FileManager.VCF_FILTER);
        phase1.setOnValueChanged(e -> OS.getProperties().setProperty(PHASE1, phase1.getValue().getAbsolutePath()));
        // Ensembl
        if (OS.getProperties().containsProperty(ENSMEBL)) {
            ensembl.setValue(new File(OS.getProperties().getProperty(ENSMEBL)));
        }
        ensembl.addFilter(FileManager.TSV_FILTER);
        ensembl.addFilter(FileManager.ALL_FILTER);
        ensembl.setOnValueChanged(e -> OS.getProperties().setProperty(ENSMEBL, ensembl.getValue().getAbsolutePath()));
        // Human genome GRCHv37
        if (OS.getProperties().containsProperty(GRCH37)) {
            grch37.setValue(new File(OS.getProperties().getProperty(GRCH37)));
        }
        grch37.addFilter(FileManager.FASTA_FILTER);
        grch37.setOnValueChanged(e -> OS.getProperties().setProperty(GRCH37, grch37.getValue().getAbsolutePath()));
        // Human genome GRCH38
        if (OS.getProperties().containsProperty(GRCH38)) {
            grch38.setValue(new File(OS.getProperties().getProperty(GRCH38)));
        }
        grch38.addFilter(FileManager.FASTA_FILTER);
        grch38.setOnValueChanged(e -> OS.getProperties().setProperty(GRCH38, grch38.getValue().getAbsolutePath()));
    }

}
