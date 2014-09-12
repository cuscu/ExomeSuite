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
import exomesuite.utils.Config;
import exomesuite.utils.Download;
import exomesuite.utils.OS;
import exomesuite.vcfreader.CombineVariants;
import exomesuite.vcfreader.VCFReader;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
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

    /**
     * The config of the application.
     */
    private static Config config;

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
    @FXML
    private ProgressBar progressBar;

    private static ProgressBar staticBar;
    /**
     * The table where all the opened projects are listed.
     */
    @FXML
    private ProjectTable projectTable;
    /**
     * Current project properties.
     */
//    @FXML
//    private ProjectProperties projectProperties;
    @FXML
    private Label info;
    @FXML
    private ProgressBar progress;
    @FXML
    private ProjectActions projectActions;
    @FXML
    private ProjectInfo projectInfo;

    public MainViewController() {
        staticBar = progressBar;
    }

    /**
     * Puts into the {@code tabPane} the open Button, new Button and Databases Button.
     */
    public void initialize() {
        config = new Config(new File("exomesuite.config"));
        setMenus();
        setToolBar();
        projectTable.getSelectionModel().selectedItemProperty().addListener((
                ObservableValue<? extends Project> observable,
                Project oldValue, Project newValue) -> {
            if (newValue != null) {
//                projectProperties.setProject(newValue);
                projectActions.setProject(newValue);
                projectInfo.setProject(newValue);
            }
        });
        FlatButton download = new FlatButton("download.png", "Download something");
//        actionButtons.getChildren().add(download);
        download.setOnAction((ActionEvent event) -> {
            downloadSomething();
        });
    }

    private void downloadSomething() {
        Task genome = new Task() {

            @Override
            protected Object call() throws Exception {
                String[] genomeFiles = {
                    "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
                    "16", "17", "18", "19", "20", "21", "22", "X", "Y"};
                String ftpserver = "ftp://ftp.ncbi.nlm.nih.gov/genbank/genomes";
                String ftpGenome = "/Eukaryotes/vertebrates_mammals/Homo_sapiens/GRCh38";
                String ftpFASTA = "/Primary_Assembly/assembled_chromosomes/FASTA/";
                final String ftpLink = ftpserver + ftpGenome + ftpFASTA;
                for (int i = 0; i < 3; i++) {
                    String chr = "chr" + genomeFiles[i] + ".fa.gz";
                    File f = new File(chr);
                    final String ftp = ftpLink + chr;
                    Download download = new Download(ftp, f);
                    info.textProperty().bind(download.messageProperty());
                    progress.progressProperty().bind(download.progressProperty());
                    Thread th = new Thread(download);
                    th.start();
                }
                return null;
            }
        };
        new Thread(genome).start();
    }

    /**
     * Opens a FileChooser and lets the user open a .config file. If returned file is not null, it
     * will call {@code addProjectTab}.
     */
    private void openProject() {
        File f = OS.openFile("Config file", OS.CONFIG_FILTER);
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
        databaseMenu.setOnAction((ActionEvent event) -> showDatabasesPane());
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
                System.out.println("Hi");
                String name = controller.getName();
                String code = controller.getCode();
                File path = controller.getPath();
                if (!name.isEmpty() && !code.isEmpty() && path != null) {
                    Project project = new Project(name, code, path.getAbsolutePath());
                    File forward = controller.getForward();
                    File reverse = controller.getReverse();
                    if (forward != null && reverse != null) {
                        project.setProperty(Project.PropertyName.FORWARD_FASTQ, forward.
                                getAbsolutePath());
                        project.setProperty(Project.PropertyName.REVERSE_FASTQ, reverse.
                                getAbsolutePath());
                    }
                    String reference = controller.getReference();
                    if (reference != null) {
                        project.setProperty(Project.PropertyName.REFERENCE_GENOME, reference);
                    }
                    projectTable.getItems().add(project);
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
        File f = OS.openFile("Choose any file", OS.MIST_FILTER, OS.TSV_FILTER, OS.ALL_FILTER);
        if (f != null) {
            new TSVReader(f).show();
        }
    }

    private void openVCF() {
        File f = OS.openFile("Select a VCF file", OS.VCF_FILTER);
        if (f != null) {
            new VCFReader(f).show();
        }
    }

    public static ProgressBar getProgressBar() {
        return staticBar;
    }

}
