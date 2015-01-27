/*
 * Copyright (C) 2015 UICHUIMI
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
package exomesuite.project;

import exomesuite.utils.Configuration;
import exomesuite.utils.OS;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class ModelProject {

    /**
     * Name of the project.
     */
    public static final String NAME = "name";
    /**
     * Code of the project. This will be used inside files such as bam or vcf.
     */
    public static final String CODE = "code";
    /**
     * Some nice description.
     */
    public static final String DESCRIPTION = "description";
    /**
     * A folder where everything is created.
     */
    public static final String PATH = "PATH";
    /**
     * Reference genome. OK, now only hg19/hgrc37.
     */
    public static final String GENOME = "reference.genome";
    /**
     * The FASTQ file 1.
     */
    public static final String FORWARD = "sequences.forward";
    /**
     * The FASTQ file 2.
     */
    public static final String REVERSE = "sequences.reverse";
    /**
     * Encoding used (phred33, phred64).
     */
    public static final String ENCODING = "fastq.encoding";
    /**
     * Illumina, SOLiD. Jus joking. We only use Illumina.
     */
    //public static final String SEQUENCING_PLATFORM = "SEQUENCING_PLATFORM";
    /**
     * Other files list
     */
    public static final String EXTRA_FILES = "extra.files";
    /**
     * The properties in memory.
     */
    private final Configuration properties;
    /**
     * Name property.
     */
    private final StringProperty name;

    /**
     * Code property.
     */
    private final StringProperty code;

    /**
     * Description of the project.
     */
    private final StringProperty description;

    /**
     * Forward sequences.
     */
    private final Property<File> forwardSequences;

    /**
     * Reverse sequences.
     */
    private final Property<File> reverseSequences;

    /**
     * Reference genome code.
     */
    private final StringProperty genomeCode;

    /**
     * Reference genome code.
     */
    private final StringProperty encoding;

    /**
     * Associated files. Files that belong to this project.
     */
    private final ObservableList<File> files = FXCollections.observableArrayList();

    /**
     * File where info is sotred.
     */
    private final File configFile;

    /**
     * Creates a new project. Creates path and path/code.config.
     *
     * @param configFile wheter is or should be the config file
     */
    public ModelProject(File configFile) {
        this.configFile = configFile;
        // Create or load the file
        properties = new Configuration(configFile);
        // If confiFile exists, these properties will be loaded,
        // otherwise, all of them are set to null
        name = new SimpleStringProperty(properties.getProperty(NAME));
        this.code = new SimpleStringProperty(properties.getProperty(CODE));
        encoding = new SimpleStringProperty(properties.getProperty(ENCODING));
        genomeCode = new SimpleStringProperty(properties.getProperty(GENOME));
        description = new SimpleStringProperty(properties.getProperty(DESCRIPTION));
        // as new File(String name) cannot have null as parameter, we must check before
        final String fw = properties.getProperty(FORWARD);
        forwardSequences = new SimpleObjectProperty(fw == null ? null : new File(fw));
        final String rv = properties.getProperty(REVERSE);
        reverseSequences = new SimpleObjectProperty(rv == null ? null : new File(rv));
        if (properties.containsProperty(EXTRA_FILES)) {
            String[] values = properties.getProperty(EXTRA_FILES).split(";");
            Arrays.stream(values).forEach(filename -> files.add(new File(filename)));
        }
        // Make all of the properties backable to properties
        setBackupListeners();
    }

    /**
     * Project's name.
     *
     * @return the name given to the project.
     */
    public String getName() {
        return name.get();
    }

    /**
     * Project's name.
     *
     * @param name a new name to the project
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Project's name's property. Use it to listen for changes.
     *
     * @return the project's name's property
     */
    public StringProperty getNameProperty() {
        return name;
    }

    /**
     * Project's description.
     *
     * @return the description of the project
     */
    public String getDescription() {
        return description.get();
    }

    /**
     * Project's description.
     *
     * @param description a new description to the project
     */
    public void setDescrition(String description) {
        this.description.set(description);
    }

    /**
     * Project's description's property. Use it to listen for changes.
     *
     * @return the project's description's property
     */
    public StringProperty getDescriptionProperty() {
        return description;
    }

    /**
     * Code of the project.
     *
     * @return the current code's property
     */
    public StringProperty getCodeProperty() {
        return code;
    }

    /**
     * Code of the project.
     *
     * @return the current code
     */
    public String getCode() {
        return code.get();
    }

    /**
     * Code of the project. The code is used for file names and as sample identifier. You must keep
     * it as simple as possible: avoid spaces, accents and region characters.
     *
     * @param code new code value
     */
    public void setCode(String code) {
        this.code.set(code);
    }

    /**
     * File containing the forward sequences.
     *
     * @return a file that contains the forward sequences or null
     */
    public File getForwardSequences() {
        return forwardSequences.getValue();
    }

    /**
     * File containing the forward sequences.
     *
     * @return forward's file's property
     */
    public Property<File> getForwardSequencesProperty() {
        return forwardSequences;
    }

    /**
     * File containing the forward sequences.
     *
     * @param file new value for this file
     */
    public void setForwardSequences(File file) {
        forwardSequences.setValue(file);
    }

    /**
     * File containing the reverse sequences.
     *
     * @return a file that contains the reverse sequences or null
     */
    public File getReverseSequences() {
        return reverseSequences.getValue();
    }

    /**
     * File containing the reverse sequences.
     *
     * @return reverse's file's property
     */
    public Property<File> getReverseSequencesProperty() {
        return reverseSequences;
    }

    /**
     * File containing the reverse sequences.
     *
     * @param file new value for this file
     */
    public void setReverseSequences(File file) {
        reverseSequences.setValue(file);
    }

    /**
     * Genome code. GRCh37 or GRCh38.
     *
     * @return the genome code for the project.
     */
    public String getGenomeCode() {
        return genomeCode.get();
    }

    /**
     * Genome code. GRCh37 or GRCh38.
     *
     * @param code a new genome code
     */
    public void setGenomeCode(String code) {
        genomeCode.set(code);
    }

    /**
     * Genome code. GRCh37 or GRCh38.
     *
     * @return the project's genome code's property
     */
    public StringProperty getGenomeCodeProperty() {
        return genomeCode;
    }

    /**
     * Encoding. phred+33 or phred+64.
     *
     * @return the encoding for the project.
     */
    public String getEncoding() {
        return encoding.get();
    }

    /**
     * Encoding. phred+33 or phred+64.
     *
     * @param encoding a new encoding
     */
    public void setEncoding(String encoding) {
        this.encoding.set(encoding);
    }

    /**
     * Encoding. phred+33 or phred+64.
     *
     * @return the project's encoding's property
     */
    public StringProperty getEncodingProperty() {
        return encoding;
    }

    public File getConfigFile() {
        return configFile;
    }

    public ObservableList<File> getFiles() {
        return files;
    }

    public Configuration getProperties() {
        return properties;
    }

    /**
     * Makes each property backable. Any time a value is changed, the property is inmediatly changed
     * in properties, so in backup file.
     */
    private void setBackupListeners() {
        name.addListener((observable, old, newValue)
                -> properties.setProperty(NAME, newValue));
        description.addListener((observable, old, newValue)
                -> properties.setProperty(DESCRIPTION, newValue));
        this.code.addListener((obs, old, newValue)
                -> properties.setProperty(CODE, newValue));
        forwardSequences.addListener((obs, old, newValue)
                -> properties.setProperty(FORWARD, newValue.getAbsolutePath()));
        reverseSequences.addListener((obs, old, newValue)
                -> properties.setProperty(REVERSE, newValue.getAbsolutePath()));
        genomeCode.addListener((obs, old, newValue)
                -> properties.setProperty(GENOME, newValue));
        encoding.addListener((obs, old, newValue)
                -> properties.setProperty(ENCODING, newValue));
        files.addListener((ListChangeListener.Change<? extends File> change) -> {
            if (files.isEmpty()) {
                properties.removeProperty(EXTRA_FILES);
            } else {
                List<String> names = new ArrayList();
                files.forEach(file -> names.add(file.getAbsolutePath()));
                properties.setProperty(EXTRA_FILES, OS.asString(";", names));
            }

        });
    }
}
