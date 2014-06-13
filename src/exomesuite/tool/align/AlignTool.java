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
package exomesuite.tool.align;

import exomesuite.MainViewController;
import exomesuite.Project;
import exomesuite.tool.ToolPane;
import exomesuite.tool.ToolPane.Status;
import exomesuite.utils.Config;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class AlignTool {

    private final ToolPane tool = new ToolPane("Align sequences", ToolPane.Status.RED);
    private final Project project;
    private final Config config;
    private final Node paramsView;
    private AlignParamsViewController params;
    private final Node runningPane;
    private Aligner aligner;
    private Status prevStatus;

    public AlignTool(Project project) {
        this.project = project;
        this.config = project.getConfig();
        paramsView = getParamsView();
        Button goButton = new Button(null, new ImageView("exomesuite/img/r_arrow.png"));
        goButton.setOnAction((ActionEvent event) -> {
            runAligner();
        });
        Button settingsButton = new Button(null, new ImageView("exomesuite/img/gear.png"));
        settingsButton.setOnAction((ActionEvent event) -> {
            showSettings();
        });
        Button stopButton = new Button(null, new ImageView("exomesuite/img/stop.png"));
        stopButton.setOnAction((ActionEvent event) -> {
            stop();
        });
        Button cancel = new Button(null, new ImageView("exomesuite/img/stop.png"));
        cancel.setOnAction((ActionEvent event) -> {
            cancelParams();
        });
        // Initialize buttons
        tool.addButton(ToolPane.Status.RED, settingsButton);
        tool.addButton(ToolPane.Status.RED, goButton);
        tool.addButton(ToolPane.Status.OPEN, cancel);
        tool.addButton(ToolPane.Status.OPEN, goButton);
        tool.addButton(ToolPane.Status.RUNNING, stopButton);
        tool.addButton(ToolPane.Status.GREEN, settingsButton);
        runningPane = new Label("Running", new ProgressBar(-1));
        // Parameters present in settings
        if (config.getProperty(Project.DBSNP) != null) {
            params.getDbsnp().setText(config.getProperty(Project.DBSNP));
        }
        if (config.getProperty(Project.MILLS) != null) {
            params.getMills().setText(config.getProperty(Project.MILLS));
        }
        if (config.getProperty(Project.PHASE1) != null) {
            params.getPhase1().setText(config.getProperty(Project.PHASE1));
        }
    }

    private Node getParamsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AlignParamsView.fxml"));
            loader.load();
            params = loader.getController();
            return loader.getRoot();
        } catch (IOException ex) {
            Logger.getLogger(AlignTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ToolPane getTool() {
        return tool;
    }

    private void runAligner() {
        boolean phred64 = params.isPhred64();
        String dbsnp = params.getDbsnp().getText();
        String mills = params.getMills().getText();
        String phase1 = params.getMills().getText();
        // Aligner will not run if any of the params is not present.
        boolean exit = false;
        if (!dbsnp.isEmpty()) {
            config.setProperty(Project.DBSNP, dbsnp);
        } else {
            exit = true;
        }
        if (!mills.isEmpty()) {
            config.setProperty(Project.MILLS, mills);
        } else {
            exit = true;
        }
        if (!phase1.isEmpty()) {
            config.setProperty(Project.PHASE1, phase1);
        } else {
            return;
        }
        if (exit) {
            return;
        }
        // PARAMS taken from here and from there.
        File pathTemp = new File(project.getPath(), Project.PATH_TEMP);
        pathTemp.mkdirs();
        String temp = pathTemp.getAbsolutePath();
        String forward = config.getProperty(Project.FORWARD);
        String reverse = config.getProperty(Project.REVERSE);
        String genome = MainViewController.getGenome();
        File out = new File(project.getPath(), Project.PATH_ALIGNMENT);
        out.mkdirs();
        String output = new File(out, project.getName() + ".bam").getAbsolutePath();
        aligner = new Aligner(temp, forward, reverse, genome, dbsnp, mills, phase1, output,
                phred64);
        aligner.progressProperty().addListener((
                ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            tool.updateProgress(aligner.getMessage(), newValue.doubleValue());
            if (newValue.intValue() == 1) {
                endAligner();
            }
        });
        aligner.messageProperty().addListener((ObservableValue<? extends String> observable,
                String oldValue, String newValue) -> {
            tool.updateProgress(newValue, aligner.getProgress());
        });
        tool.showPane(runningPane);
        tool.setStatus(ToolPane.Status.RUNNING);
        new Thread(aligner).start();
    }

    private void showSettings() {
        tool.showPane(paramsView);
        prevStatus = tool.getStatus();
        tool.setStatus(ToolPane.Status.OPEN);
    }

    private void stop() {
        if (aligner != null) {
            aligner.stop();
        }
        tool.setStatus(ToolPane.Status.RED);
        tool.hidePane();
    }

    private void endAligner() {
        System.out.println("Done");
        tool.setStatus(ToolPane.Status.GREEN);
    }

    private void cancelParams() {
        tool.hidePane();
        tool.setStatus(prevStatus);
    }

}
