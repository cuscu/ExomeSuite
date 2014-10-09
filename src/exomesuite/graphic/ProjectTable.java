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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

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
    @FXML
    private TableColumn<Project, Project> actions;

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
        // Actions
        actions.setCellValueFactory((
                TableColumn.CellDataFeatures<Project, Project> param)
                -> new SimpleObjectProperty<>(param.getValue()));
        actions.setCellFactory((TableColumn<Project, Project> param) -> new ActionsCell());
        setPlaceholder(new Label("Open or create a project."));
    }

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

    private class ActionsCell extends TableCell<Project, Project> {

        private final HBox box;
        private final FlatButton close, delete;

        public ActionsCell() {
            close = new FlatButton("cancel4.png", "Close project");
            delete = new FlatButton("delete.png", "Delete project");
            close.setOnAction((ActionEvent event) -> close());
            delete.setOnAction((ActionEvent event) -> delete());
            box = new HBox(5, close, delete);
        }

        @Override
        protected void updateItem(Project item, boolean empty) {
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(box);
            }
        }

        private void delete() {

        }

        private void close() {
            System.out.println("Close " + getIndex());
            getTableView().getItems().remove(getIndex());
        }

    }
}
