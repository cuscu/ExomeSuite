package exomesuite.utils;

import java.io.File;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

/**
 * Contains methods to control files in DNAnalytics and fields containing file filters. Open and
 * save files, set TextFileds with the name of the files and compress and uncompress files.
 *
 * @author Pascual Lorente Arencibia
 */
public class OS {

    private static File lastPath;

//    public static final String FASTQ_DESCRIPTION = resources.getString("file.fastq");
    public static final String FASTQ_EXTENSION = ".fastq .fq .fq.gz .fastq.gz";
    public static final String FASTQ_DESCRIPTION = "FASTQ file";
    public static final String[] FASTQ_FILTERS = new String[]{"*.fq", "*.fastq", "*.fq.gz",
        "*.fastq.gz"};

    public static final String FASTA_EXTENSION = ".fasta";
//    public static final String FASTA_DESCRIPTION = resources.getString("file.fasta");
    public static final String FASTA_DESCRIPTION = "FASTA file";
    public static final String[] FASTA_FILTERS = new String[]{"*.fasta", "*.fa"};

    public static final String VCF_EXTENSION = ".vcf";
    public static final String VCF_DESCRIPTION = "VCF file";
//    public static final String VCF_DESCRIPTION = resources.getString("file.vcf");
    public static final String[] VCF_FILTERS = new String[]{"*.vcf"};

    public static final String SAM_EXTENSION = ".sam";
    public static final String BAM_EXTENSION = ".bam";
    public static final String SAM_BAM_DESCRIPTION = "SAM/BAM file";
//    public static final String SAM_BAM_DESCRIPTION = resources.getString("file.sambam");
    public static final String[] SAM_BAM_FILTERS = new String[]{"*.bam", "*.sam"};

    public static final String TSV_DESCRIPTION = "Text/Tab separated values file";
//    public static final String TSV_DESCRIPTION = resources.getString("file.tsv");
    public static final String TSV_EXTENSION = ".tsv";
    public static final String[] TSV_FILTERS = new String[]{"*.tsv"};

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
     * Opens a dialog for the users to select a File from local directory.
     *
     * @param title Dialog title.
     * @param description Description of file type.
     * @param filters Regular expressions to filter file types (*.extension).
     * @return A File with user selected file, or null if user canceled.
     */
    public static File openFile(String title, String description, String[] filters) {
        FileChooser fileChooser = new FileChooser();
        if (title != null) {
            fileChooser.setTitle(title);
        }
        fileChooser.setInitialDirectory(lastPath);
        if (description != null && filters != null) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(description, filters));
        }
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            lastPath = file.getParentFile();
            return file;
        }
        return null;
    }

    /**
     * Opens a dialog for the user to select a file and sets textField text to selected file name.
     * If FileChooser is canceled, textField is not modified.
     *
     * @param title FileChooser Title.
     * @param filterDesc Short description of the file filter.
     * @param filters List of regular expressions to filter.
     * @param textField The textField to modify.
     * @return The chosen file or null if the operation was canceled.
     */
    public static File openFile(String title, String filterDesc, String[] filters,
            TextField textField) {
        File file = openFile(title, filterDesc, filters);
        if (file != null) {
            textField.setText(file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Shows a dialog to the user to select a Variant Call File (.vcf). Sets the text of the
     * TextField to the name of the file.
     *
     * @param textField A TextField to contain the file name.
     * @return The file selected or null if user canceled.
     */
    public static File openVCF(TextField textField) {
        return openFile(VCF_DESCRIPTION, VCF_DESCRIPTION, VCF_FILTERS, textField);
    }

    /**
     * Shows a dialog to the user to select a Binary SAM file (.bam). Sets the text of the TextField
     * to the name of the file.
     *
     * @param textField A TextField to contain the file name.
     * @return The file selected or null if user canceled.
     */
    public static File openBAM(TextField textField) {
        return openFile(SAM_BAM_DESCRIPTION, SAM_BAM_DESCRIPTION, SAM_BAM_FILTERS, textField);
    }

    /**
     * Shows a dialog to the user to select a FASTA file (.fa or .fasta). Sets the text of the
     * TextField to the name of the file.
     *
     * @param textField A TextField to contain the file name.
     * @return The file selected or null if user canceled.
     */
    public static File openFASTA(TextField textField) {
        return openFile(FASTA_DESCRIPTION, FASTA_EXTENSION, FASTA_FILTERS, textField);
    }

    /**
     * Shows a dialog to the user to select a FASTA file (.fa or .fasta). Sets the text of the
     * TextField to the name of the file.
     *
     * @return The file selected or null if user canceled.
     */
    public static File openFASTA() {
        return openFile(FASTA_DESCRIPTION, FASTA_EXTENSION, FASTA_FILTERS);
    }

    /**
     * Shows a dialog to the user to select a FASTA file (.fa or .fasta). Sets the text of the
     * TextField to the name of the file.
     *
     * @param textField A TextField to contain the file name.
     * @return The file selected or null if user canceled.
     */
    public static File openTSV(TextField textField) {
        return openFile(TSV_DESCRIPTION, TSV_EXTENSION, TSV_FILTERS, textField);
    }

    public static File openFASTQ(TextField textField) {
        return openFile(FASTQ_DESCRIPTION, FASTQ_EXTENSION, FASTQ_FILTERS, textField);
    }

    /**
     * Opens a dialog for the user to create a File. File system file is not created immediately.
     * The File is just passed to one of the Workers. If the Worker ends successfully, then the file
     * will have been created.
     *
     * @param title Dialog title
     * @param filterDesc Description of file type
     * @param filters Regular expressions to filter file types (*.extension)
     * @param extension default extension
     * @return A File with the user selected file, or null if not file selected
     */
    public static File saveFile(String title, String filterDesc, String[] filters, String extension) {
        FileChooser fileChooser = new FileChooser();
        if (title != null) {
            fileChooser.setTitle(title);
        }
        fileChooser.setInitialDirectory(lastPath);
        if (filters != null && filterDesc != null) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(filterDesc, filters));
        }
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            lastPath = file.getParentFile();
            // Add extension to bad named files
            return file.getAbsolutePath().endsWith(extension) ? file : new File(
                    file.getAbsolutePath() + extension);
        }
        return null;
    }

    /**
     * Opens a dialog for the user to create a file and sets the text of the TextField to the file
     * name. File system file is not created immediately. The File is just passed to one of the
     * Workers. If the Worker ends successfully, then the file will have been created.
     *
     * @param title Dialog title
     * @param filterDesc Description of file type
     * @param filters Regular expressions to filter file types (*.extension)
     * @param extension Default extension
     * @param textField textField associated to the file
     */
    public static void saveFile(String title, String filterDesc, String[] filters, String extension,
            TextField textField) {
        File file = saveFile(title, filterDesc, filters, extension);
        if (file != null) {
            textField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Opens a dialog for the user to create a Variant Call File (.vcf). The file is not created
     * immediately, just stored as text.
     *
     * @param textField textField containig VCF file name.
     */
    public static void saveVCF(TextField textField) {
        saveFile(VCF_DESCRIPTION, VCF_DESCRIPTION, VCF_FILTERS, VCF_EXTENSION, textField);
    }

    /**
     * Opens a dialog for the user to create a Tabular Separated Vaules file (.tsv). The file is not
     * created immediately, just stored as text.
     *
     * @param textField textField containig TSV file name.
     */
    public static void saveTSV(TextField textField) {
        saveFile(TSV_DESCRIPTION, TSV_DESCRIPTION, TSV_FILTERS, TSV_EXTENSION, textField);
    }

    /**
     * Opens a dialog for the user to create a Binary SAM file (.bam). The file is not created
     * immediately, just stored as text.
     *
     * @param textField textField containig BAM file name.
     */
    public static void saveBAM(TextField textField) {
        saveFile(SAM_BAM_DESCRIPTION, SAM_BAM_DESCRIPTION, SAM_BAM_FILTERS, BAM_EXTENSION, textField);
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

}
