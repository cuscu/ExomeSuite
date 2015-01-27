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
import exomesuite.project.ModelProject;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Shows properties of a project. When a property is changed, changes it on the project.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ProjectInfo extends TabPane {

    @FXML
    private Button addButton;
    @FXML
    private TableView<File> moreFiles;
    @FXML
    private TextField name;
    @FXML
    private Label code;
    @FXML
    private TextField description;
    @FXML
    private TextField forward;
    @FXML
    private TextField reverse;
    @FXML
    private ComboBox<String> genome;
    @FXML
    private ComboBox<String> encoding;
    @FXML
    private Button selectForward;
    @FXML
    private Button selectReverse;

    /**
     * Selected project.
     */
    private ModelProject project;

    /**
     * Creates a Projet Info pane with the properties of the project.
     */
    public ProjectInfo() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectInfo.fxml"), ExomeSuite.getResources());
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
        initFilesTable();
        initProperties();
    }

    /**
     * Changes the project for whose properties are being displayed.
     *
     * @param project the new project
     */
    public void setProject(ModelProject project) {
        if (project == null) {
            setVisible(false);
            this.project = null;
        } else {
            setVisible(true);
            if (this.project != null) {
                unbind(this.project);
            }
            bind(project);
            this.project = project;
            moreFiles.setItems(project.getFiles());
        }
    }

    /**
     * Opens a dialog to select a File and, if not null, adds it to the project.
     */
    private void addFile() {
        File f = FileManager.openFile(ExomeSuite.getResources().getString("choose.file"), FileManager.ALL_FILTER);
        if (f != null) {
            project.getFiles().add(f);
        }
    }

    /**
     * Removes the selected file.
     */
    private void removeFile() {
        File f = moreFiles.getSelectionModel().getSelectedItem();
        if (f != null) {
            project.getFiles().remove(f);
        }
    }

    private ContextMenu getFilesContextMenu() {
        MenuItem delete = new MenuItem(ExomeSuite.getResources().getString("delete"),
                new SizableImage("exomesuite/img/cancel.png", SizableImage.SMALL_SIZE));
        delete.setOnAction(event -> removeFile());
        return new ContextMenu(delete);
    }

    private void initFilesTable() {
        TableColumn<File, String> nam = new TableColumn(ExomeSuite.getResources().getString("name"));
        nam.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        TableColumn<File, String> type = new TableColumn(ExomeSuite.getResources().getString("type"));
        type.setCellValueFactory(param -> {
            String na = param.getValue().getName();
            return new SimpleStringProperty(na.substring(na.lastIndexOf(".") + 1));
        });
        moreFiles.getColumns().addAll(nam, type);
        moreFiles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        moreFiles.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                showFile();
            }
        });
        moreFiles.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                showFile();
            }
        });
        moreFiles.setContextMenu(getFilesContextMenu());
        addButton.setGraphic(new SizableImage("exomesuite/img/addFile.png", SizableImage.SMALL_SIZE));
        addButton.setOnAction(event -> addFile());
    }

    /**
     * Opens the current selected file.
     */
    private void showFile() {
        File f = moreFiles.getSelectionModel().getSelectedItem();
        if (f != null) {
            MainViewController.showFileContent(f, project);
        }
    }

    private void initProperties() {
        genome.setItems(OS.getReferenceGenomes());
        encoding.setItems(OS.getEncodings());
        selectForward.setOnAction(event -> {
            String message = ExomeSuite.getStringFormatted("select.file", "FASTQ file");
            File f;
            if (!forward.getText().isEmpty()) {
                File parent = new File(forward.getText()).getParentFile();
                f = FileManager.openFile(forward, message, parent, FileManager.FASTQ_FILTER);
            } else {
                f = FileManager.openFile(forward, message, FileManager.FASTQ_FILTER);
            }
            if (f != null) {
                project.setForwardSequences(f);
                forward.end();
            }
        });
        selectReverse.setOnAction(event -> {
            String message = ExomeSuite.getStringFormatted("select.file", "FASTQ file");
            File f;
            if (!reverse.getText().isEmpty()) {
                File parent = new File(reverse.getText()).getParentFile();
                f = FileManager.openFile(reverse, message, parent, FileManager.FASTQ_FILTER);
            } else {
                f = FileManager.openFile(reverse, message, FileManager.FASTQ_FILTER);
            }
            if (f != null) {
                project.setReverseSequences(f);
                reverse.end();
            }
        });
    }

    private void unbind(ModelProject project) {
        name.textProperty().unbindBidirectional(project.getNameProperty());
        description.textProperty().unbindBidirectional(project.getDescriptionProperty());
        name.setText(null);
        description.setText(null);
        genome.setOnAction(null);
        encoding.setOnAction(null);
        forward.setText(null);
        reverse.setText(null);
        genome.setValue(null);
        encoding.setValue(null);
    }

    private void bind(ModelProject project) {
        name.textProperty().bindBidirectional(project.getNameProperty());
        description.textProperty().bindBidirectional(project.getDescriptionProperty());
        code.textProperty().bind(project.getCodeProperty());
        if (project.getForwardSequences() != null) {
            forward.setText(project.getForwardSequences().getAbsolutePath());
            forward.end();
        }
        if (project.getReverseSequences() != null) {
            reverse.setText(project.getReverseSequences().getAbsolutePath());
            reverse.end();
        }
        if (project.getGenomeCode() != null && !project.getGenomeCode().isEmpty()) {
            genome.getSelectionModel().select(project.getGenomeCode());
        }
        genome.setOnAction(event -> project.setGenomeCode(genome.getValue()));
        if (project.getEncoding() != null && !project.getEncoding().isEmpty()) {
            encoding.getSelectionModel().select(project.getEncoding());
        }
        encoding.setOnAction(event -> project.setEncoding(encoding.getValue()));
    }
}
