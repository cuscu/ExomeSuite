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

import exomesuite.project.ProjectData;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class ProjectTable extends TableView<ProjectData> {

    @FXML
    private TableColumn<ProjectData, String> name;
    @FXML
    private TableColumn<ProjectData, String> code;

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        // Name column
        name.setCellValueFactory((TableColumn.CellDataFeatures<ProjectData, String> param)
                -> new SimpleStringProperty(param.getValue().getProperty(
                                ProjectData.PropertyName.NAME)));
        // Code column
        code.setCellValueFactory((TableColumn.CellDataFeatures<ProjectData, String> param)
                -> new SimpleStringProperty(param.getValue().getProperty(
                                ProjectData.PropertyName.CODE)));
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
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

}
