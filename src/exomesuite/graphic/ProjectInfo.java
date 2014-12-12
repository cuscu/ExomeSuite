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

import exomesuite.ExomeSuite;
import exomesuite.MainViewController;
import exomesuite.project.Project;
import exomesuite.utils.Configuration;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

/**
 * Shows properties of a project. When a property is changed, changes it on the project.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ProjectInfo extends VBox implements Configuration.ConfigurationListener {

    @FXML
    private FileParam forward;
    @FXML
    private FileParam reverse;
    @FXML
    private TextParam name;
    @FXML
    private TextParam code;
    @FXML
    private TextParam description;
    @FXML
    private Param path;
    @FXML
    private ChoiceParam encoding;
    @FXML
    private ChoiceParam genome;
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

        forward.addFilter(FileManager.FASTQ_FILTER);
        reverse.addFilter(FileManager.FASTQ_FILTER);
        // Add listeners to each property change, so every time a property is changed,
        // it is reflected in project.getProperties()
        forward.setOnValueChanged(event
                -> project.getProperties().setProperty(Project.FORWARD_FASTQ, forward.getValue().getAbsolutePath()));
        reverse.setOnValueChanged(event
                -> project.getProperties().setProperty(Project.REVERSE_FASTQ, reverse.getValue().getAbsolutePath()));
        name.setOnValueChanged(event
                -> project.getProperties().setProperty(Project.NAME, name.getValue()));
        description.setOnValueChanged(event
                -> project.getProperties().setProperty(Project.DESCRIPTION, description.getValue()));
//        path.setOnValueChanged(event -> changePath());
        encoding.setOnValueChanged(event -> project.getProperties().setProperty(Project.FASTQ_ENCODING,
                encoding.getValue()));
        genome.setOnValueChanged(event -> project.getProperties().setProperty(Project.REFERENCE_GENOME,
                genome.getValue()));
        files.setContextMenu(getFilesContextMenu());
        files.setCellFactory((ListView<String> param) -> new PlainCell());
        files.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                showFile();
            }
        });
        files.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                showFile();
            }
        });
        addButton.setGraphic(new ImageView("exomesuite/img/addFile.png"));
        addButton.setOnAction(event -> addFile());
    }

    public void setProject(Project project) {
        if (project == null) {
            setVisible(false);
            this.project = null;
        } else {
            setVisible(true);
            if (this.project != null) {
                this.project.getProperties().removeListener(this);
            }
            this.project = project;
            project.getProperties().addListener(this);
//            project.addListener(this);
            if (project.getProperties().containsProperty(Project.FORWARD_FASTQ)) {
                forward.setValue(new File(project.getProperties().getProperty(Project.FORWARD_FASTQ)));
            }
            if (project.getProperties().containsProperty(Project.REVERSE_FASTQ)) {
                reverse.setValue(new File(project.getProperties().getProperty(Project.REVERSE_FASTQ)));
            }
            name.setValue(project.getProperties().getProperty(Project.NAME));
            code.setValue(project.getProperties().getProperty(Project.CODE));
            description.setValue(project.getProperties().getProperty(Project.DESCRIPTION));
            if (project.getProperties().containsProperty(Project.PATH)) {
                path.setValue(new File(project.getProperties().getProperty(Project.PATH)));
            }
            encoding.setOptions(OS.getEncodings());
            encoding.setValue(project.getProperties().getProperty(Project.FASTQ_ENCODING));
            genome.setOptions(OS.getReferenceGenomes());
            genome.setValue(project.getProperties().getProperty(Project.REFERENCE_GENOME));
            files.getItems().clear();
            // Extra files are stored separated by ;
            String fs = project.getProperties().getProperty(Project.FILES);
            if (fs != null && !fs.isEmpty()) {
                files.getItems().setAll(Arrays.asList(fs.split(";")));
            }
        }
    }

    /**
     * Opens a dialog to select a File and, if not null, adds it to the project.
     */
    private void addFile() {
        File f = FileManager.openFile("Select a file", FileManager.ALL_FILTER);
        if (f != null) {
            project.addExtraFile(f.getAbsolutePath());
        }
    }

    /**
     * When user double clicks on a File, tries to open it in the working area.
     */
    private void showFile() {
        String f = files.getSelectionModel().getSelectedItem();
        if (FileManager.tripleCheck(f)) {
            File file = new File(f);
            String grch = project.getProperties().getProperty(Project.REFERENCE_GENOME);
            ExomeSuite.getController().showFileContent(file, new File(OS.getGenome(grch)));
        } else {
            MainViewController.printMessage("File " + f + " is not accesible", "warning");
        }
    }

    /**
     * Removes the selected file.
     */
    private void removeFile() {
        String file = files.getSelectionModel().getSelectedItem();
        project.removeExtraFile(file);
    }

//    private void changePath() {
//        // Move files and directories
//        final File from = new File(project.getProperties().getProperty(Project.PATH));
//        final File to = path.getValue();
//        clone(from, to);
//        // Change properties by replacing path in all properties
//        project.getProperties().forEach((Object t, Object u) -> {
//            String key = (String) t;
//            String value = (String) u;
//            if (value.startsWith(from.getAbsolutePath())) {
//                project.getProperties().setProperty(key,
//                        value.replace(from.getAbsolutePath(), to.getAbsolutePath()));
//            }
//        });
//        // Force properties file to be written
//        project.getProperties().setProperty(Project.PATH, to.getAbsolutePath());
//    }
    private void clone(File from, File to) {
        for (File f : from.listFiles()) {
            if (f.isFile()) {
                Path source = f.toPath();
                Path target = new File(to, f.getName()).toPath();
                try {
                    Files.copy(source, target, COPY_ATTRIBUTES);
                    f.delete();
                } catch (IOException ex) {
//                    Dialogs.create().printException(ex);
                    MainViewController.printException(ex);
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

    private ContextMenu getFilesContextMenu() {
        MenuItem delete = new MenuItem("Delete", new SizableImage("exomesuite/img/cancel4.png", 16));
        delete.setOnAction(event -> removeFile());
        return new ContextMenu(delete);
    }

    @Override
    public void configurationChanged(Configuration configuration, String keyChanged) {
        System.out.println("Config changed");
        if (keyChanged.equals(Project.FILES)) {
            files.getItems().clear();
            String fs = project.getProperties().getProperty(Project.FILES);
            if (fs != null && !fs.isEmpty()) {
                files.getItems().setAll(Arrays.asList(fs.split(";")));
            }
        }
    }

    private static class PlainCell extends ListCell<String> {

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                setText(new File(item).getName());
                setTooltip(new Tooltip(item));
            } else {
                setText(null);
                setTooltip(null);
            }
        }

    }

}
