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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;

/**
 * Shows properties of a project. When a property is changed, changes it on the project.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ProjectInfo extends TabPane implements Configuration.ConfigurationListener {

    @FXML
    private Button addButton;
    @FXML
    private TableView<File> moreFiles;
    @FXML
    private TableView<Prop> properties;

    TableColumn<Prop, String> value;

    private Project project;

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
        initFilesTable();
        initPropertiesTable();
    }

    /**
     * Changes the project for whose properties are being displayed.
     *
     * @param project the new project
     */
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
            moreFiles.getItems().clear();
            // Extra files are stored separated by ;
            String fs = project.getProperties().getProperty(Project.FILES);
            if (fs != null && !fs.isEmpty()) {
                for (String s : fs.split(";")) {
                    moreFiles.getItems().add(new File(s));
                }
            }
            reShowPropTable();
        }
    }

    /**
     * Opens a dialog to select a File and, if not null, adds it to the project.
     */
    private void addFile() {
        File f = FileManager.openFile(ExomeSuite.getResources().getString("choose.file"), FileManager.ALL_FILTER);
        if (f != null) {
            project.addExtraFile(f.getAbsolutePath());
        }
    }

    /**
     * Removes the selected file.
     */
    private void removeFile() {
        File f = moreFiles.getSelectionModel().getSelectedItem();
        if (f != null) {
            project.removeExtraFile(f.getAbsolutePath());
        }
    }

    private ContextMenu getFilesContextMenu() {
        MenuItem delete = new MenuItem(ExomeSuite.getResources().getString("delete"),
                new SizableImage("exomesuite/img/cancel.png", SizableImage.SMALL_SIZE));
        delete.setOnAction(event -> removeFile());
        return new ContextMenu(delete);
    }

    @Override
    public void configurationChanged(Configuration configuration, String keyChanged) {
        if (keyChanged.equals(Project.FILES)) {
            moreFiles.getItems().clear();
            String fs = project.getProperties().getProperty(Project.FILES);
            if (fs != null && !fs.isEmpty()) {
                for (String s : fs.split(";")) {
                    moreFiles.getItems().add(new File(s));
                }
            }
        } else {
            reShowPropTable();
        }
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
    }

    /**
     * Opens the current selected file.
     */
    private void showFile() {
        File f = moreFiles.getSelectionModel().getSelectedItem();
        if (f == null) {
            return;
        }
        if (f.exists()) {
            String grch = project.getProperties().getProperty(Project.REFERENCE_GENOME);
            String refgen = OS.getProperties().getProperty(grch);
            File secondary = null;
            if (refgen != null) {
                secondary = new File(refgen);
            }
            MainViewController.showFileContent(f, secondary);
        } else {
            String message = ExomeSuite.getStringFormatted("file.not.accesible", f);
            MainViewController.printMessage(message, "warning");
        }
    }

    /**
     * Initializes the properties table.
     */
    private void initPropertiesTable() {
        TableColumn<Prop, String> key = new TableColumn(ExomeSuite.getResources().getString("name"));
        key.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().key));
        value = new TableColumn(ExomeSuite.getResources().getString("value"));
        value.setCellValueFactory(row
                -> new SimpleStringProperty(project != null ? project.getProperties().getProperty(row.getValue().key) : ""));
        properties.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        properties.getColumns().addAll(key, value);
        Prop n = new Prop(Project.NAME, Prop.TYPE.STRING);
        Prop c = new Prop(Project.CODE, Prop.TYPE.READ_ONLY);
        Prop enc = new Prop(Project.FASTQ_ENCODING, Prop.TYPE.COMBO);
        enc.options = OS.getEncodings();
        Prop desc = new Prop(Project.DESCRIPTION, Prop.TYPE.STRING);
        Prop pat = new Prop(Project.PATH, Prop.TYPE.READ_ONLY);
        Prop fw = new Prop(Project.FORWARD_FASTQ, Prop.TYPE.FILE);
        fw.filters.add(FileManager.FASTQ_FILTER);
        Prop rv = new Prop(Project.REVERSE_FASTQ, Prop.TYPE.FILE);
        rv.filters.add(FileManager.FASTQ_FILTER);
        Prop rf = new Prop(Project.REFERENCE_GENOME, Prop.TYPE.COMBO);
        rf.options = OS.getReferenceGenomes();
        properties.getItems().addAll(n, c, pat, desc, fw, rv, enc, rf);
        reShowPropTable();
    }

    private void reShowPropTable() {
        value.setCellFactory(column -> new PropCell(project));
    }

    /**
     * Prepares a property for the Properties Table
     */
    private static class Prop {

        /**
         * Key of the property.
         */
        String key;
        /**
         * Type of the property.
         */
        TYPE type;
        /**
         * Options for COMBO properties.
         */
        List<String> options = new ArrayList();
        /**
         * Filters for FILE property.
         */
        List<FileChooser.ExtensionFilter> filters = new ArrayList();

        /**
         * Creates a Property wrap for the properties table.
         *
         * @param key property key
         * @param type type of property
         */
        public Prop(String key, TYPE type) {
            this.key = key;
            this.type = type;
        }

        /**
         * Type of property.
         */
        enum TYPE {

            /**
             * Allows to select a single file.
             */
            FILE,
            /**
             * Allows to edit a text value.
             */
            STRING,
            /**
             * Allows to select a single Path.
             */
            PATH,
            /**
             * Allows to select value from options.
             */
            COMBO,
            /**
             * Only shows the value. Do not allow to edit.
             */
            READ_ONLY
        }

    }

    /**
     * Cells to edit propoerties. The behaviour of each cell depends on its type.
     */
    private static final class PropCell extends TableCell<Prop, String> {

        /**
         * For text properties (TYPE:STRING).
         */
        final TextField textField = new TextField();
        /**
         * For combo properties (TYPE:COMBO).
         */
        final ComboBox<String> comboBox = new ComboBox();
        /**
         * Associated project.
         */
        Project project;

        /**
         * Creates a cell that conveniently edits the property based on its type.
         *
         * @param project associated project
         */
        public PropCell(Project project) {
            textField.setOnAction(event -> commitEdit(textField.getText()));
            comboBox.setOnAction(event -> commitEdit(comboBox.getValue()));
            this.project = project;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item);
                setGraphic(null);
            }
        }

        @Override
        public void startEdit() {
            super.startEdit();
            setText(null);
            Prop p = (Prop) getTableRow().getItem();
            switch (p.type) {
                case STRING:
                    // Show the textField. When textField is actioned,
                    // it calls commitEdit(testField.getText()) (see Constructor)
                    setGraphic(textField);
                    textField.setText(getItem());
                    break;
                case FILE:
                    // Opens a file with FileManager.
                    // Then cancels or commits, but does not stay in edit mode.
                    File f = FileManager.openFile(ExomeSuite.getResources().getString("choose.file"), p.filters);
                    if (f == null) {
                        cancelEdit();
                    } else {
                        commitEdit(f.getAbsolutePath());
                    }
                    break;
                case PATH:
                    // Opens a path with FileManager.
                    // Then cancels or commits, but does not stay in edit mode.
                    File path = FileManager.openDirectory(ExomeSuite.getResources().getString("select.path"));
                    if (path == null) {
                        cancelEdit();
                    } else {
                        commitEdit(path.getAbsolutePath());
                    }
                    break;
                case COMBO:
                    // Shows the comboBox and tries to select current option.
                    // Combobox is closed when a different option is selected (see constructor).
                    setGraphic(comboBox);
                    comboBox.getItems().setAll(p.options);
//                    comboBox.getSelectionModel().select(getItem());
                    break;
                case READ_ONLY:
                    // Buahahahahaha. Doesn't allow edit.
                    cancelEdit();
            }
        }

        @Override
        public void commitEdit(String newValue) {
            super.commitEdit(newValue);
            // Here is where the property is in fact changed.
            Prop p = (Prop) getTableRow().getItem();
            project.getProperties().setProperty(p.key, newValue);
            // And the cell goes to non-edit mode.
            setText(newValue);
            setGraphic(null);
        }

        @Override
        public void cancelEdit() {
            // I suppose that when user clicks outside the cell it also cancels edit.
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }

    }

}
