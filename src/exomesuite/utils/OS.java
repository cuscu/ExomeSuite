package exomesuite.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Contains methods to control files in DNAnalytics and fields containing file filters. Open and
 * save files, set TextFileds with the name of the files and compress and uncompress files.
 *
 * @author Pascual Lorente Arencibia
 */
public class OS {

    /**
     * Filters FASTA files (.fasta .fa)
     */
    public static final ExtensionFilter FASTA_FILTER = new ExtensionFilter("FASTA (.fasta .fa)",
            "*.fasta", "*.fa");
    /**
     * Filters FASTQ files (.fq .fastq .fq.gz .fasq.gz)
     */
    public static final ExtensionFilter FASTQ_FILTER = new ExtensionFilter(
            "FASTQ (.fastq .fq .fq.gz .fastq.gz)", "*.fastq", "*.fq", "*.fq.gz", "*.fastq.gz");
    /**
     * Filters MIST files (.mist)
     */
    public static final ExtensionFilter MIST_FILTER = new ExtensionFilter(
            "Missing sequences tool format (.mist)", "*.mist");
    /**
     * Filters TSV files (.tsv)
     */
    public static final ExtensionFilter TSV_FILTER
            = new ExtensionFilter("Tab Separated Values (.tsv)", "*.tsv");
    /**
     * Filters SAM or BAM files (.bam and .sam)
     */
    public static final ExtensionFilter SAM_FILTER = new ExtensionFilter(
            "Sequence Alignment/Map Format (.bam .sam)", "*.bam", "*.sam");
    /**
     * Filters VCF files (.vcf)
     */
    public static final ExtensionFilter VCF_FILTER = new ExtensionFilter(
            "Variant Call Format (.vcf)", "*.vcf");
    /**
     * Admits all files
     */
    public static final ExtensionFilter ALL_FILTER = new ExtensionFilter("All files", "*");
    /**
     * Filters config files (.config)
     */
    public static final ExtensionFilter CONFIG_FILTER = new ExtensionFilter("Config file (.config)",
            "*.config");

    /**
     * The last successful path. Id est, the last path where the user did not canceled the file
     * selection.
     */
    private static File lastPath;

    private static Properties properties;
    private static File propertiesFile;
    private static File usrPath;
    private static File usrHomePath;

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
        return getProperties().containsKey(key);
    }

    public OS() {
        lastPath = getUserPath();

    }

    /**
     * Opens a dialog for the user to create a file. File system file is not created in this method.
     * If the user do not write the file extension, the default will be the first of the selected
     * ExtensionFilter.
     *
     * @param title dialog title
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(String title, ExtensionFilter... filters) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.setInitialDirectory(getLastPath());
        chooser.getExtensionFilters().addAll(filters);
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            lastPath = file.getParentFile();
            String ext = chooser.getSelectedExtensionFilter().getExtensions().get(0).
                    replace("*", "");
            // Add extension to bad named files

            if (file.getName().endsWith(ext)) {
                return file;
            } else {
                return new File(file.getAbsolutePath() + ext);
            }
        }
        return null;
    }

    /**
     * Opens a dialog for the user to create a file and sets the text of the TextField to the file
     * name. If the user do not write the file extension, the default will be the first of the
     * selected ExtensionFilter.
     *
     * @param textField
     * @param title dialog title
     * @param filters any number of ExtensionFilters
     * @return the selected file or null
     */
    public static File saveFile(TextField textField, String title, ExtensionFilter... filters) {
        File f = saveFile(title, filters);
        if (f != null) {
            textField.setText(f.getAbsolutePath());
        }
        return f;
    }

    /**
     * Opens a Dialog to select a folder.
     *
     * @param title The title for the DirectoryChooser.
     * @return A File or null if user canceled.
     */
    public static File selectFolder(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        if (title != null) {
            chooser.setTitle(title);
        }
        chooser.setInitialDirectory(getLastPath());
        File file = chooser.showDialog(null);
        return (file != null) ? (lastPath = file) : null;
    }

    /**
     * TODO: search for jre1.7.0_51/bin/java.
     *
     * @return a String with java7 path.
     */
    public static String scanJava7() {
        ProcessBuilder pb = new ProcessBuilder("locate", "--regex", ".*1\\.7.*java$");
        String java7 = null;
        try {
            Process p = pb.start();
            List<String> javas = new ArrayList<>();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                while ((java7 = in.readLine()) != null) {
                    ProcessBuilder pbj = new ProcessBuilder(java7);
                    Process pj = pbj.start();
                    if (pj.waitFor() != 127) {
                        p.destroy();
                        return java7;
                    }
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.println("Java 1.7 not found in System");
        return java7;
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select a single File. Additionally, if
     * the selected file is not null, the textField text will be set to the file.getAbsolutePath().
     *
     * @param textField the textField to set if a file is selected
     * @param title the title of the FileChooser
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected file or null
     */
    public static File openFile(TextField textField, String title, ExtensionFilter... filters) {
        File f = openFile(title, filters);
        if (f != null) {
            textField.setText(f.getAbsolutePath());
        }
        return f;
    }

    /**
     * Opens a dialog window (FileChooser) and lets the user select a single File.
     *
     * @param title the title of the FileChooser
     * @param filters any number of ExtensionFilter. Use OS.[FORMAT]_FILTER constants.
     * @return the selected file or null
     */
    public static File openFile(String title, ExtensionFilter... filters) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(getLastPath());
        chooser.getExtensionFilters().addAll(filters);
        chooser.setTitle(title);
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            lastPath = f.getParentFile();
        }
        return f;
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] -> value1,value2,value3
     *
     * @param separator
     * @param values
     * @return
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
     * @param separator
     * @param values
     * @return
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

    public static boolean downloadFromWeb(String url, String user, String pass) {
        return false;
    }

    public static String getTempDir() {
        return new File(getUserPath(), "temp").getAbsolutePath();
    }

    public static String getGenome(String property) {
        return getProperties().getProperty(property);
    }

    public static File getUserPath() {
        if (usrPath == null) {
            usrPath = new File(System.getProperty("user.dir"));
        }
        return usrPath;
    }

    public static File getUserHomePath() {
        if (usrHomePath == null) {
            usrHomePath = new File(System.getProperty("user.home"));
        }
        return usrHomePath;
    }

    private static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            propertiesFile = new File(getUserPath(), "properties.txt");
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

    public static File getLastPath() {
        if (lastPath == null) {
            lastPath = getUserPath();
        }
        return lastPath;
    }

    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }

    public static void setProperty(String key, String value) {
        getProperties().setProperty(key, value);
        try {
            properties.store(new FileOutputStream(propertiesFile), null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
