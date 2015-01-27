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
import exomesuite.graphic.Databases;
import exomesuite.graphic.Dialog;
import exomesuite.graphic.PActions;
import exomesuite.graphic.ProjectInfo;
import exomesuite.graphic.SizableImage;
import exomesuite.mist.CombineMIST;
import exomesuite.project.ModelProject;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.FlowPane;
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
    private Label info;
    @FXML
    private HBox infoBox;
    @FXML
    private PActions pactions;
    @FXML
    private ProjectInfo projectInfo;
    @FXML
    private TabPane workingArea;
    @FXML
    private Menu language;
    @FXML
    private ListView<ModelProject> projectListView;

    private final static DateFormat df = new SimpleDateFormat("HH:mm:ss");
    private static TabPane staticWorkingArea;
    private static Label infoLabel;
    private static HBox infoHBox;

    /**
     * Puts into the {@code tabPane} the open Button, new Button and Databases Button.
     */
    @FXML
    public void initialize() {
        setMenus();
        infoLabel = info;
        staticWorkingArea = workingArea;
        infoHBox = infoBox;
        setLocales();
        setProjectsList();
    }

    /**
     * Opens the project in configFile. configFile must exist.
     *
     * @param configFile the config file of the project
     */
    public void openProject(File configFile) {
        if (configFile != null && configFile.exists()) {
            ModelProject project = new ModelProject(configFile);
            boolean opened = false;
            for (ModelProject pr : OS.getProjects()) {
                if (pr.getCode().equals(project.getCode())) {
                    opened = true;
                    break;
                }
            }
            if (!opened) {
                OS.getProjects().add(new ModelProject(configFile));
                String message = ExomeSuite.getStringFormatted("project.opened",
                        project.getName());
                printMessage(message, "info");
            }
        }
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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("NewProjectView.fxml"), ExomeSuite.getResources());
        try {
            loader.load();
        } catch (IOException ex) {
            printException(ex);
        }
        Stage stage = new Stage();
        Scene scen = new Scene(loader.getRoot());
        stage.setScene(scen);
        stage.setTitle(ExomeSuite.getResources().getString("create.project"));
        stage.initModality(Modality.APPLICATION_MODAL);
        NewProjectView controller = loader.getController();
        controller.setHandler(event -> stage.close());
        stage.showAndWait();

        if (controller.isAccepted()) {
            String name = controller.getName();
            String code = controller.getCode();
            String path = controller.getPath();
            String forward = controller.getForward();
            String reverse = controller.getReverse();
            String genome = controller.getGenome();
            String encoding = controller.getEncoding();
            // NUll check
            ModelProject pr = new ModelProject(new File(path, code + ".config"));
            OS.getProjects().add(pr);
            pr.setCode(code);
            pr.setName(name);
            if (forward != null && !forward.isEmpty()) {
                pr.setForwardSequences(new File(forward));
            }
            if (reverse != null && !reverse.isEmpty()) {
                pr.setReverseSequences(new File(reverse));
            }
            if (genome != null && !genome.isEmpty()) {
                pr.setGenomeCode(genome);
            }
            if (encoding != null && !encoding.isEmpty()) {
                pr.setEncoding(encoding);
            }
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
     * @param project project owner of file, if it exists. If the file comes from the filesystem,
     * put null
     */
    public static void showFileContent(File file, ModelProject project) {
        if (file != null && file.exists()) {
            // Check if the file is already opened
            for (Tab t : staticWorkingArea.getTabs()) {
                if (t.getText().equals(file.getName())) {
                    staticWorkingArea.getSelectionModel().select(t);
                    return;
                }
            }
            // Select the class depending on the extension
            Tab t = new Tab(file.getName());
            if (file.getName().endsWith(".tsv") || file.getName().endsWith(".mist")) {
                t.setContent(new TSVReader(file));
            } else if (file.getName().endsWith(".vcf")) {
                t.setContent(new VCFReader(file));
            } else if (file.getName().endsWith(".bam")) {
                // A bam file needs a reference genome
                File ref = null;
                // First try to fetch from project
                if (project != null) {
                    String grch = project.getGenomeCode();
                    String refgen = OS.getProperties().getProperty(grch);
                    if (refgen != null) {
                        ref = new File(refgen);
                    }
                }
                // If not, ask user
                if (ref == null) {
                    ref = FileManager.openFile(ExomeSuite.getResources().getString("select.genome"),
                            FileManager.FASTA_FILTER);
                }
                // Still nothing? meeeeh: error
                if (ref == null) {
                    MainViewController.printMessage("Genome not selected", "error");
                    return;
                } else {
                    t.setContent(new BamReader(file, ref));
                }
            } else {
                String message = ExomeSuite.getStringFormatted("extension.unsupported", file.getName());
                printMessage(message, "warning");
                return;
            }
            String message = ExomeSuite.getStringFormatted("file.opened", file.getAbsolutePath());
            printMessage(message, "success");
            staticWorkingArea.getTabs().add(t);
            staticWorkingArea.getSelectionModel().select(t);
        } else {
            String message = ExomeSuite.getStringFormatted("file.not.accesible", file);
            MainViewController.printMessage(message, "warning");
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
            FXMLLoader loader = new FXMLLoader(CombineMIST.class
                    .getResource("CombineMIST.fxml"), ExomeSuite.getResources());
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.initOwner(ExomeSuite.getMainStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(ExomeSuite.getResources().getString("combine.mist"));
            stage.showAndWait();
        } catch (IOException ex) {
            printException(ex);
        }
    }

    /**
     * Opens the dialog of combining VCF files.
     */
    private void combineVCF() {
        try {
            FXMLLoader loader = new FXMLLoader(CombineVariants.class
                    .getResource("CombineVariants.fxml"), ExomeSuite.getResources());
            Parent p = loader.load();
            Scene scene = new Scene(p);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.initOwner(ExomeSuite.getMainStage());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(ExomeSuite.getResources().getString("combine.vcf"));
            stage.showAndWait();
        } catch (IOException ex) {
            printException(ex);
        }

    }

    /**
     * Shows the about panel.
     */
    private void showAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(About.class
                    .getResource("About.fxml"), ExomeSuite.getResources());
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
        }
    }

    /**
     * Type must be one of INFO, ERROR, WARNING, SUCCESS, info, error, success and warning.
     *
     * @param message the message to print
     * @param type one of the "info", "error", "warning" and "success" String
     */
    public static void printMessage(String message, String type) {
        infoLabel.setText(df.format(new Date()) + " - " + message);
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
            area.setEditable(false);
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

    private void setProjectsList() {
        projectListView.setItems(OS.getProjects());
        projectListView.setCellFactory(listview -> new ProjectCell());
        // Cell factory
        projectListView.setEditable(false);
        // The placeholder, when there are no projects opened.
        String message = ExomeSuite.getResources().getString("no.projects");
        // In order to show a resizable message, I use a FlowPane with individual words.
        FlowPane placeholder = new FlowPane();
        String[] words = message.split(" ");
        for (String word : words) {
            placeholder.getChildren().add(new Label(word + " "));
        }
        projectListView.setPlaceholder(placeholder);
        projectListView.getSelectionModel().selectedItemProperty().addListener((obs, old, newValue) -> {
            pactions.setProject(newValue);
            projectInfo.setProject(newValue);
        });
    }

    private static class ProjectCell extends ListCell<ModelProject> {

        final MenuItem close = new MenuItem(ExomeSuite.getResources().getString("close"),
                new SizableImage("exomesuite/img/cancel.png", SizableImage.SMALL_SIZE));
        final MenuItem delete = new MenuItem(ExomeSuite.getResources().getString("delete"),
                new SizableImage("exomesuite/img/delete.png", SizableImage.SMALL_SIZE));
        final ContextMenu contextMenu = new ContextMenu(close, delete);

        public ProjectCell() {
            close.setOnAction(event -> close(getListView().getSelectionModel().getSelectedItem()));
            delete.setOnAction(event -> delete(getListView().getSelectionModel().getSelectedItem()));
        }

        @Override
        protected void updateItem(ModelProject project, boolean empty) {
            super.updateItem(project, empty);
            if (empty) {
                textProperty().unbind();
                setText(null);
                setGraphic(null);
                setContextMenu(null);
            } else {
                textProperty().unbind();
                textProperty().bind(project.getNameProperty());
//                setText(item.getName());
                setGraphic(null);
                setContextMenu(contextMenu);
            }
        }

        private void close(ModelProject selectedItem) {
            if (selectedItem != null) {
                getListView().getItems().remove(selectedItem);
            }
        }

        private void delete(ModelProject project) {
            if (project != null) {
                // Ask user to remove folder content
                File path = project.getConfigFile().getParentFile();
                //File config = project.getConfigFile();
                String title = ExomeSuite.getResources().getString("delete.project.title");
                String message = ExomeSuite.getStringFormatted("delete.project.message", path.toString());
                String yes = ExomeSuite.getResources().getString("delete.project.yes");
                String no = ExomeSuite.getResources().getString("delete.project.no");
                String cancel = ExomeSuite.getResources().getString("cancel");
                Dialog.Response response = new Dialog().showYesNoCancel(title, message, yes, no, cancel);
                switch (response) {
                    case YES:
                        FileManager.delete(path, true);
                    case NO:
//                    config.delete();
                        OS.getProjects().remove(project);
                }
            }
        }

    }

}
