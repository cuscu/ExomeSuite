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
package exomesuite.phase.align;

import exomesuite.MainViewController;
import exomesuite.Project;
import exomesuite.systemtask.Aligner;
import exomesuite.tool.Console;
import exomesuite.tool.ToolPane;
import exomesuite.tool.ToolPane.Status;
import exomesuite.utils.Config;
import exomesuite.utils.Phase;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class AlignPhase extends Phase {

    private final ToolPane tool;
    private final Project project;
    private final Config config;
    private final Node paramsView;
    private AlignParamsViewController params;
    private Aligner aligner;
    private Status prevStatus;

    public AlignPhase(Project project) {
        this.project = project;
        this.config = MainViewController.getConfig();
        paramsView = getParamsView();
        tool = new ToolPane("Align sequences", ToolPane.Status.GREEN);
        // Service buttons.
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
        selectProperStatus();
    }

    private Node getParamsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AlignParamsView.fxml"));
            loader.load();
            params = loader.getController();
            return loader.getRoot();
        } catch (IOException ex) {
            Logger.getLogger(AlignPhase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void runAligner() {
        boolean phred64 = params.isPhred64();
        String dbsnp = config.getProperty(Config.DBSNP);
        String mills = config.getProperty(Config.MILLS);
        String phase1 = config.getProperty(Config.PHASE1);
        // Aligner will not run if any of the params is not present.
        if (dbsnp.isEmpty() || mills.isEmpty() || phase1.isEmpty()) {
            return;
        }
        // PARAMS taken from here and from there.
        File pathTemp = new File(project.getPath(), Project.PATH_TEMP);
        pathTemp.mkdirs();
        String temp = pathTemp.getAbsolutePath();
        String forward = config.getProperty(Config.FORWARD);
        String reverse = config.getProperty(Config.REVERSE);
        String genome = MainViewController.getConfig().getProperty("genome");
        File out = new File(project.getPath(), Project.PATH_ALIGNMENT);
        out.mkdirs();
        String output = new File(out, project.getName() + ".bam").getAbsolutePath();
        Console console = new Console();
        aligner
                = new Aligner(console.getPrintStream(), temp, forward, reverse, genome, dbsnp, mills,
                        phase1, output, phred64);
        aligner.progressProperty().addListener((
                ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            tool.updateProgress(aligner.getMessage(), newValue.doubleValue());
        });
        aligner.messageProperty().addListener((ObservableValue<? extends String> observable,
                String oldValue, String newValue) -> {
            tool.updateProgress(newValue, aligner.getProgress());
        });
        aligner.setOnCancelled((WorkerStateEvent event) -> {
            tool.setStatus(Status.RED);
        });
        aligner.setOnSucceeded((WorkerStateEvent event) -> {
            tool.setStatus(Status.GREEN);
        });
        aligner.progressProperty().addListener((
                ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            tool.updateProgress(aligner.getMessage(), newValue.doubleValue());
            if (newValue.intValue() == 1) {
                tool.setStatus(ToolPane.Status.GREEN);
            }
        });
        aligner.messageProperty().addListener((
                ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            tool.updateProgress(newValue, aligner.getProgress());
        });
        tool.setStatus(ToolPane.Status.RUNNING);
        new Thread(aligner).start();
    }

    private void showSettings() {
        tool.showPane(paramsView);
        prevStatus = tool.getStatus();
        tool.setStatus(ToolPane.Status.OPEN);
    }

    @Override
    public void stop() {
        if (aligner != null) {
            aligner.cancel();
        }
        tool.setStatus(ToolPane.Status.RED);
        tool.hidePane();
    }

    private void cancelParams() {
        tool.hidePane();
        tool.setStatus(prevStatus);
    }

    @Override
    protected void configChanged() {
        selectProperStatus();
    }

    @Override
    public Node getView() {
        return tool.getView();
    }

    private void selectProperStatus() {
        // Green if this sample has been aligned yet.
        // Red if sequences, genome and dbSNP are available.
        // Otherwise, disabled.
        if (project.getConfig().containsKey(Config.ALIGN_DATE)) {
            tool.setStatus(Status.GREEN);
        } else if (project.getConfig().containsKey(Config.FORWARD)
                && project.getConfig().containsKey(Config.REVERSE)
                && MainViewController.getConfig().containsKey(Config.GENOME)
                && MainViewController.getConfig().containsKey(Config.DBSNP)) {
            tool.setStatus(Status.RED);
        } else {
            tool.setStatus(Status.DISABLED);
        }
    }

    @Override
    public boolean isRunning() {
        return tool.getStatus() == Status.RUNNING;
    }

}
