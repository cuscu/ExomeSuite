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

import exomesuite.phase.CallPhase;
import exomesuite.phase.MistPhase;
import exomesuite.phase.SequencesPhase;
import exomesuite.phase.AlignPhase;
import exomesuite.utils.Config;
import exomesuite.utils.Phase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Project {

    /*
     * Structure tree.
     */
    public final static String PATH_ALIGNMENT = "alignments";
    public final static String PATH_TEMP = "temp";
    public final static String PATH_VARIANTS = "variants";
    public final static String PATH_MIST = "mist";

    /**
     * Project name. Will be used for tab title and file naming.
     */
    private String name;
    /**
     * Path to root folder.
     */
    private File path;
    /**
     * A Node (VBox) that stores all the tools.
     */
    private final VBox toolsPane;
    /**
     * Configuration for this project. Config file use to be name.config.
     */
    private final Config config;

    private final List<Phase> phases = new ArrayList<>();

    /**
     * New projects will create a folder name under parent an an empty config file.
     * <p>
     * parent
     * <p>
     * [Folder]name
     * <p>
     * [File] name/name.config
     *
     * @param name
     * @param parent
     */
    Project(String name, File parent) {
        path = new File(parent, name);
        path.mkdirs();
        config = new Config(new File(path, name + ".config"));
        toolsPane = new VBox(5);
        this.name = name;
        this.path = new File(parent, name);
        path.mkdirs();
        loadTools();
    }

    /**
     * Add the tools for each project. I tried to create and interface or abstract class. but didn't
     * succeed. The tree is too long: ??->ToolPane->ToolViewController.
     */
    private void loadTools() {
        phases.add(new SequencesPhase(this));
        phases.add(new AlignPhase(this));
        phases.add(new CallPhase(this));
        phases.add(new MistPhase(this));
        phases.forEach((Phase phase) -> {
            toolsPane.getChildren().add(phase.getView());
            config.addListener(phase);
        });
    }

    /**
     * Returns the directory where all the files are stored.
     *
     * @return
     */
    public File getPath() {
        return path;
    }

    /**
     * Don't use it please. It is intended to move everything to a new place, but now it only
     * changes the path.
     *
     * @param path
     */
    public void setPath(File path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VBox getToolsPane() {
        return toolsPane;
    }

    public Config getConfig() {
        return config;
    }

    /**
     * This method must be called to ensure everything within this project is properly closed.
     *
     * @return
     */
    public boolean close() {
        for (Phase phase : phases) {
            if (phase.isRunning()) {
                if (!askUserToClose()) {
                    return false;
                } else {
                    phase.stop();
                }
            }
        }
        return true;
    }

    private boolean exit = false;

    private boolean askUserToClose() {
        Stage stage = new Stage();
        Label ask = new Label("There are still tasks running, are you sure you want to exit?");
        Button yes = new Button("Exit");
        yes.setOnAction((ActionEvent event) -> {
            stage.close();
            exit = true;
        });
        Button no = new Button("Continue");
        no.setOnAction((ActionEvent event) -> {
            stage.close();
            exit = false;
        });
        HBox hBox = new HBox(5, yes, no);
        VBox box = new VBox(5, ask, hBox);
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.setTitle("Leave? :'-(");
        stage.showAndWait();
        return exit;
    }

}
