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

import exomesuite.bam.BamReader;
import exomesuite.graphic.About;
import exomesuite.graphic.CombineMIST;
import exomesuite.graphic.Databases;
import exomesuite.graphic.FlatButton;
import exomesuite.graphic.ProjectActions;
import exomesuite.graphic.ProjectInfo;
import exomesuite.graphic.ProjectList;
import exomesuite.project.Project;
import exomesuite.tsvreader.TSVReader;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import exomesuite.vcf.CombineVariants;
import exomesuite.vcf.VCFTable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller class for the main window. Manages projects (open, close, create, delete...), adds
 * tabs to the rigth panel.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
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
    private MenuItem about;
    @FXML
    private ProjectList projectList;
    /**
     * Current project properties.
     */
    @FXML
    private Label info;
    @FXML
    private HBox infoBox;
    @FXML
    private ProjectActions projectActions;
    @FXML
    private ProjectInfo projectInfo;
    @FXML
    private TabPane workingArea;

    private static TabPane staticWorkingArea;
    private static Label infoLabel;
    private static HBox infoHBox;
    @FXML
    private BorderPane root;

    /**
     * Puts into the {@code tabPane} the open Button, new Button and Databases Button.
     */
    public void initialize() {
        setMenus();
        setToolBar();
        projectList.getSelectionModel().selectedItemProperty().addListener((
                ObservableValue<? extends Project> observable, Project oldValue, Project newValue)
                -> {
                    projectActions.setProject(newValue);
                    projectInfo.setProject(newValue);
                });
        infoLabel = info;
        staticWorkingArea = workingArea;
        infoHBox = infoBox;
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
        printMessage("Project " + project.getProperty(Project.PropertyName.NAME) + " opened", "INFO");
    }

    /**
     * Checks if all projects can be close. Iterates over projectList, if any of the projects
     * couldn't be closed, returns false.
     *
     */
    public void exitApplication() {
        Button exit = new Button("Exit", new ImageView("exomesuite/img/exit.png"));
        Button cont = new Button("Continue", new ImageView("exomesuite/img/cancel.png"));
        infoBox.getChildren().setAll(infoLabel, exit, cont);
        printMessage("Are you sure you want to exit?", "info");
        root.getTop().setDisable(true);
        root.getCenter().setDisable(true);
        exit.setOnAction(e -> {
            String projects = "";
            projects = projectList.getItems().stream().
                    map((p) -> p.getConfigFile().getAbsolutePath() + ";").reduce(projects,
                            String::concat);
            OS.setProperty("projects", projects);
            ExomeSuite.getMainStage().close();
        });
        cont.setOnAction(e -> {
            infoBox.getChildren().setAll(infoLabel);
            printMessage("Good, keep working", "success");
            root.getTop().setDisable(false);
            root.getCenter().setDisable(false);
        });
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
        openFile.setGraphic(new ImageView("exomesuite/img/file.png"));
        openFile.setOnAction(e -> openFile());
        // VCF menu
        combineVCFMenu.setOnAction(e -> combineVCF());
        combineVCFMenu.setGraphic(new ImageView("exomesuite/img/documents_vcf.png"));
        // Mist Menu
        intersectMIST.setOnAction(e -> combineMIST());
        intersectMIST.setGraphic(new ImageView("exomesuite/img/documents_mist.png"));
        // About
        about.setOnAction(e -> showAbout());
    }

    /**
     * Fills the top toolbar with quick access buttons.
     */
    private void setToolBar() {
        FlatButton open = new FlatButton("open.png", "Open project... Ctrl+O");
        open.setOnAction(e -> openProject(FileManager.openFile("Config file",
                FileManager.CONFIG_FILTER)));
        FlatButton newProject = new FlatButton("add.png", "New project... Ctrl+N");
        newProject.setOnAction(e -> showNewPane());
        FlatButton db = new FlatButton("database.png", "Select databases... Ctrl+D");
        db.setOnAction(e -> showDatabasesPane());
        FlatButton fileOpen = new FlatButton("file.png", "Open file");
        fileOpen.setOnAction(e -> openFile());
        Separator s = new Separator(Orientation.VERTICAL);
        Separator s2 = new Separator(Orientation.VERTICAL);
        toolBar.getChildren().addAll(fileOpen, s, open, newProject, s2, db);
    }

    /**
     * Opens dialog to create a new project.
     */
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
            printMessage("Problem loading new project pane", "error");
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Opens dialog to set databases selection.
     */
    private void showDatabasesPane() {
        Databases db = new Databases();
        ScrollPane pane = new ScrollPane(db);
        db.setPadding(new Insets(5));
        pane.setFitToHeight(true);
        pane.setFitToWidth(true);
        Scene scene = new Scene(pane);
        Stage stage = new Stage();
        stage.setWidth(800);
        stage.setHeight(400);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);
        stage.setTitle("Databases manager");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private void openFile() {
        File f = FileManager.openFile("Choose any file", FileManager.ALL_FILTER);
        showFileContent(f, null);
    }

    /**
     * Tries to open the file in the editor. If the file is already opened, it just selects its tab.
     * If the file does not exist does nothing. Depending on the extension of the file, it will use
     * TSVReader or VCFReader.
     *
     * @param file the file to open
     * @param secondary a second file if needed
     */
    public static void showFileContent(File file, File secondary) {
        if (file != null) {
            /* Check if the file is already opened */
            for (Tab t : staticWorkingArea.getTabs()) {
                if (t.getText().equals(file.getName())) {
                    staticWorkingArea.getSelectionModel().select(t);
                    return;
                }
            }
            /* Select the class depending on the extension */
            Tab t = new Tab(file.getName());
            if (file.getName().endsWith(".tsv") || file.getName().endsWith(".mist")) {
                t.setContent(new TSVReader(file).get());
            } else if (file.getName().endsWith(".vcf")) {
                VCFTable table = new VCFTable();
                table.setFile(file);
                t.setContent(table);
                //t.setContent(new VCFReader(file).getView());
            } else if (file.getName().endsWith(".bam")) {
                if (secondary == null) {
                    secondary = FileManager.openFile("Select reference genome", FileManager.FASTA_FILTER);
                }
                t.setContent(new BamReader(file, secondary));
            } else {
                printMessage("File extension not compatible (" + file.getName() + ")", "warning");
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

    private void combineMIST() {
        try {
            FXMLLoader loader = new FXMLLoader(CombineMIST.class.getResource("CombineMIST.fxml"));
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            scene.getStylesheets().add("/exomesuite/main.css");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.initOwner(ExomeSuite.getMainStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Intersect MIST files");
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void combineVCF() {
        try {
            Double.valueOf("e");
        } catch (NumberFormatException e) {
            showException(e);
        }
        try {
            FXMLLoader loader = new FXMLLoader(CombineVariants.class.getResource("CombineVariants.fxml"));
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            scene.getStylesheets().add("/exomesuite/main.css");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.initOwner(ExomeSuite.getMainStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Intersect VCF files");
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(About.class.getResource("About.fxml"));
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
            stage.initOwner(ExomeSuite.getMainStage());
            stage.showAndWait();
        } catch (IOException e) {
            showException(e);
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Type must be one of INFO, ERROR, WARNING, SUCCESS, info, error, success and warning.
     *
     * @param message
     * @param type
     */
    public static void printMessage(String message, String type) {
        infoLabel.setText(message);
        infoLabel.getStyleClass().clear();
        infoHBox.getStyleClass().clear();
        infoLabel.getStyleClass().add(type.toLowerCase() + "-label");
        infoHBox.getStyleClass().add(type.toLowerCase() + "-box");
        if (type.equals("error")) {
            System.err.println(message);
        } else {
            System.out.println(message);
        }
    }

    public static void showException(Exception e) {
        infoLabel.setText(e.getClass() + " " + e.getMessage());
        infoLabel.getStyleClass().clear();
        infoHBox.getStyleClass().clear();
        infoLabel.getStyleClass().add("error-label");
        infoHBox.getStyleClass().add("error-box");
        TextArea area = new TextArea();
        area.getStyleClass().add("error-label");
        e.printStackTrace(new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                byte[] c = {(byte) b};
                final String character = new String(c, Charset.defaultCharset());
                Platform.runLater(() -> {
                    area.appendText(character);
                });
            }
        }));
        Button view = new Button("View details");
        view.setOnAction(event -> {
            Stage stage = new Stage();
            Scene scene = new Scene(area);
            stage.centerOnScreen();
            stage.setScene(scene);
            stage.showAndWait();
        });
        infoHBox.getChildren().setAll(infoLabel, view);
    }

}
