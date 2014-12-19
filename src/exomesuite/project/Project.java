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

import exomesuite.utils.Configuration;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains the properties of a project.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public final class Project {

    /**
     * Name of the project.
     */
    public static final String NAME = "NAME";
    /**
     * Code of the project. This will be used inside files such as bam or vcf.
     */
    public static final String CODE = "CODE";
    /**
     * Some nice description.
     */
    public static final String DESCRIPTION = "DESCRIPTION";
    /**
     * A folder where everything is created.
     */
    public static final String PATH = "PATH";
    /**
     * Reference genome. OK, now only hg19/hgrc37.
     */
    public static final String REFERENCE_GENOME = "REFERENCE_GENOME";
    /**
     * The FASTQ file 1.
     */
    public static final String FORWARD_FASTQ = "FORWARD_FASTQ";
    /**
     * The FASTQ file 2.
     */
    public static final String REVERSE_FASTQ = "REVERSE_FASTQ";
    /**
     * Encoding used (phred33, phred64).
     */
    public static final String FASTQ_ENCODING = "FASTQ_ENCODING";
    /**
     * Illumina, SOLiD. Jus joking. We only use Illumina.
     */
    public static final String SEQUENCING_PLATFORM = "SEQUENCING_PLATFORM";
    /**
     * Other files list
     */
    public static final String FILES = "FILES";
    /**
     * The properties in memory.
     */
    private final Configuration properties;

    /**
     * Creates a new ProjectData with the name and the code. If an existing project with the same
     * code is present, project data will be loaded from disk and name will NOT be changed. If
     * project do not exists, a folder called parent/code will be created.
     *
     * @param name the name of the project
     * @param code the code of the project
     * @param parent the parent folder
     */
    public Project(String name, String code, File parent) {
        File path = new File(parent, code);
        File file = new File(path, code + ".config");
        if (file.exists()) {
            properties = new Configuration(file);
            // If file code.conf exists, data is loaded and no changes are done.
        } else {
            // If file does no exist, path is created (parent/code)
            // and PATH, NAME and CODE are updated.
            properties = new Configuration(file);
            properties.setProperty(PATH, path.getAbsolutePath());
            properties.setProperty(NAME, name);
            properties.setProperty(CODE, code);
        }
    }

    /**
     * Creates a new Project using data from file. file must exist or Project will not be loaded.
     *
     * @param file the project config file
     * @throws IllegalArgumentException if file does not contain a Project properties. At least
     * NAME, CODE and PATH.
     */
    public Project(File file) throws IllegalArgumentException {
        properties = new Configuration(file);
        if (!properties.containsProperty(CODE)) {
            throw new IllegalArgumentException("File " + file.getAbsolutePath()
                    + " does not contain CODE");
        }
        if (!properties.containsProperty(NAME)) {
            throw new IllegalArgumentException("File " + file.getAbsolutePath()
                    + " does not contain NAME");
        }
        String path = properties.getProperty(PATH);
        if (!file.getParentFile().equals(new File(path))) {

            properties.setProperty(PATH, file.getParent());
        }
    }

    /**
     * Get the properties of the project.
     *
     * @return the properties of the project
     */
    public Configuration getProperties() {
        return properties;
    }

    /**
     * Adds an extra file to the project. File is added only if it exists and is not already in the
     * list.
     *
     * @param file the file to add
     */
    public void addExtraFile(String file) {
        // Valid file?
        if (!FileManager.tripleCheck(file)) {
            return;
        }
        // Get the current list of files
        List<String> fs = new ArrayList<>(Arrays.asList(properties.getProperty(FILES, "").split(";")));
        // Is the file already in the list?
        if (fs.contains(file)) {
            return;
        }
        fs.add(file);
        properties.setProperty(FILES, OS.asString(";", fs));
    }

    /**
     * Remove a file from the "files" property.
     *
     * @param file the file to remove
     */
    public void removeExtraFile(String file) {
        List<String> files = new ArrayList(Arrays.asList(properties.getProperty(FILES).split(";")));
        if (files.remove(file)) {
            String newFiles = OS.asString(";", files);
            properties.setProperty(FILES, newFiles);
        }
    }

    @Override
    public String toString() {
        return properties.getProperty(NAME);

    }

}
