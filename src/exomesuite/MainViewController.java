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

import exomesuite.graphic.Databases;
import exomesuite.graphic.FlatButton;
import exomesuite.graphic.ProjectActions;
import exomesuite.graphic.ProjectInfo;
import exomesuite.graphic.ProjectTable;
import exomesuite.graphic.ToolBarButton;
import exomesuite.project.Project;
import exomesuite.tsvreader.TSVReader;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import exomesuite.vcfreader.CombineVariants;
import exomesuite.vcfreader.VCFReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller class for the main window. Manages projects, adds tabs to the
 *
 * @author Pascual Lorente Arencibia
 */
public class MainViewController {

    @FXML
    private MenuItem openMenu;
    @FXML
    private MenuItem newMenu;
    @FXML
    private MenuItem databaseMenu;
    @FXML
    private FlowPane toolBar;
    @FXML
    private MenuItem openTSV;
    @FXML
    private MenuItem openVCFMenu;
    @FXML
    private MenuItem combineVCFMenu;
    /**
     * The table where all the opened projects are listed.
     */
    @FXML
    private ProjectTable projectTable;
    /**
     * Current project properties.
     */
    @FXML
    private Label info;
    @FXML
    private ProgressBar progress;
    @FXML
    private ProjectActions projectActions;
    @FXML
    private ProjectInfo projectInfo;
    @FXML
    private TabPane workingArea;

    private static TabPane staticWorkingArea;
    private static Label infoLabel;
    private static ProgressBar mainProgress;

    /**
     * Puts into the {@code tabPane} the open Button, new Button and Databases Button.
     */
    public void initialize() {
        setMenus();
        setToolBar();
        progress.setProgress(0);
        projectTable.getSelectionModel().selectedItemProperty().addListener((
                ObservableValue<? extends Project> observable,
                Project oldValue, Project newValue) -> {
            projectActions.setProject(newValue);
            projectInfo.setProject(newValue);
        });
        mainProgress = progress;
        infoLabel = info;
        staticWorkingArea = workingArea;
        FlatButton download = new FlatButton("download.png", "Download something");
//        actionButtons.getChildren().add(download);
        download.setOnAction((ActionEvent event) -> {
            OS.downloadSomething(this);
        });
    }

    /**
     * Opens a FileChooser and lets the user open a .config file. If returned file is not null, it
     * will call {@code addProjectTab}.
     */
    private void openProject() {
        File f = FileManager.openFile("Config file", FileManager.CONFIG_FILTER);
        if (f == null) {
            return;
        }
        Project project = new Project(f);
        if (!projectTable.getItems().contains(project)) {
            projectTable.getItems().add(project);
            projectTable.getSelectionModel().select(project);
//            projectProperties.setProject(project);
            projectActions.setProject(project);
            projectInfo.setProject(project);
        }
    }

    /**
     * Checks if all projects can be close. Iterates over projectList, if any of the projects
     * couldn't be closed, returns false.
     *
     * @return true if all projects can be closed, false otherwise.
     */
    boolean canClose() {
        return true;
//        AtomicBoolean exit = new AtomicBoolean(true);
//        projectList.forEach((Project project) -> {
//            if (!project.close()) {
//                exit.set(false);
//            }
//        });
//        return true;
    }

    /**
     * Gets a Vbox with a TextField for each database.
     *
     * @return the vbox
     */
    private VBox getDatabasesView() {
//        return new VBox(new Databases(), new GenomeManager().getView());
        return new Databases();
    }

    /**
     * Prepares the menus.
     */
    private void setMenus() {
        // Open menu
        openMenu.setOnAction((ActionEvent event) -> openProject());
        openMenu.setGraphic(new ImageView("exomesuite/img/open.png"));
        openMenu.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        // New menu
        newMenu.setOnAction((ActionEvent event) -> showNewPane());
        newMenu.setGraphic(new ImageView("exomesuite/img/add.png"));
        newMenu.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        // Databases menu
        databaseMenu.setOnAction(event -> showDatabasesPane());
        databaseMenu.setGraphic(new ImageView("exomesuite/img/database.png"));
        databaseMenu.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        // Open TSV
        openTSV.setOnAction((ActionEvent event) -> openTSV());
        // VCF menu
        openVCFMenu.setOnAction((ActionEvent event) -> openVCF());
        combineVCFMenu.setOnAction((ActionEvent event) -> combineVCF());

    }

    private void setToolBar() {
        Button open = new ToolBarButton("open.png", "Open project... Ctrl+O", "Open");
        open.setOnAction((ActionEvent event) -> openProject());
        Button newProject = new ToolBarButton("add.png", "New project... Ctrl+N", "New");
        newProject.setOnAction((ActionEvent event) -> showNewPane());
        Button db = new ToolBarButton("database.png", "Select databases... Ctrl+D", "Databases");
        db.setOnAction((ActionEvent event) -> showDatabasesPane());

        toolBar.getChildren().addAll(open, newProject, db);
    }

    private void showNewPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NewProjectView.fxml"));
            loader.load();
            NewProjectViewController controller = loader.getController();
            Stage stage = new Stage();
            Scene scen = new Scene(loader.getRoot());
            stage.setScene(scen);
            stage.setTitle("Create new project");
            stage.initModality(Modality.APPLICATION_MODAL);
            controller.setHandler((EventHandler) (Event event) -> {
                String name = controller.getName();
                String code = controller.getCode();
                String path = controller.getPath();
                if (!name.isEmpty() && !code.isEmpty() && !path.isEmpty()) {
                    Project project = new Project(name, code, path);
                    String forward = controller.getForward();
                    String reverse = controller.getReverse();
                    if (!forward.isEmpty() && !reverse.isEmpty()) {
                        project.setProperty(Project.PropertyName.FORWARD_FASTQ, forward);
                        project.setProperty(Project.PropertyName.REVERSE_FASTQ, reverse);
                    }
                    String reference = controller.getReference();
                    if (reference != null && !reference.isEmpty()) {
                        project.setProperty(Project.PropertyName.REFERENCE_GENOME, reference);
                    }
                    String encoding = controller.getEncoding();
                    if (encoding != null) {
                        project.setProperty(Project.PropertyName.FASTQ_ENCODING, encoding);
                    }
                    projectTable.getItems().add(project);
                    projectTable.getSelectionModel().select(project);
                    stage.close();
                }
            });
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showDatabasesPane() {
        Databases db = new Databases();
        ScrollPane pane = new ScrollPane(db);
        db.setPadding(new Insets(5));
        pane.setFitToHeight(true);
        pane.setFitToWidth(true);
//        Scene scene = new Scene(new ScrollPane(getDatabasesView()));
        Scene scene = new Scene(pane);
        Stage stage = new Stage();
        stage.setWidth(800);
        stage.setHeight(400);
        stage.setScene(scene);
        stage.setTitle("Databases manager");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void combineVCF() {
        new CombineVariants().show();

    }

    private void openTSV() {
        File f = FileManager.openFile("Choose any file", FileManager.MIST_FILTER,
                FileManager.TSV_FILTER,
                FileManager.ALL_FILTER);
        if (f != null) {
///            new TSVReader(f).show();
            Tab t = new Tab(f.getName());
            t.setContent(new TSVReader(f).get());
            workingArea.getTabs().add(t);
        }
    }

    private void openVCF() {
        File f = FileManager.openFile("Select a VCF file", FileManager.VCF_FILTER);
        if (f != null) {
            new VCFReader(f).show();
        }
    }

    public static TabPane getWorkingArea() {
        return staticWorkingArea;
    }

    public static Label getInfo() {
        return infoLabel;
    }

    public ProgressBar getProgress() {
        return mainProgress;
    }

}
