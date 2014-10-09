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

import exomesuite.project.Project;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class ProjectTable extends TableView<Project> {

    @FXML
    private TableColumn<Project, String> name;
    @FXML
    private TableColumn<Project, String> code;

    public ProjectTable() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectTable.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        // Name column
        name.setCellValueFactory((TableColumn.CellDataFeatures<Project, String> param)
                -> new SimpleStringProperty(param.getValue().getProperty(Project.PropertyName.NAME)));
        // Code column
        code.setCellValueFactory((TableColumn.CellDataFeatures<Project, String> param)
                -> new SimpleStringProperty(param.getValue().getProperty(Project.PropertyName.CODE)));
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        // ContextMenu
        setPlaceholder(new Label("Open or create a project."));
        MenuItem close = new MenuItem("Close project", new ImageView("exomesuite/img/cancel4.png"));
        MenuItem delete = new MenuItem("Delete project", new ImageView("exomesuite/img/delete.png"));
        final ContextMenu contextMenu = new ContextMenu(close, delete);
        setContextMenu(contextMenu);
        close.setOnAction(event -> closeProject());
        delete.setOnAction(event -> deleteProject());
    }

    private void closeProject() {
        getItems().remove(getSelectionModel().getSelectedIndex());
    }

    private void deleteProject() {
        Project project = getSelectionModel().getSelectedItem();
        if (project != null) {
            // Ask user to remove folder content
            File path = new File(project.getProperty(Project.PropertyName.PATH));
            File config = project.getConfigFile();
            Action showConfirm = Dialogs.create()
                    .message("Do you want to delete everything under " + path + "?")
                    .showConfirm();
            if (showConfirm == Dialog.ACTION_NO) {
                config.delete();
                getItems().remove(getSelectionModel().getSelectedIndex());
            } else if (showConfirm == Dialog.ACTION_YES) {
                config.delete();
                delete(path, true);
                getItems().remove(getSelectionModel().getSelectedIndex());
            }
        }
    }

    private boolean delete(File file, boolean recursive) {
        if (recursive) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    if (!delete(f, recursive)) {
                        return false;
                    }
                }
                return true;
            } else {
                return file.delete();
            }
        } else {
            return file.delete();
        }
    }
}
