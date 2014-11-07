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

import exomesuite.graphic.CombineMIST;
import exomesuite.graphic.Databases;
import exomesuite.graphic.ProjectActions;
import exomesuite.graphic.ProjectInfo;
import exomesuite.graphic.ProjectList;
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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 * Controller class for the main window. Manages projects (open, close, create, delete...), adds
 * tabs to the rigth panel.
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
    private MenuItem openFile;
    @FXML
    private MenuItem combineVCFMenu;
    @FXML
    private MenuItem intersectMIST;
    @FXML
    private ProjectList projectList;
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
        projectList.getSelectionModel().selectedItemProperty().addListener((
                ObservableValue<? extends Project> observable, Project oldValue, Project newValue)
                -> {
            projectActions.setProject(newValue);
            projectInfo.setProject(newValue);
        });
        mainProgress = progress;
        infoLabel = info;
        staticWorkingArea = workingArea;
        // Open recently opened projects
        final String openedProjects = OS.getProperty("projects");
        if (openedProjects != null && !openedProjects.isEmpty()) {
            String[] op = openedProjects.split(";");
            for (String s : op) {
                openProject(new File(s));
            }
        }
    }

    /**
     * Opens a FileChooser and lets the user open a .config file. If returned file is not null, it
     * will call {@code addProjectTab}.
     */
    private void openProject(File configFile) {
        if (configFile == null || !configFile.exists()) {
            return;
        }
        Project project = new Project(configFile);
        if (!projectList.getItems().contains(project)) {
            projectList.getItems().add(project);
            projectList.getSelectionModel().select(project);
        }
    }

    /**
     * Checks if all projects can be close. Iterates over projectList, if any of the projects
     * couldn't be closed, returns false.
     *
     * @return true if all projects can be closed, false otherwise.
     */
    boolean canClose() {
        Action showConfirm
                = Dialogs.create().title("We will miss you")
                .message("Are you sure you want to exit?").showConfirm();
        return showConfirm == Dialog.ACTION_YES;
//        AtomicBoolean exit = new AtomicBoolean(true);
//        projectList.forEach((Project project) -> {
//            if (!project.close()) {
//                exit.set(false);
//            }
//        });
//        return true;
    }

    /**
     * Prepares the menus.
     */
    private void setMenus() {
        // Open menu
        openMenu.setOnAction(e -> openProject(FileManager.openFile("Config file",
                FileManager.CONFIG_FILTER)));
        openMenu.setGraphic(new ImageView("exomesuite/img/open.png"));
        openMenu.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        // New menu
        newMenu.setOnAction(e -> showNewPane());
        newMenu.setGraphic(new ImageView("exomesuite/img/add.png"));
        newMenu.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        // Databases menu
        databaseMenu.setOnAction(e -> showDatabasesPane());
        databaseMenu.setGraphic(new ImageView("exomesuite/img/database.png"));
        databaseMenu.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        // Open TSV
        openFile.setOnAction(e -> openFile());
        // VCF menu
        combineVCFMenu.setOnAction(e -> combineVCF());
        // Mist Menu
        intersectMIST.setOnAction(e -> combineMIST());
    }

    private void setToolBar() {
        Button open = new ToolBarButton("open.png", "Open project... Ctrl+O", "Open");
        open.setOnAction(e -> openProject(FileManager.openFile("Config file",
                FileManager.CONFIG_FILTER)));
        Button newProject = new ToolBarButton("add.png", "New project... Ctrl+N", "New");
        newProject.setOnAction(e -> showNewPane());
        Button db = new ToolBarButton("database.png", "Select databases... Ctrl+D", "Databases");
        db.setOnAction(e -> showDatabasesPane());

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
                // NUll check
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
                    projectList.getItems().add(project);
                    projectList.getSelectionModel().select(project);
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
        stage.centerOnScreen();
        stage.setTitle("Databases manager");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void combineVCF() {
        new CombineVariants().show();

    }

    private void openFile() {
        File f = FileManager.openFile("Choose any file", FileManager.ALL_FILTER);
        showFileContent(f);
    }

    public static void showFileContent(File file) {
        if (file != null) {
            for (Tab t : staticWorkingArea.getTabs()) {
                if (t.getText().equals(file.getName())) {
                    staticWorkingArea.getSelectionModel().select(t);
                    return;
                }
            }
            Tab t = new Tab(file.getName());
            if (file.getName().endsWith(".tsv") || file.getName().endsWith(".mist")) {
                t.setContent(new TSVReader(file).get());
            } else if (file.getName().endsWith(".vcf")) {
                t.setContent(new VCFReader(file).getView());
                //VCFTable vCFTable = new VCFTable();
                //vCFTable.setFile(file);
                //t.setContent(vCFTable);
            } else {
                return;
            }
            staticWorkingArea.getTabs().add(t);
            staticWorkingArea.getSelectionModel().select(t);
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

    void closeWindow() {
        String projects = "";
        projects = projectList.getItems().stream().
                map((p) -> p.getConfigFile().getAbsolutePath() + ";").reduce(projects,
                        String::concat);
        OS.setProperty("projects", projects);
    }

    private void combineMIST() {
        try {
            FXMLLoader loader = new FXMLLoader(CombineMIST.class.getResource("CombineMIST.fxml"));
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            scene.getStylesheets().add("/exomesuite/main.css");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Intersect MIST files");
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
