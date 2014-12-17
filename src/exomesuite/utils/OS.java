package exomesuite.utils;

import exomesuite.MainViewController;
import exomesuite.project.Project;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains methods to control application properties (databases, opened projects..) and most
 * general methods, like {@code humanReadableByteCount()} or {@code asString()}. If you want to
 * perform a general GUI operation, such as print a message, use {@link MainViewController}.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class OS {

    /**
     * The name where properties are stored in disk.
     */
    private static final String CONFIG_FILE_NAME = "exomesuite.properties";
    /**
     * Main application properties in disk.
     */
    private static final File propertiesFile = new File(FileManager.getUserPath(), CONFIG_FILE_NAME);
    /**
     * Main application properties in memory.
     */
    private static final Configuration properties = new Configuration(propertiesFile);

    /**
     * The list of supported reference genomes.
     */
    private static final List<String> referenceGenomes = new ArrayList();
    /**
     * The list of supported encondings.
     */
    private static final List<String> encodings = new ArrayList();
    /**
     * The list of ordered standard chromosomes (1-22, X and Y).
     */
    private static final List<String> standardChromosomes = new ArrayList();

    /**
     * Static "Constructor" of the class.
     */
    static {
        final String[] e = {"phred+64", "phred+33"};
        final String[] r = {"GRCh37", "GRCh38"};
        final String[] chrs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
            "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
        encodings.addAll(Arrays.asList(e));
        referenceGenomes.addAll(Arrays.asList(r));
        standardChromosomes.addAll(Arrays.asList(chrs));
    }

    /**
     * Takes a byte value and convert it to the corresponding human readable unit.
     *
     * @param bytes value in bytes
     * @param si if true, divides by 1000; else by 1024
     * @return a human readable size
     */
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] to value1,value2,value3
     *
     * @param separator something like "\t" or ","
     * @param values a list of values
     * @return the stringified list
     */
    public static String asString(String separator, String... values) {
        if (values.length == 0) {
            return "";
        }
        String s = values[0];
        int i = 1;
        while (i < values.length) {
            s += separator + values[i++];
        }
        return s;
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] to value1,value2,value3
     *
     * @param separator something like "\t" or ","
     * @param values a list of values
     * @return the stringified list
     */
    public static String asString(String separator, List<String> values) {
        if (values.isEmpty()) {
            return "";
        }
        String s = "";
        int i = 0;
        while (i < values.size() - 1) {
            s += values.get(i++) + separator;
        }
        return s + values.get(i);
    }

    /**
     * Gets the temporary path of the application. (that is userdir/temp).
     *
     * @return the temp path.
     */
    public static String getTempDir() {
        File temp = new File(FileManager.getUserPath(), "temp");
        temp.mkdirs();
        return temp.getAbsolutePath();
    }

    /**
     * Get the properties of the application.
     *
     * @return the properties of application.
     */
    public static Configuration getProperties() {
        return properties;
    }

    /**
     * Removes a project from the projects list.
     *
     * @param project the project to remove
     */
    public static void removeProject(Project project) {
        // Import projects
        List<String> files = new ArrayList<>(
                Arrays.asList(properties.getProperty("projects", "").split(";")));
        // Check if project is in the list by trying to remove it
        String path = project.getProperties().getProperty(Project.PATH);
        String code = project.getProperties().getProperty(Project.CODE);
        String configFile = path + File.separator + code + ".config";
        if (files.remove(configFile)) {
            properties.setProperty("projects", OS.asString(";", files));
        }
    }

    /**
     * Adds a project to the project list.
     *
     * @param project the project to add
     */
    public static void addProject(Project project) {
        // Import projects
        List<String> projects = new ArrayList<>(
                Arrays.asList(properties.getProperty("projects", "").split(";")));
        // If project is not yet in the list
        String path = project.getProperties().getProperty(Project.PATH);
        String code = project.getProperties().getProperty(Project.CODE);
        String configFile = path + File.separator + code + ".config";
        if (!projects.contains(configFile)) {
            // Add it
            projects.add(configFile);
            properties.setProperty("projects", OS.asString(";", projects));
        }
    }

    /**
     * Gets the supported reference genomes.
     *
     * @return the list of reference genomes.
     */
    public static List<String> getReferenceGenomes() {
        return referenceGenomes;
    }

    /**
     * Gets the list of supported encodings(phred+33 and phred+64).
     *
     * @return the supported encodings
     */
    public static List<String> getEncodings() {
        return encodings;
    }

    /**
     * Gets the list of standard chromosomes (1-22, X and Y).
     *
     * @return the list of chromosomes
     */
    public static List<String> getStandardChromosomes() {
        return standardChromosomes;
    }
}
