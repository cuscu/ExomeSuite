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
package exomesuite;

import exomesuite.phase.Databases;
import exomesuite.phase.GenomeManager;
import exomesuite.utils.Config;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller class for the main window. Manages projects, adds tabs to the
 *
 * @author Pascual Lorente Arencibia
 */
public class MainViewController {

    /**
     * The config of the project.
     */
    private static Config config;

    /**
     * The TabPane where all the projects views are put. Into the tabPane there are only Nodes. The
     * {@link Project} are stored into projectList.
     */
    @FXML
    private TabPane projects;

    /**
     * An ArrayList to store all the opened projects.
     */
    private final List<Project> projectList = new ArrayList<>();

    /**
     * Puts into the {@code tabPane} the open Button, new Button and Databases Button.
     */
    public void initialize() {
        addOpenButton();
        addNewButton();
        addDatabaseButton();
    }

    /**
     * Initialize the tab to open projects.
     */
    private void addOpenButton() {
        config = new Config(new File("exomesuite.config"));
        final Tab newTab = new Tab();
        Button openButton = new Button(null, new ImageView("exomesuite/img/open.png"));
        newTab.setGraphic(openButton);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("WelcomeView.fxml"));
            loader.load();
            newTab.setContent(loader.getRoot());
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
        newTab.setClosable(false);
        projects.getTabs().add(newTab);
        openButton.setOnAction((ActionEvent event) -> {
            openProject();
        });
    }

    /**
     * Initialize the tab to select databases.
     */
    private void addDatabaseButton() {
        final Tab tab = new Tab();
        tab.setGraphic(new ImageView("exomesuite/img/database.png"));
        projects.getTabs().add(tab);
        tab.setClosable(false);
        tab.setContent(new VBox(new Databases().getView(), new GenomeManager().getView()));
    }

    /**
     * initialize the tab to add new project.
     */
    private void addNewButton() {
        final Tab tab = new Tab();
        tab.setGraphic(new ImageView("exomesuite/img/add.png"));
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NewProjectView.fxml"));
            loader.load();
            NewProjectViewController controller = loader.getController();
            controller.getAcceptButton().setOnAction((ActionEvent event) -> {
                String name = controller.getName();
                File path = controller.getPath();
                if (!name.isEmpty() && !(path == null)) {
                    addProjectTab(name, path);
                    controller.clear();
                }
            });
            tab.setContent(loader.getRoot());
            tab.setClosable(false);
            projects.getTabs().add(tab);
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Opens a FileChooser and lets the user open a .config file. If returned file is not null, it
     * will call {@code addProjectTab}.
     */
    private void openProject() {
        String[] filters = {"*.config"};
        File f = OS.openFile("Config file", ".config", filters);
        if (f == null) {
            return;
        }
        File path = f.getParentFile().getParentFile();
        String name = f.getParentFile().getName();
        addProjectTab(name, path);

    }

    /**
     * Creates a project. Adds the project view to a new tab in the tabPane and the project to
     * projectList.
     *
     * @param name The name of the project.
     * @param path The path where the project must be stored.
     */
    private void addProjectTab(String name, File path) {
        final Tab tab = new Tab(name);
        projects.getTabs().add(0, tab);
        Project project = new Project(name, path);
        tab.setContent(project.getToolsPane());
        tab.setOnCloseRequest((Event event) -> {
            if (!project.close()) {
                event.consume();
            }
        });
        projectList.add(project);
        projects.getSelectionModel().select(tab);
    }

    /**
     * Gets the Config of this project.
     *
     * @return the Config value of this project.
     */
    public static Config getConfig() {
        return config;
    }

    /**
     * Checks if all projects can be close. Iterates over projectList, if any of the projects
     * couldn't be closed, returns false.
     *
     * @return false if all projects can be closed, true otherwise.
     */
    boolean canClose() {
        for (Project project : projectList) {
            if (!project.close()) {
                return false;
            }
        }
        return true;
    }
}
