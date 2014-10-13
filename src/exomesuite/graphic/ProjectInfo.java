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
package exomesuite.graphic;

import exomesuite.MainViewController;
import exomesuite.project.Project;
import exomesuite.project.ProjectListener;
import exomesuite.tsvreader.TSVReader;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import exomesuite.vcfreader.VCFReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.controlsfx.dialog.Dialogs;

/**
 * Shows properties of a project.
 *
 * @author Pascual Lorente Arencibia
 */
public class ProjectInfo extends VBox implements ProjectListener {

    @FXML
    private FileSelector forward;
    @FXML
    private FileSelector reverse;
    @FXML
    private TextField name;
    @FXML
    private Label code;
    @FXML
    private TextField description;
    @FXML
    private FileSelector path;
    @FXML
    private ComboBox<String> encoding;
    @FXML
    private ComboBox<String> genome;
    @FXML
    private FileSelector alignments;
    @FXML
    private FileSelector variants;
    @FXML
    private ListView<String> files;
    @FXML
    private Button addButton;

    private Project project;

    public ProjectInfo() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectInfo.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void initialize() {
        // By default, this panel is hidden
        setVisible(false);

        forward.addExtensionFilter(FileManager.FASTQ_FILTER);
        reverse.addExtensionFilter(FileManager.FASTQ_FILTER);
        // Add listeners to each property change, so every time a property is changed,
        // it is reflected in project.getProperties()
        forward.setOnFileChange(event
                -> project.setProperty(Project.PropertyName.FORWARD_FASTQ, forward.getFile()));
        reverse.setOnFileChange(event
                -> project.setProperty(Project.PropertyName.REVERSE_FASTQ, reverse.getFile()));
        name.setOnKeyReleased(event
                -> project.setProperty(Project.PropertyName.NAME, name.getText()));
        description.setOnKeyReleased(event
                -> project.setProperty(Project.PropertyName.DESCRIPTION, description.getText()));
        path.setOnFileChange(event -> changePath());
        alignments.addExtensionFilter(FileManager.SAM_FILTER);
        alignments.setOnFileChange(event
                -> project.setProperty(Project.PropertyName.BAM_FILE, alignments.getFile()));
        variants.addExtensionFilter(FileManager.VCF_FILTER);
        variants.setOnFileChange(event
                -> project.setProperty(Project.PropertyName.VCF_FILE, variants.getFile()));
//        path.setDisable(true);
        // Fill encodings and genomes lists
        encoding.getItems().addAll(OS.getSupportedEncodings().keySet());
        encoding.getSelectionModel().selectedItemProperty().addListener((
                ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (oldValue != null) {
                Dialogs.create().title("Change encoding").
                        message("Changing encoding can change the behaviour of aligning process.").
                        showWarning();
            }
            project.setProperty(Project.PropertyName.FASTQ_ENCODING,
                    OS.getSupportedEncodings().get(encoding.getValue()));
        });
        genome.getItems().addAll(OS.getSupportedReferenceGenomes().keySet());
        genome.getSelectionModel().selectedItemProperty().addListener((
                ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (oldValue != null) {
                Dialogs.create().title("Warning").
                        message("Changing genome can change the behaviour of aligning and call processes.").
                        showWarning();
            }
            project.setProperty(Project.PropertyName.REFERENCE_GENOME,
                    OS.getSupportedReferenceGenomes().get(genome.getValue()));
        });
        // Add behaviour to other files list
        files.getSelectionModel().selectedItemProperty().addListener((
                ObservableValue<? extends String> observable, String oldValue, String newValue)
                -> showFile());
        files.setContextMenu(getFilesContextMenu());
        // humm, with only one file I cannot listen to chagnes in selected item.
        files.setOnMouseClicked((MouseEvent event) -> showFile());
        addButton.setGraphic(new ImageView("exomesuite/img/addFile.png"));
        addButton.setOnAction(event -> addFile());
    }

    public void setProject(Project project) {
        if (project == null) {
            setVisible(false);
            this.project = null;
        } else {
            setVisible(true);
            this.project = project;
            forward.setFile(project.getProperty(Project.PropertyName.FORWARD_FASTQ, ""));
            reverse.setFile(project.getProperty(Project.PropertyName.REVERSE_FASTQ, ""));
            name.setText(project.getProperty(Project.PropertyName.NAME, ""));
            code.setText(project.getProperty(Project.PropertyName.CODE, ""));
            description.setText(project.getProperty(Project.PropertyName.DESCRIPTION, ""));
            path.setFile(project.getProperty(Project.PropertyName.PATH, ""));
            alignments.setFile(project.getProperty(Project.PropertyName.BAM_FILE, ""));
            variants.setFile(project.getProperty(Project.PropertyName.VCF_FILE, ""));
            String encodingKey = project.getProperty(Project.PropertyName.FASTQ_ENCODING);
            if (encodingKey != null) {
                // Look for a value equals to
                OS.getSupportedEncodings().entrySet().stream().
                        filter((entrySet) -> (entrySet.getValue().equals(encodingKey))).
                        forEach((entrySet) -> {
                    encoding.getSelectionModel().select(entrySet.getKey());
                });
            }
            String genomeValue = project.getProperty(Project.PropertyName.REFERENCE_GENOME);
            if (genomeValue != null) {
                // Look for a value equals to
                OS.getSupportedReferenceGenomes().entrySet().stream().
                        filter((entrySet) -> (entrySet.getValue().equals(genomeValue))).
                        forEach((entrySet) -> {
                    genome.getSelectionModel().select(entrySet.getKey());
                });
            }
            files.getItems().clear();
            String fs = project.getProperty(Project.PropertyName.FILES);
            if (fs != null && !fs.isEmpty()) {
                files.getItems().setAll(Arrays.asList(fs.split(";")));
            }
        }
    }

    private void addFile() {
        File f = FileManager.openFile("Select a file", FileManager.ALL_FILTER);
        if (f != null && !files.getItems().contains(f.getAbsolutePath())) {
            files.getItems().add(f.getAbsolutePath());
            // Add file to properties
            String fils = project.getProperty(Project.PropertyName.FILES, "");
            fils += f.getAbsolutePath() + ";";
            project.setProperty(Project.PropertyName.FILES, fils);
        }
    }

    private void showFile() {
        String f = files.getSelectionModel().getSelectedItem();
        if (f == null || f.isEmpty()) {
            return;
        }
        File file = new File(f);
        TabPane wa = MainViewController.getWorkingArea();
        // If it is already open, do not open another tab, do not be a spammer
        for (Tab t : wa.getTabs()) {
            if (t.getText().equals(file.getName())) {
                wa.getSelectionModel().select(t);
                return;
            }
        }
        Tab t = new Tab(file.getName());
        // Use TSV only on TSV files
        if (f.endsWith(".tsv") || f.endsWith(".txt") || f.endsWith(".mist")) {
            t.setContent(new TSVReader(file).get());
        } else if (f.endsWith(".vcf")) {
            t.setContent(new VCFReader(file).getView());
        } else {
            return;
        }
        MainViewController.getWorkingArea().getTabs().add(t);
    }

    private void removeFile() {
        String file = files.getSelectionModel().getSelectedItem();
        int index = files.getSelectionModel().getSelectedIndex();
        // Remove file from properties
        String fils = project.getProperty(Project.PropertyName.FILES, "");
        fils = fils.replace(file + ";", "");
        project.setProperty(Project.PropertyName.FILES, fils);
//            new File(file).delete(); NOO, don't delete the file. Or at least ask the user for
        files.getItems().remove(index);
    }

    private void changePath() {
        // Move files and directories
        final File from = new File(project.getProperty(Project.PropertyName.PATH));
        final File to = new File(path.getFile());
        clone(from, to);
        // Change properties by replacing path in all properties
        project.getProperties().forEach((Object t, Object u) -> {
            String key = (String) t;
            String value = (String) u;
            if (value.startsWith(from.getAbsolutePath())) {
                project.getProperties().setProperty(key,
                        value.replace(from.getAbsolutePath(), to.getAbsolutePath()));
            }
        });
        // Force properties file to be written
        project.setProperty(Project.PropertyName.PATH, to.getAbsolutePath());
    }

    private void clone(File from, File to) {
        for (File f : from.listFiles()) {
            if (f.isFile()) {
                Path source = f.toPath();
                Path target = new File(to, f.getName()).toPath();
                try {
                    Files.copy(source, target, COPY_ATTRIBUTES);
                    f.delete();
                } catch (IOException ex) {
                    Dialogs.create().showException(ex);
                    Logger.getLogger(ProjectInfo.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else if (f.isDirectory()) {
                File newPath = new File(to, f.getName());
                newPath.mkdirs();
                clone(f, newPath);
            } else {
                f.delete();
            }
        }
        from.delete();
    }

    @Override
    public void projectChanged(Project.PropertyName property) {
        // Outside ProjectInfo, only a few properties can be changed.
        switch (property) {
            case BAM_FILE:
                alignments.setFile(project.getProperty(Project.PropertyName.BAM_FILE, ""));
                break;
            case VCF_FILE:
                variants.setFile(project.getProperty(Project.PropertyName.VCF_FILE, ""));
                break;
            case FILES:
                files.getItems().clear();
                String fs = project.getProperty(Project.PropertyName.FILES);
                if (fs != null && !fs.isEmpty()) {
                    files.getItems().setAll(Arrays.asList(fs.split(";")));
                }
                break;

        }
    }

    private ContextMenu getFilesContextMenu() {
        MenuItem delete = new MenuItem("Delete", new ImageView("exomesuite/img/cancel4.png"));
        delete.setOnAction(event -> removeFile());
        return new ContextMenu(delete);
    }

}
