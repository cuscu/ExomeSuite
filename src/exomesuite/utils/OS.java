package exomesuite.utils;

import exomesuite.MainViewController;
import exomesuite.project.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains methods to control application properties (databases, opened projects..) and most
 * general methods, like {@code humanReadableByteCount()} or {@code asString()}. If you want to
 * perform a general GUI operation, such as print a message, use {@link MainViewController}.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class OS {

    private static Properties properties;
    private static File propertiesFile;

    private static List<String> referenceGenomes;
    private static List<String> encodings;
    private static List<String> standardChromosomes;

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

    public static boolean containsKey(String key) {
        return getProperties().containsKey(key.toLowerCase());
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] -> value1,value2,value3
     *
     * @param separator something like "\t" or ","
     * @param values a list of values
     * @return the stringified list
     */
    public static String asString(String separator, String[] values) {
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
     * value3] -> value1,value2,value3
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

    public static String getTempDir() {
        File temp = new File(FileManager.getUserPath(), "temp");
        temp.mkdirs();
        return temp.getAbsolutePath();
    }

    public static String getGenome(String property) {
        return getProperties().getProperty(property.toLowerCase());
    }

    private static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            propertiesFile = new File(FileManager.getUserPath(), "properties.txt");
            if (propertiesFile.exists()) {
                try {
                    properties.load(new FileInputStream(propertiesFile));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(OS.class.getName()).log(Level.SEVERE, "Check permissions", ex);
                } catch (IOException ex) {
                    Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return properties;
    }

    /**
     * Get the property value or null if it does not exist.
     *
     * @param key the key of the property
     * @return the property value if contained, null otherwise
     */
    public static String getProperty(String key) {
        return getProperties().getProperty(key.toLowerCase());
    }

    public static void setProperty(String key, String value) {
        getProperties().setProperty(key.toLowerCase(), value);
        try {
            properties.store(new FileOutputStream(propertiesFile), null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<String> getSupportedReferenceGenomes() {
        if (referenceGenomes == null) {
            referenceGenomes = new ArrayList<>();
            referenceGenomes.add("GRCh37");
            referenceGenomes.add("GRCh38");
        }
        return referenceGenomes;
    }

    public static List<String> getSupportedEncodings() {
        if (encodings == null) {
            encodings = new ArrayList<>();
            encodings.add("phred+64");
            encodings.add("phred+33");
        }
        return encodings;
    }

    public static List<String> getStandardChromosomes() {
        if (standardChromosomes == null) {
            final String[] chrs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
            standardChromosomes = Arrays.asList(chrs);
        }
        return standardChromosomes;
    }

    public static Properties getProperies() {
        return properties;
    }

    /**
     * Removes a project from the projects list.
     *
     * @param project the project to remove
     */
    public static void removeProject(Project project) {
        // Import projects
        List<String> files = Arrays.asList(properties.getProperty("projects", "").split(";"));
        // Check if project is in the list by trying to remove it
        if (files.remove(project.getConfigFile().getAbsolutePath())) {
            setProperty("projects", OS.asString(";", files));
        }
    }

    /**
     * Adds a project to the project list.
     *
     * @param project the project to add
     */
    public static void addProject(Project project) {
        // Import projects
        List<String> projects = new ArrayList<>(Arrays.asList(
                properties.getProperty("projects", "").split(";")));
        // If project is not yet in the list
        if (!projects.contains(project.getConfigFile().getAbsolutePath())) {
            // Add it
            projects.add(project.getConfigFile().getAbsolutePath());
            setProperty("projects", OS.asString(";", projects));
        }
    }
}
