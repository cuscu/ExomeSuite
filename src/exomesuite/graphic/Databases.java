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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class Databases extends VBox {

    @FXML
    private FileSelector mills;
    @FXML
    private FileSelector omni;
    @FXML
    private FileSelector phase1;
    @FXML
    private FileSelector dbsnp;
    @FXML
    private FileSelector hapmap;
    @FXML
    private FileSelector grch37;
    @FXML
    private FileSelector grch38;

    private final static String MILLS = "mills";
    private final static String DBSNP = "dbsnp";
    private final static String OMNI = "omni";
    private final static String HAPMAP = "hapmap";
    private final static String PHASE1 = "phase1";
    private final static String GRCH37 = "grch37";
    private final static String GRCH38 = "grch38";

    public Databases() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Databases.fxml"));
            loader.setRoot(this);
            loader.setController(this);
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
            mills.setFile(OS.getProperty(MILLS));
        }
        mills.addExtensionFilter(OS.VCF_FILTER);
        mills.setOnFileChange((EventHandler) (Event event)
                -> OS.setProperty(MILLS, mills.getFile()));
        // OMNI
        if (OS.containsKey(OMNI)) {
            omni.setFile(OS.getProperty(OMNI));
        }
        omni.addExtensionFilter(OS.VCF_FILTER);
        omni.setOnFileChange((EventHandler) (Event event)
                -> OS.setProperty(OMNI, omni.getFile()));
        // dbSNP
        if (OS.containsKey(DBSNP)) {
            dbsnp.setFile(OS.getProperty(DBSNP));
        }
        dbsnp.addExtensionFilter(OS.VCF_FILTER);
        dbsnp.setOnFileChange((EventHandler) (Event event)
                -> OS.setProperty(DBSNP, dbsnp.getFile()));
        // Hapmap
        if (OS.containsKey(HAPMAP)) {
            hapmap.setFile(OS.getProperty(HAPMAP));
        }
        hapmap.addExtensionFilter(OS.VCF_FILTER);
        hapmap.setOnFileChange((EventHandler) (Event event)
                -> OS.setProperty(HAPMAP, hapmap.getFile()));
        // Phase 1
        if (OS.containsKey(PHASE1)) {
            phase1.setFile(OS.getProperty(PHASE1));
        }
        phase1.addExtensionFilter(OS.VCF_FILTER);
        phase1.setOnFileChange((EventHandler) (Event event)
                -> OS.setProperty(PHASE1, phase1.getFile()));
        // Human genome GRCHv37
        if (OS.containsKey(GRCH37)) {
            grch37.setFile(OS.getProperty(GRCH37));
        }
        grch37.addExtensionFilter(OS.FASTA_FILTER);
        grch37.setOnFileChange((EventHandler) (Event event)
                -> OS.setProperty("grch37", grch37.getFile()));
        // Human genome GRCH38
        if (OS.containsKey(GRCH38)) {
            grch38.setFile(OS.getProperty(GRCH38));
        }
        grch38.addExtensionFilter(OS.FASTA_FILTER);
        grch38.setOnFileChange((EventHandler) (Event event)
                -> OS.setProperty("grch38", grch38.getFile()));
    }

}
