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
package exomesuite.deprecated;

import exomesuite.graphic.FlatButton;
import exomesuite.systemtask.Aligner;
import exomesuite.systemtask.Caller;
import exomesuite.systemtask.DindelTask;
import exomesuite.systemtask.Mist;
import exomesuite.systemtask.Recalibrator;
import exomesuite.tool.Step;
import exomesuite.tool.ToolPane;
import exomesuite.tsvreader.TSVReader;
import exomesuite.utils.Config;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @deprecated Use exomesuite.project.Project instead.
 * @author Pascual Lorente Arencibia
 */
public class Project {

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
    private VBox toolsPane;
    /**
     * Configuration for this project. Config file use to be name.config.
     */
    private final Config config;

    /**
     * A list with the steps.
     */
    private List<Step> steps;

    /**
     * New projects will create a folder name under parent an an empty config file.
     * <p>
     * [Folder] parent/name
     * <p>
     * [ File ] parent/name/name.config
     *
     * @param name name of the project
     * @param parent root folder
     */
    public Project(String name, File parent) {
        steps = new ArrayList<>();
        path = new File(parent, name);
        path.mkdirs();
        config = new Config(new File(path, name + ".config"));
        // Create folders structure.
        if (!config.containsKey(Config.PATH_TEMP)) {
            File temp = new File(path, "temp");
            temp.mkdirs();
            config.setProperty(Config.PATH_TEMP, temp.getAbsolutePath());
        }
        if (!config.containsKey(Config.NAME)) {
            config.setProperty(Config.NAME, name);
        }
        this.name = name;
    }

//    /**
//     * Add the tools for each project. I tried to create and interface or abstract class. but didn't
//     * succeed. The tree is too long: ??->ToolPane->ToolViewController.
//     */
//    private void loadTools() {
//        Step[] st = {getAlign(), getCall(), getRecal(), getMist(), getDindel()};
//        for (Step step : st) {
//            toolsPane.getChildren().add(step.getToolPane().getView());
//            config.addListener(step);
//        }
//        steps = Arrays.asList(st);
//    }
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

    /**
     * Gets the name of the project.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the project.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the configuration of the project.
     *
     * @return the configuration
     */
    public Config getConfig() {
        return config;
    }

    /**
     * This method must be called to ensure everything within this project is properly closed.
     * iterates over Steps checking if they are running and asking the user if he/she wants to stop
     * them.
     *
     * @return false if the user decide not to stop any running task, true otherwise.
     */
    public boolean close() {
        for (Step step : steps) {
            if (step.isRunning()) {
                if (!askUserToClose()) {
                    return false;
                } else {
                    step.stop();
                }
            }
        }
        return true;
    }

    /**
     * The answer of the user to {@code askUserToClose}.
     */
    private boolean exit = false;

    /**
     * Shows a window asking the user to close the tab.
     *
     * @return true if ok button is clicked, false if continue button is clicked or window is
     * closed.
     */
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
        hBox.setAlignment(Pos.CENTER);
        VBox box = new VBox(5, ask, hBox);
        box.setAlignment(Pos.CENTER);
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.setTitle("Leave? :'-(");
        stage.showAndWait();
        return exit;
    }

    /**
     * Gets the recalibrate variants tool.
     *
     * @return the recal Step
     */
    private Step getRecal() {
        String[] prReqs = {Config.CALL_DATE};
        String[] maReqs = {Config.GENOME, Config.DBSNP, Config.MILLS, Config.OMNI, Config.HAPMAP};
        Step recal = new Step(this, "recal", true, "Recalibrate variants",
                new ImageView("exomesuite/img/recal.png"),
                Arrays.asList(prReqs), Arrays.asList(maReqs));
        recal.setTask(new Recalibrator());
        return recal;
    }

    /**
     * Gets the align Step.
     *
     * @return the align Step
     */
    private Step getAlign() {
        String[] projectReqs = {Config.FORWARD, Config.REVERSE};
        String[] mainReqs = {Config.DBSNP, Config.MILLS, Config.PHASE1, Config.GENOME};
        return new Step(this, "align", true, "Align sequences",
                new ImageView("exomesuite/img/align.png"),
                Arrays.asList(projectReqs), Arrays.asList(mainReqs),
                getAlignConfig(), new Aligner());
    }

    /**
     * Gets the align config pane.
     *
     * @return the align config pane
     */
    private Node getAlignConfig() {
        ToggleGroup group = new ToggleGroup();
        RadioButton phred64 = new RadioButton("Phred + 64 (Illumina 1.3 to 1.6)");
        phred64.setSelected(true);
        RadioButton phred33 = new RadioButton("Phred + 33 (Sanger)");
        group.getToggles().addAll(phred33, phred64);
        getConfig().setProperty("phred64", "true");
        phred64.setOnAction((ActionEvent event) -> {
            getConfig().setProperty("phred64", phred64.isSelected() + "");
        });
        return new VBox(2, phred64, phred33);
    }

    /**
     * Gets the call Step.
     *
     * @return the call Step
     */
    private Step getCall() {
        String[] prReqs = {Config.ALIGN_DATE};
        String[] mReqs = {Config.GENOME, Config.DBSNP};
        Step call = new Step(this, "call", true, "Call variants",
                new ImageView("exomesuite/img/call.png"),
                Arrays.asList(prReqs), Arrays.asList(mReqs));
        call.setTask(new Caller());
        return call;
    }

    /**
     * Gets the mist Step.
     *
     * @return the mist step
     */
    private Step getMist() {
        String[] prReqs = {Config.ALIGN_DATE};
        String[] maReqs = {Config.ENSEMBL_EXONS};
        Step s = new Step(this, "mist", true, "MIST analysis",
                new ImageView("exomesuite/img/mist.png"),
                Arrays.asList(prReqs), Arrays.asList(maReqs), getMistConfig(), new Mist());
        Button view = new FlatButton("eye.png", "View results");
        view.setOnAction((ActionEvent event) -> {
            new TSVReader(new File(getConfig().getProperty("mist_path"), name + ".mist")).show();
        });
        s.addButton(ToolPane.Status.GREEN, view);
        return s;
    }

    /**
     * Gets the mist config pane.
     *
     * @return the mist config pane
     */
    private Node getMistConfig() {
        Slider threshold = new Slider(0, 100, 10);
        HBox.setHgrow(threshold, Priority.ALWAYS);
        Label value = new Label("Threshold: 10");
        getConfig().setProperty("threshold", "10");
        threshold.setBlockIncrement(1);
        threshold.setShowTickLabels(true);
        threshold.setShowTickMarks(true);
        threshold.setMinorTickCount(5);
        threshold.setMajorTickUnit(20);
        threshold.valueProperty().addListener((ObservableValue<? extends Number> observable,
                Number oldValue, Number newValue) -> {
            value.setText("Threshold: " + newValue.intValue());
            getConfig().setProperty("threshold", newValue.intValue() + "");
        });
        return new HBox(5, value, threshold);
    }

    private Step getDindel() {
        String[] prReqs = {Config.ALIGN_DATE};
        String[] maReqs = {Config.GENOME};
        Step step = new Step(this, "dindel", true, "Dindel", new ImageView(
                "exomesuite/img/dindel.png"), Arrays.asList(prReqs), Arrays.asList(maReqs));
        step.setTask(new DindelTask());
        return step;
    }

    /**
     * Gets the view of the project.
     *
     * @return the view
     */
    public Node getView() {
        if (toolsPane == null) {
            toolsPane = new VBox();
            toolsPane.setPadding(new Insets(5));
            final Step[] st = {getAlign(), getCall(), getRecal(), getMist(), getDindel()};
            for (Step step : st) {
                toolsPane.getChildren().add(step.getToolPane().getView());
                config.addListener(step);
            }
            steps = Arrays.asList(st);
        }
        return toolsPane;
    }

}
