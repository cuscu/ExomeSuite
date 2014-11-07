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
import exomesuite.utils.FileManager;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ProjectList extends ListView<Project> {

    /**
     * Custom control standard constructor
     */
    public ProjectList() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectList.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void initialize() {
        setPlaceholder(new Label("Open or create a project."));
        // Context menu
        MenuItem close = new MenuItem("Close project", new ImageView("exomesuite/img/cancel4.png"));
        MenuItem delete = new MenuItem("Delete project", new ImageView("exomesuite/img/delete.png"));
        close.setOnAction(event -> close(getSelectionModel().getSelectedItem()));
        delete.setOnAction(event -> delete(getSelectionModel().getSelectedItem()));
        final ContextMenu contextMenu = new ContextMenu(close, delete);
        setContextMenu(contextMenu);
        // Cell factory
        setCellFactory(p -> new MyCell());
        setEditable(false);
    }

    /**
     * Removes from list the given project
     *
     * @param project the project to remove from list
     */
    private void close(Project project) {
        getItems().remove(project);
    }

    /**
     * Deletes the selected project and asks user for removing the whole folder.
     *
     * @param project
     */
    private void delete(Project project) {
        if (project != null) {
            // Ask user to remove folder content
            File path = new File(project.getProperty(Project.PropertyName.PATH));
            File config = project.getConfigFile();
            Action showConfirm = Dialogs.create()
                    .message("Do you want to delete everything under " + path + "?")
                    .showConfirm();
            if (showConfirm == Dialog.ACTION_NO) {
                config.delete();
                getItems().remove(project);
            } else if (showConfirm == Dialog.ACTION_YES) {
                config.delete();
                FileManager.delete(path, true);
                getItems().remove(project);
            }
        }
    }

    /**
     * Cells for the list, they include buttons to delete or close the projects.
     */
    private class MyCell extends ListCell<Project> {

        private final FlatButton delete = new FlatButton("delete.png", "Delete project");
        private final FlatButton close = new FlatButton("cancel4.png", "Close project");
        private final HBox box = new HBox(close, delete);
        private Project project;

        public MyCell() {
            setContentDisplay(ContentDisplay.LEFT);
            box.setAlignment(Pos.CENTER_RIGHT);
            box.setMaxWidth(Double.MAX_VALUE);
            setMaxWidth(Double.MAX_VALUE);
            delete.setOnAction(event -> delete(project));
            close.setOnAction(event -> close(project));
        }

        @Override
        protected void updateItem(Project project, boolean empty) {
            super.updateItem(project, empty);
            this.project = project;
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                setText(project.getProperty(Project.PropertyName.NAME));
                setGraphic(box);
                project.addListener(e -> {
                    if (getItem() == project) {
                        setText(project.getProperty(Project.PropertyName.NAME));
                    }
                });
            }
        }

    }
}