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
import exomesuite.utils.FileManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ButtonsBar extends FlowPane {

    @FXML
    private Button openFile;
    @FXML
    private Button openProject;
    @FXML
    private Button newProject;
    @FXML
    private Button openDatabases;

    public ButtonsBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ButtonsBar.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            MainViewController.printException(e);
        }
    }

    @FXML
    private void initialize() {
        openFile.setOnAction(e -> ExomeSuite.getController().openFile());
        openProject.setOnAction(e -> ExomeSuite.getController().openProject(
                FileManager.openFile("Select a config file", FileManager.CONFIG_FILTER)));
        openDatabases.setOnAction(e -> ExomeSuite.getController().showDatabasesPane());
        newProject.setOnAction(e -> ExomeSuite.getController().showNewPane());
        openProject.setGraphic(new SizableImage("exomesuite/img/open.png", 32));
        openProject.setTooltip(new Tooltip("Open project... Ctrl+O"));
        newProject.setGraphic(new SizableImage("exomesuite/img/add.png", 32));
        newProject.setTooltip(new Tooltip("New project... Ctrl+N"));
        openDatabases.setGraphic(new SizableImage("exomesuite/img/database.png", 32));
        openDatabases.setTooltip(new Tooltip("Databases... Ctrl+D"));
        openFile.setGraphic(new SizableImage("exomesuite/img/file.png", 32));
        openFile.setTooltip(new Tooltip("Open file"));
    }

}
