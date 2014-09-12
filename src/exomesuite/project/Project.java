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
package exomesuite.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Project {

    private final static String[] ENCODING_VALUES = {"phred+33", "phred+64"};
    private final static String[] REFERENCE_GENOMES = {"grch37", "grch38"};

    public enum PropertyName {

        /**
         * Name of the project.
         */
        NAME,
        /**
         * Code of the project. This will be used inside files such as bam or vcf.
         */
        CODE,
        /**
         * Some nice description.
         */
        DESCRIPTION,
        /**
         * A folder where everything is created.
         */
        PATH,
        /**
         * i don't know why this is needed.
         */
        STATUS,
        /**
         * Reference genome. OK, now only hg19/hgrc37.
         */
        REFERENCE_GENOME,
        /**
         * The FASTQ file 1.
         */
        FORWARD_FASTQ,
        /**
         * The FASTQ file 2.
         */
        REVERSE_FASTQ,
        /**
         * Encoding used (phred33, phred64).
         */
        FASTQ_ENCODING,
        /**
         * Illumina, SOLiD. Jus joking. We only use Illumina.
         */
        SEQUENCING_PLATFORM,
        /**
         * The alignments file.
         */
        BAM_FILE
    }

    /**
     * The properties in memory.
     */
    private final Properties properties;
    /**
     * The properties is disk.
     */
    private File file;

    /**
     * Creates a new ProjectData with the name and the code. If an existing project with the same
     * code is present, project data will be loaded from disk and name will NOT be changed. If
     * project do not exists, a folder called parent/code will be created.
     *
     * @param name the name of the project
     * @param code the code of the project
     * @param parent the parent folder
     */
    public Project(String name, String code, String parent) {
        properties = new Properties();
        file = new File(code + ".config");
        if (file.exists()) {
            // If file code.conf exists, data is loaded and no changes are done.
            loadFromDisk();
        } else {
            // If file do no exist, path is created (parent/code)
            // and PATH, NAME and CODE are updated.
            File path = new File(parent, code);
            path.mkdirs();
            properties.setProperty(PropertyName.PATH.toString(), path.getAbsolutePath());
            properties.setProperty(PropertyName.NAME.toString(), name);
            properties.setProperty(PropertyName.CODE.toString(), code);
            saveToDisk();
        }
    }

    /**
     * Creates a new Project using data from file. file must exist or Project will not be loaded.
     *
     * @param file
     * @throws IllegalArgumentException if file does not contain a Project properties. At least
     * NAME, CODE and PATH.
     */
    public Project(File file) throws IllegalArgumentException {
        this.file = file;
        properties = new Properties();
        loadFromDisk();
        if (!properties.containsKey(PropertyName.CODE.toString())) {
            throw new IllegalArgumentException("File " + file.getAbsolutePath()
                    + " does not contain CODE");
        }
        if (!properties.containsKey(PropertyName.NAME.toString())) {
            throw new IllegalArgumentException("File " + file.getAbsolutePath()
                    + " does not contain NAME");
        }
        if (!properties.containsKey(PropertyName.PATH.toString())) {
            throw new IllegalArgumentException("File " + file.getName() + " does not contain PATH");
        }
    }

    /**
     * Sets the given properties. Be careful, some properties must not be changed, such us reference
     * genome.
     *
     * @param name the name of the property
     * @param value the new value of the property
     */
    public void setProperty(PropertyName name, String value) {
        System.out.println("Set " + name + " to " + value);
        if (name == PropertyName.CODE && !value.isEmpty()) {
            file.delete();
            file = new File(file.getParent(), value + ".config");
        }
        properties.setProperty(name.toString(), value);
        saveToDisk();
    }

    public String getProperty(PropertyName name) {
        return properties.getProperty(name.toString());
    }

    public String getProperty(PropertyName key, String defaultValue) {
        return properties.getProperty(key.toString(), defaultValue);
    }

    public boolean contains(PropertyName name) {
        return properties.containsKey(name.toString());
    }

    /**
     * Stores current configuration to disk into the file "code.conf".
     */
    private void saveToDisk() {
        try {
            properties.store(new FileOutputStream(file), null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Loads properties from the file in disk. File existence must be checked before calling this
     * method.
     */
    private void loadFromDisk() {
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException ex) {
            Logger.getLogger(Project.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    public static List<String> encondingValues() {
        return Arrays.asList(ENCODING_VALUES);
    }

    public static List<String> referenceGenomes() {
        return Arrays.asList(REFERENCE_GENOMES);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != Project.class) {
            return false;
        }
        Project pd = (Project) obj;
        return this.getProperty(PropertyName.CODE).equals(pd.getProperty(PropertyName.CODE));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.file);
        return hash;
    }

}
