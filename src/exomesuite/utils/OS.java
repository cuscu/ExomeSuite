package exomesuite.utils;

import java.io.File;
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
     * The last successful path. Id est, the last path where the user did not canceled the file
     * selection.
     */
    private static File lastPath;
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

    public OS() {
        switch (System.getProperty("os.name")) {
            case "Windows 7":
                lastPath = new File(System.getenv("user.dir"));
                break;
            case "Linux":
            default:
                lastPath = new File(System.getenv("PWD"));
        }
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
        chooser.setInitialDirectory(lastPath);
        chooser.getExtensionFilters().addAll(filters);
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            lastPath = file.getParentFile();
            String ext = chooser.getSelectedExtensionFilter().getExtensions().get(0);
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
        chooser.setInitialDirectory(lastPath);
        File file = chooser.showDialog(null);
        return (file != null) ? (lastPath = file) : null;
    }

    /**
     * TODO: search for jre1.7.0_51/bin/java.
     *
     * @return a String with java7 path.
     */
    public static String scanJava7() {
        return "/usr/java/jre1.7.0_51/bin/java";
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
        chooser.setInitialDirectory(lastPath);
        chooser.getExtensionFilters().addAll(filters);
        chooser.setTitle(title);
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            lastPath = f.getParentFile();
        }
        return f;
    }
}
