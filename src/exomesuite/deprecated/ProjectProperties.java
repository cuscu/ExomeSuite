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
package exomesuite.deprecated;

import exomesuite.graphic.ProjectTable;
import exomesuite.graphic.PropertyCell;
import exomesuite.project.Project;
import exomesuite.project.Project.PropertyName;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
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
public class ProjectProperties extends TableView<Map.Entry<PropertyName, String>> {

    @FXML
    private TableColumn<Map.Entry<PropertyName, String>, PropertyName> value;
    @FXML
    private TableColumn<Map.Entry<PropertyName, String>, String> name;

    private Project project;

    public ProjectProperties() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectProperties.fxml"));
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
        name.setCellValueFactory((
                TableColumn.CellDataFeatures<Map.Entry<PropertyName, String>, String> param)
                -> new SimpleStringProperty(param.getValue().getKey().toString()));
        // To clarify, this method takes a row from the table,
        // which contains a Map.Entry<PropertyName, String>>
        // and returns a PropertyName, id est, the key of the Map.Entry
        // nothing else
        value.setCellValueFactory((
                TableColumn.CellDataFeatures<Map.Entry<PropertyName, String>, PropertyName> param)
                -> new SimpleObjectProperty<>(param.getValue().getKey()));
        // This other methods takes a row from the table
        // and display it on this cell
        value.setCellFactory((TableColumn<Map.Entry<PropertyName, String>, PropertyName> param)
                -> new PropertyCell(project));
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setPlaceholder(new Label("Select any project to see its properties."));
    }

    public void setProject(Project project) {
        this.project = project;
        getItems().clear();
        for (PropertyName pname : PropertyName.values()) {
            String val = project.getProperties().getProperty(pname.toString(), "");
            getItems().add(new AbstractMap.SimpleEntry<>(pname, val));
        }
        value.setCellFactory((TableColumn<Map.Entry<PropertyName, String>, PropertyName> param)
                -> new PropertyCell(project));
    }

}
