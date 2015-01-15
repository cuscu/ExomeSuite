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
import exomesuite.graphic.ButtonsBar;
import exomesuite.graphic.Databases;
import exomesuite.graphic.Dialog;
import exomesuite.graphic.PActions;
import exomesuite.graphic.ProjectInfo;
import exomesuite.graphic.ProjectList;
import exomesuite.graphic.SizableImage;
import exomesuite.mist.CombineMIST;
import exomesuite.project.Project;
import exomesuite.tsv.TSVReader;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import exomesuite.vcf.CombineVariants;
import exomesuite.vcf.VCFReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Controller class for the main window. Manages projects (open, close, create, delete...), adds
 * tabs to the rigth panel and prints messages, among other operations. This is GOD. For some
 * general methods related with app, but not GUI, use {@link OS}.
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
    private PActions pactions;
    @FXML
    private ProjectInfo projectInfo;
    @FXML
    private TabPane workingArea;

    private static TabPane staticWorkingArea;
    private static Label infoLabel;
    private static HBox infoHBox;
    @FXML
    private BorderPane root;
    @FXML
    private ButtonsBar toolbar;
    @FXML
    private Menu language;

    // My ResourceBundle
//    StringRepository repository = new StringRepository();
    /**
     * Puts into the {@code tabPane} the open Button, new Button and Databases Button.
     */
    public void initialize() {
        setMenus();
        projectList.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
//                    projectActions.setProject(newValue);
            pactions.setProject(newValue);
            projectInfo.setProject(newValue);
        });
        infoLabel = info;
        staticWorkingArea = workingArea;
        infoHBox = infoBox;
        // Open recently opened projects (.config files)
        final String openedProjects = OS.getProperties().getProperty("projects", "");
        if (!openedProjects.isEmpty()) {
            String[] op = openedProjects.split(";");
            for (String s : op) {
                openProject(new File(s));
            }
        }
        setLocales();
    }

    /**
     * Opens a FileChooser and lets the user open a .config file. If returned file is not null, it
     * will call {@code addProjectTab}.
     *
     * @param configFile the config file of the project
     */
    public void openProject(File configFile) {
        if (configFile == null || !configFile.exists()) {
            return;
        }
        Project project;
        try {
            project = new Project(configFile);
        } catch (Exception e) {
            printMessage(e.getMessage(), "error");
            return;
//            printException(e);
        }
        if (!projectList.getItems().contains(project)) {
            projectList.getItems().add(project);
            projectList.getSelectionModel().select(project);
        }
        String message = ExomeSuite.getStringFormatted("project.opened",
                project.getProperties().getProperty(Project.NAME));
        printMessage(message, "info");
        OS.addProject(project);
    }

    /**
     * Checks if all projects can be closed. Iterates over projectList, if any of the projects
     * couldn't be closed, returns false.
     *
     */
    public void exitApplication() {
        if (OS.getProperties().getProperty("confirm.exit", "true").equals("true")) {
            ResourceBundle resources = ExomeSuite.getResources();
            String title = resources.getString("exit.app.title");
            String question = resources.getString("exit.app.question");
            String yes = resources.getString("exit.app.yes");
            String no = resources.getString("exit.app.no");
            String never = resources.getString("exit.app.neverask");
            Dialog d = new Dialog();
            d.setYesIcon(new SizableImage("exomesuite/img/exit.png", SizableImage.SMALL_SIZE));
            Dialog.Response resp = d.showYesNoCancel(title, question, yes, never, no);
            if (resp == Dialog.Response.YES) {
                ExomeSuite.getMainStage().close();
            } else if (resp == Dialog.Response.CANCEL) {
                printMessage(resources.getString("keep.work"), "success");
            } else if (resp == Dialog.Response.NO) {
                OS.getProperties().setProperty("confirm.exit", "false");
                ExomeSuite.getMainStage().close();
            } else {
                // User closed dialog
                printMessage(resources.getString("keep.work"), "success");
            }
        } else {
            ExomeSuite.getMainStage().close();
        }
    }

    /**
     * Prepares the menus.
     */
    private void setMenus() {
        // Open menu
        openMenu.setOnAction(e -> openProject(FileManager.openFile(
                ExomeSuite.getResources().getString("config.file"), FileManager.CONFIG_FILTER)));
        openMenu.setGraphic(new SizableImage("exomesuite/img/open.png", SizableImage.SMALL_SIZE));
        openMenu.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        // New menu
        newMenu.setOnAction(e -> showNewPane());
        newMenu.setGraphic(new SizableImage("exomesuite/img/add.png", SizableImage.SMALL_SIZE));
        newMenu.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        // Databases menu
        databaseMenu.setOnAction(e -> showDatabasesPane());
        databaseMenu.setGraphic(new SizableImage("exomesuite/img/database.png", SizableImage.SMALL_SIZE));
        databaseMenu.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        // Open TSV
        openFile.setGraphic(new SizableImage("exomesuite/img/file.png", SizableImage.SMALL_SIZE));
        openFile.setOnAction(e -> openFile());
        // VCF menu
        combineVCFMenu.setOnAction(e -> combineVCF());
        combineVCFMenu.setGraphic(new SizableImage("exomesuite/img/documents_vcf.png", SizableImage.SMALL_SIZE));
        // Mist Menu
        intersectMIST.setOnAction(e -> combineMIST());
        intersectMIST.setGraphic(new SizableImage("exomesuite/img/documents_mist.png", SizableImage.SMALL_SIZE));
        // About
        about.setOnAction(e -> showAbout());
        language.setGraphic(new SizableImage("exomesuite/img/world.png", SizableImage.SMALL_SIZE));
    }

    /**
     * Opens dialog to create a new project.
     */
    public void showNewPane() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("NewProjectView.fxml"), ExomeSuite.getResources());
            loader.load();
            NewProjectViewController controller = loader.getController();
            Stage stage = new Stage();
            Scene scen = new Scene(loader.getRoot());
            stage.setScene(scen);
            stage.setTitle(ExomeSuite.getResources().getString("create.project"));
            stage.initModality(Modality.APPLICATION_MODAL);
            controller.setHandler(event -> {
                String name = controller.getName();
                String code = controller.getCode();
                File path = controller.getPath();
                // NUll check
                if (!name.isEmpty() && !code.isEmpty() && path.exists()) {
                    Project project = new Project(name, code, path);
                    File forward = controller.getForward();
                    File reverse = controller.getReverse();
                    if (forward != null && reverse != null) {
                        project.getProperties().setProperty(Project.FORWARD_FASTQ, forward.getAbsolutePath());
                        project.getProperties().setProperty(Project.REVERSE_FASTQ, reverse.getAbsolutePath());
                    }
                    String reference = controller.getReference();
                    if (reference != null && !reference.isEmpty()) {
                        project.getProperties().setProperty(Project.REFERENCE_GENOME, reference);
                    }
                    String encoding = controller.getEncoding();
                    if (encoding != null) {
                        project.getProperties().setProperty(Project.FASTQ_ENCODING, encoding);
                    }
                    projectList.getItems().add(project);
                    projectList.getSelectionModel().select(project);
                    stage.close();
                }
            });
            stage.showAndWait();
        } catch (IOException ex) {
            printException(ex);
        }
    }

    /**
     * Opens dialog to set databases selection.
     */
    public void showDatabasesPane() {
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
        stage.setTitle(ExomeSuite.getResources().getString("database.manager"));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    /**
     * Opens a dialog that let the user select any file, and tries to open it depending on its
     * extension.
     */
    public void openFile() {
        File f = FileManager.openFile(ExomeSuite.getResources().getString("choose.file"), FileManager.ALL_FILTER);
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
                //t.setContent(new TSVReader(file).get());
                TSVReader reader = new TSVReader(file);
//                reader.setFile(file);
                t.setContent(reader);
            } else if (file.getName().endsWith(".vcf")) {
                VCFReader table = new VCFReader(file);
                t.setContent(table);
                //t.setContent(new VCFReader(file).getView());
            } else if (file.getName().endsWith(".bam")) {
                if (secondary == null) {
                    secondary = FileManager.openFile(ExomeSuite.getResources().getString("select.genome"), FileManager.FASTA_FILTER);
                }
                t.setContent(new BamReader(file, secondary));
            } else {
                String message = ExomeSuite.getStringFormatted("extension.unsupported", file.getName());
                printMessage(message, "warning");
                return;
            }
            String message = ExomeSuite.getStringFormatted("file.opened", file.getAbsolutePath());
            printMessage(message, "success");
            staticWorkingArea.getTabs().add(t);
            staticWorkingArea.getSelectionModel().select(t);
        }
    }

    /**
     * Gets the center pane, where new tabs can be added.
     *
     * @return the TabPane of the working area
     */
    public static TabPane getWorkingArea() {
        return staticWorkingArea;
    }

    /**
     * Opens the dialog of combining MIST files.
     */
    private void combineMIST() {
        try {
            FXMLLoader loader = new FXMLLoader(CombineMIST.class.getResource("CombineMIST.fxml"), ExomeSuite.getResources());
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            scene.getStylesheets().add("/exomesuite/main.css");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.initOwner(ExomeSuite.getMainStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(ExomeSuite.getResources().getString("combine.mist"));
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Opens the dialog of combining VCF files.
     */
    private void combineVCF() {
        try {
            FXMLLoader loader = new FXMLLoader(CombineVariants.class.getResource("CombineVariants.fxml"), ExomeSuite.getResources());
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            scene.getStylesheets().add("/exomesuite/main.css");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.initOwner(ExomeSuite.getMainStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(ExomeSuite.getResources().getString("combine.vcf"));
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Shows the about panel.
     */
    private void showAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(About.class.getResource("About.fxml"), ExomeSuite.getResources());
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setAlwaysOnTop(true);
            stage.initOwner(ExomeSuite.getMainStage());
            stage.showAndWait();
        } catch (IOException e) {
            printException(e);
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /**
     * Type must be one of INFO, ERROR, WARNING, SUCCESS, info, error, success and warning.
     *
     * @param message the message to print
     * @param type one of the "info", "error", "warning" and "success" String
     */
    public static void printMessage(String message, String type) {
        infoLabel.setText(message);
        infoLabel.getStyleClass().clear();
        infoHBox.getStyleClass().clear();
        infoLabel.getStyleClass().add(type.toLowerCase() + "-label");
        infoHBox.getStyleClass().add(type.toLowerCase() + "-box");
        infoHBox.getChildren().setAll(infoLabel);
        if (type.equals("error")) {
            System.err.println(message);
        } else {
            System.out.println(message);
        }
    }

    /**
     * Prints an error message and allocates a 'View details' Button to see the whole stackTrace of
     * the exception.
     *
     * @param e the exeption to print
     */
    public static void printException(Exception e) {
        Platform.runLater(() -> {
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
            Button view = new Button(ExomeSuite.getResources().getString("view.details"));
            Stage stage = new Stage();
            Scene scene = new Scene(area);
            stage.setTitle(ExomeSuite.getResources().getString("exception"));
            stage.centerOnScreen();
            stage.setScene(scene);
            view.setOnAction(event -> stage.showAndWait());
            infoHBox.getChildren().setAll(infoLabel, view);
        });
    }

    /**
     * Fills the locales menu. Each locale is shown in its own locale: Español (España), English
     * (UK). If you want to display all languages in the current locale use
     * {@code getDisplayName(ExomeSuite.getLocale())}.
     */
    private void setLocales() {
        language.getItems().clear();
        OS.getAvailableLocales().forEach(locale -> {
            MenuItem mi = new MenuItem(locale.getDisplayName(locale));
            mi.setOnAction(event -> ExomeSuite.changeLocale(locale));
            language.getItems().add(mi);
        });
    }

}
