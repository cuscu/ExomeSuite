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
package exomesuite.utils;

import exomesuite.Project;
import exomesuite.phase.reference.GenomeManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Config {

    /* *************************************
     * General databases.
     * *************************************/
    /**
     * Current reference genome.
     */
    public static final String GENOME = "genome";
    /**
     * Date of index of the reference genome.
     */
    public static final String INDEX_DATE = "index_date";
    /**
     * Database containing variants from OMNI.
     */
    public static final String OMNI = "omni";
    /**
     * Database of SNPs.
     */
    public static final String DBSNP = "dbsnp";
    /**
     * 1000 genomes phase1 variants.
     */
    public static final String PHASE1 = "phase1";
    /**
     * Database with Mills and 1000 Genome indels.
     */
    public static final String MILLS = "mills";

    /* **************************************
     * Settings for a single project.
     ************************************** */
    /**
     * First file of sequences.
     */
    public static final String FORWARD = "forward_sequence";
    /**
     * Second file of sequences.
     */
    public static final String REVERSE = "reverse_sequence";
    /**
     * Date of success for a call.
     */
    public static final String CALL_DATE = "call_date";
    /**
     * Date of success for the alignment.
     */
    public static final String ALIGN_DATE = "align_date";
    /**
     * Date of success for mist analysis.
     */
    public static String MIST_DATE = "mist_date";
    /**
     * Exons from Ensembl.
     */
    public static String ENSEMBL_EXONS = "ensembl_exons";

    /**
     * The map of properties.
     */
    private Properties properties;
    /**
     * The file where the properties are stored.
     */
    private final File file;
    /**
     * A list of phases to call when some settings are changed.
     */
    private final List<Phase> phases;

    public Config(File file) {
        this.properties = new Properties();
        this.file = file;
        phases = new ArrayList<>();
        properties = new Properties();
        try {
            if (file.exists()) {
                properties.load(new FileInputStream(file));
            } else {
                file.createNewFile();
            }
        } catch (IOException ex) {
            Logger.getLogger(GenomeManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Removes the key and the value from the config, if they exist. Otherwise, do nothing.
     *
     * @param key
     */
    public void removeProperty(String key) {
        try {
            if (properties.remove(key) != null) {
                properties.store(new FileOutputStream(file), null);
                callListeners();
            }
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Returns the value for this key in the config, or null if the key is not present.
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Puts or updates an entry in the config, using the pair key=value.
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        try {
            properties.setProperty(key, value);
            properties.store(new FileOutputStream(file), null);
            callListeners();
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns true if they key is set in the config. Note that a key with value "" is a valid key.
     *
     * @param key
     * @return true if key exists.
     */
    public boolean containsKey(String key) {
        return properties.containsKey(key);
    }

    /**
     * Calls all listeners when something changes.
     */
    private void callListeners() {
        phases.forEach(Phase::configChanged);
    }

    /**
     * Adds a phase to listen for changes in config. Phases are called when a property is added or
     * removed.
     *
     * @param phase
     */
    public void addListener(Phase phase) {
        phases.add(phase);
    }

    /**
     * Remove a phase from listeners.
     *
     * @param phase
     * @return
     */
    public boolean removeListener(Phase phase) {
        return phases.remove(phase);
    }

}
