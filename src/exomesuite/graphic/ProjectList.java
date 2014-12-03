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
import exomesuite.utils.OS;
import java.io.File;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ProjectList extends ListView<Project> {

    public ProjectList() {
        initialize();
    }

    private void initialize() {
        setPlaceholder(new Label("Open or create a project."));
        // Context menu
        MenuItem close = new MenuItem("Close", new SizableImage("exomesuite/img/cancel.png", 16));
        MenuItem delete = new MenuItem("Delete", new SizableImage("exomesuite/img/delete.png", 16));
        close.setOnAction(event -> close(getSelectionModel().getSelectedItem()));
        delete.setOnAction(event -> delete(getSelectionModel().getSelectedItem()));
        final ContextMenu contextMenu = new ContextMenu(close, delete);
        setContextMenu(contextMenu);
        // Cell factory
        setEditable(false);
    }

    /**
     * Removes from list the given project
     *
     * @param project the project to remove from list
     */
    private void close(Project project) {
        getItems().remove(project);
        OS.removeProject(project);
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
            Dialog.Response response = new Dialog().showYesNoCancel("Delete content",
                    "Do you also want to delete everything under" + path + "?",
                    "Delete everything", "Delete only project", "Cancel");
            switch (response) {
                case YES:
                    FileManager.delete(path, true);
                case NO:
                    config.delete();
                    getItems().remove(project);
                    OS.removeProject(project);
            }
        }
    }

}
