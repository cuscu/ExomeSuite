package exomesuite.utils;

import exomesuite.MainViewController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 * Contains methods to control files in DNAnalytics and fields containing file filters. Open and
 * save files, set TextFileds with the name of the files and compress and uncompress files.
 *
 * @author Pascual Lorente Arencibia
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
     * TODO: search for jre1.7.0_51/bin/java.
     *
     * @return a String with java7 path.
     */
    public static String scanJava7() {
        if (System.getProperty("os.name").contains("Linux")) {
            ProcessBuilder pb = new ProcessBuilder("locate", "--regex", ".*1\\.7.*java$");
            String java7 = null;
            try {
                Process p = pb.start();
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(p.getInputStream()))) {
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

        } else {
            return null;
        }
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

    public static void downloadSomething(MainViewController mainViewController) {
        Task genome
                = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        String[] genomeFiles
                        = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
                            "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
                        String ftpserver = "ftp://ftp.ncbi.nlm.nih.gov/genbank/genomes";
                        String ftpGenome = "/Eukaryotes/vertebrates_mammals/Homo_sapiens/GRCh38";
                        String ftpFASTA = "/Primary_Assembly/assembled_chromosomes/FASTA/";
                        final String ftpLink = ftpserver + ftpGenome + ftpFASTA;
                        for (int i = 0; i < 3;
                        i++) {
                            String chr = "chr" + genomeFiles[i] + ".fa.gz";
                            File f = new File(chr);
                            final String ftp = ftpLink + chr;
                            Download download = new Download(ftp, f);
                            MainViewController.getInfo().textProperty().bind(download.
                                    messageProperty());
                            mainViewController.getProgress().progressProperty().bind(download.
                                    progressProperty());
                            Thread th = new Thread(download);
                            th.start();
                        }
                        return null;
                    }
                };
        new Thread(genome).start();
    }

    public static List<String> getStandardChromosomes() {
        if (standardChromosomes == null) {
            final String[] chrs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
            standardChromosomes = Arrays.asList(chrs);
        }
        return standardChromosomes;
    }
}
