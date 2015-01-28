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
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

/**
 * FXML Controller class. Manages the databases windows. These databases are: the reference genomes,
 * the Ensembl exons and some important VCFs with 1000G or other sites variants.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class Databases {

    @FXML
    private TextField mills;
    @FXML
    private TextField omni;
    @FXML
    private TextField phase1;
    @FXML
    private TextField dbsnp;
    @FXML
    private TextField hapmap;
    @FXML
    private TextField ensembl;
    @FXML
    private TextField grch37;
    @FXML
    private TextField grch38;

    private final static String MILLS = "mills";
    private final static String DBSNP = "dbsnp";
    private final static String OMNI = "omni";
    private final static String HAPMAP = "hapmap";
    private final static String PHASE1 = "phase1";
    private final static String ENSMEBL = "ensembl";
    private final static String GRCH37 = "GRCh37";
    private final static String GRCH38 = "GRCh38";

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        mills.setText(OS.getProperties().getProperty(MILLS, ""));
        omni.setText(OS.getProperties().getProperty(OMNI, ""));
        dbsnp.setText(OS.getProperties().getProperty(DBSNP, ""));
        hapmap.setText(OS.getProperties().getProperty(HAPMAP, ""));
        phase1.setText(OS.getProperties().getProperty(PHASE1, ""));
        ensembl.setText(OS.getProperties().getProperty(ENSMEBL, ""));
        grch37.setText(OS.getProperties().getProperty(GRCH37, ""));
        grch38.setText(OS.getProperties().getProperty(GRCH38, ""));
    }

    @FXML
    private void selectMills() {
        selectDatabase(mills, MILLS, "VCF", FileManager.VCF_FILTER);
    }

    @FXML
    private void selectOmni() {
        selectDatabase(omni, OMNI, "VCF", FileManager.VCF_FILTER);
    }

    @FXML
    private void selectPhase1() {
        selectDatabase(phase1, PHASE1, "VCF", FileManager.VCF_FILTER);
    }

    @FXML
    private void selectDbsnp() {
        selectDatabase(dbsnp, DBSNP, "VCF", FileManager.VCF_FILTER);
    }

    @FXML
    private void selectHapmap() {
        selectDatabase(hapmap, HAPMAP, "VCF", FileManager.VCF_FILTER);
    }

    @FXML
    private void selectGrch37() {
        selectDatabase(grch37, GRCH37, "FASTA", FileManager.FASTA_FILTER);
    }

    @FXML
    private void selectGrch38() {
        selectDatabase(grch38, GRCH38, "FASTA", FileManager.FASTA_FILTER);
    }

    @FXML
    private void selectEnsembl() {
        selectDatabase(ensembl, ENSMEBL, "exons", FileManager.TSV_FILTER, FileManager.ALL_FILTER);
    }

    /**
     * Opens a database. If user selects a valid database, textfield and OS properties are updated.
     *
     * @param textField textField that shows file
     * @param key key in OS properties
     * @param type a string to show to the user: select [type] file
     * @param filters list of filters of the file
     */
    private void selectDatabase(TextField textField, String key, String type, FileChooser.ExtensionFilter... filters) {
        String message = ExomeSuite.getStringFormatted("select.file", type);
        File f;
        if (textField.getText().isEmpty()) {
            f = FileManager.openFile(textField, message, filters);
        } else {
            File parent = new File(textField.getText()).getParentFile();
            f = FileManager.openFile(textField, message, parent, filters);
        }
        if (f != null) {
            OS.getProperties().setProperty(key, f.getAbsolutePath());
        }
    }

}
