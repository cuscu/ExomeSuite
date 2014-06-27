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
package exomesuite.phase;

import exomesuite.MainViewController;
import exomesuite.Project;
import exomesuite.systemtask.Mist;
import exomesuite.tool.Console;
import exomesuite.tool.ToolPane;
import exomesuite.tool.ToolPane.Status;
import exomesuite.utils.Config;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class MistPhase extends Phase {

    private final ToolPane toolPane;
    private final Project project;
    private final Node params;
    private Status prevStatus;
    private Slider threshold;
    private final DateFormat df = new SimpleDateFormat("yyMMdd");

    public MistPhase(Project project) {
        this.project = project;
        toolPane = new ToolPane("MIST analysis", properStatus(), "mist.png");
        Button go = new Button(null, new ImageView("exomesuite/img/r_arrow.png"));
        go.setOnAction((ActionEvent event) -> {
            go();
        });
        Button settings = new Button(null, new ImageView("exomesuite/img/gear.png"));
        settings.setOnAction((ActionEvent event) -> {
            settings();
        });
        Button cancel = new Button(null, new ImageView("exomesuite/img/stop.png"));
        cancel.setOnAction((ActionEvent event) -> {
            cancel();
        });
        Button stop = new Button(null, new ImageView("exomesuite/img/stop.png"));
        stop.setOnAction((ActionEvent event) -> {
            stop();
        });
        toolPane.addButton(ToolPane.Status.RED, settings);
        toolPane.addButton(ToolPane.Status.RED, go);
        toolPane.addButton(ToolPane.Status.GREEN, settings);
        toolPane.addButton(ToolPane.Status.OPEN, cancel);
        toolPane.addButton(ToolPane.Status.OPEN, go);
        toolPane.addButton(ToolPane.Status.RUNNING, stop);
        params = getParams();
    }

    @Override
    public Node getView() {
        return toolPane.getView();
    }

    @Override
    public void configChanged() {
        toolPane.setStatus(properStatus());
    }

    private ToolPane.Status properStatus() {
        if (project.getConfig().containsKey(Config.MIST_DATE)) {
            return ToolPane.Status.GREEN;
        } else if (project.getConfig().containsKey(Config.ALIGN_DATE)
                && MainViewController.getConfig().containsKey(Config.ENSEMBL_EXONS)) {
            return ToolPane.Status.RED;
        } else {
            return ToolPane.Status.DISABLED;
        }
    }

    private void go() {
        toolPane.setStatus(ToolPane.Status.RUNNING);
        toolPane.hidePane();
        int val = new Double(threshold.getValue()).intValue();
        // INPUT=path/alignments/name.bam
        File aln = new File(project.getPath(), Project.PATH_ALIGNMENT);
        File input = new File(aln, project.getName() + ".bam");
        // OUTPUT=path/mist/name.mist
        File mistDir = new File(project.getPath(), Project.PATH_MIST);
        mistDir.mkdirs();
        File output = new File(mistDir, project.getName() + ".mist");
        File ensembl = new File(MainViewController.getConfig().getProperty(Config.ENSEMBL_EXONS));
        System.out.println("Input:" + input);
        System.out.println("Output:" + output);
        System.out.println("Threshold:" + val);
        System.out.println("Ensembl:" + ensembl);
        Console console = new Console();
        Mist mist = new Mist(console.getPrintStream(), input, output, ensembl, val);
        mist.setOnSucceeded((WorkerStateEvent event) -> {
            project.getConfig().setProperty(Config.MIST_DATE, df.format(System.currentTimeMillis()));
            toolPane.setStatus(Status.GREEN);
        });
        mist.progressProperty().addListener((ObservableValue<? extends Number> observable,
                Number old, Number current) -> {
            toolPane.updateProgress(mist.getMessage(), mist.getProgress());
        });
        mist.messageProperty().addListener((ObservableValue<? extends String> observable,
                String oldValue, String newValue) -> {
            toolPane.updateProgress(mist.getMessage(), mist.getProgress());
        });
        new Thread(mist).start();
    }

    private void settings() {
        toolPane.showPane(params);
        prevStatus = toolPane.getStatus();
        toolPane.setStatus(ToolPane.Status.OPEN);
    }

    private void cancel() {
        toolPane.hidePane();
        toolPane.setStatus(prevStatus);
    }

    @Override
    public final void stop() {
        toolPane.setStatus(Status.RED);
        toolPane.hidePane();
    }

    private Node getParams() {
        threshold = new Slider(0, 100, 10);
        HBox.setHgrow(threshold, Priority.ALWAYS);
        Label value = new Label("Threshold: 10");
        threshold.setBlockIncrement(1);
        threshold.setShowTickLabels(true);
        threshold.setShowTickMarks(true);
        threshold.setMinorTickCount(5);
        threshold.setMajorTickUnit(20);
        threshold.valueProperty().addListener((ObservableValue<? extends Number> observable,
                Number oldValue, Number newValue) -> {
            value.setText("Threshold: " + newValue.intValue());
        });
        return new HBox(5, value, threshold);
    }

    @Override
    public boolean isRunning() {
        return toolPane.getStatus() == Status.RUNNING;
    }

}
