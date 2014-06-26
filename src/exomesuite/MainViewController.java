package exomesuite;

import exomesuite.phase.Databases;
import exomesuite.phase.GenomeManager;
import exomesuite.utils.Config;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class MainViewController {

    private static Config config;

    @FXML
    private TabPane projects;

    public void initialize() {
        initializeManager();
        initializeNewProjectTab();
        initializeDatabases();
    }

    private void initializeManager() {
        config = new Config(new File("exomesuite.config"));
        final Tab newTab = new Tab();
        Button openButton = new Button(null, new ImageView("exomesuite/img/open.png"));
        HBox newOpenProject = new HBox(openButton);
        newTab.setGraphic(newOpenProject);
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

    private void initializeDatabases() {
        final Tab tab = new Tab();
        tab.setGraphic(new ImageView("exomesuite/img/database.png"));
        projects.getTabs().add(tab);
        tab.setClosable(false);
        tab.setContent(new VBox(new Databases().getView(), new GenomeManager().getView()));
    }

    private void initializeNewProjectTab() {
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
     *
     * @param name
     * @param path
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
//        tabList.add(tab);
        projects.getSelectionModel().select(tab);
    }

    public static Config getConfig() {
        return config;
    }

}
