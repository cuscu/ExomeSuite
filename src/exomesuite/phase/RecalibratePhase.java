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
import exomesuite.systemtask.Recalibrator;
import exomesuite.tool.Console;
import exomesuite.tool.ToolPane;
import exomesuite.utils.Config;
import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class RecalibratePhase extends Phase {

    private final ToolPane toolPane;
    private final Project project;
    private Recalibrator recalibrator;

    public RecalibratePhase(Project project) {
        this.project = project;
        toolPane = new ToolPane("Realign", ToolPane.Status.RED, "align.png");
        Button go = new Button(null, new ImageView("exomesuite/img/r_arrow.png"));
        toolPane.addButton(ToolPane.Status.RED, go);
        go.setOnAction((ActionEvent event) -> {
            go();
        });
    }

    @Override
    public Node getView() {
        return toolPane.getView();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void configChanged() {
        toolPane.setStatus(getProperStatus());
    }

    private ToolPane.Status getProperStatus() {
        final Config general = MainViewController.getConfig();
        if (project.getConfig().containsKey(Config.RECAL_DATE)) {
            return ToolPane.Status.GREEN;
        } else if (project.getConfig().containsKey(Config.CALL_DATE)
                && general.containsKey(Config.MILLS) && general.containsKey(Config.OMNI)
                && general.containsKey(Config.HAPMAP) && general.containsKey(Config.DBSNP)
                && general.containsKey(Config.GENOME)) {
            return ToolPane.Status.RED;
        }
        return ToolPane.Status.DISABLED;
    }

    private void go() {
        final Config general = MainViewController.getConfig();
        File f = project.getPath();
        File variants = new File(f, Project.PATH_VARIANTS);
        final String input = new File(variants, project.getName() + ".vcf").getAbsolutePath();
        final String output = new File(variants, project.getName() + "_recal.vcf").getAbsolutePath();
        String mills = general.getProperty(Config.MILLS);
        String omni = general.getProperty(Config.OMNI);
        String hapmap = general.getProperty(Config.HAPMAP);
        String dbsnp = general.getProperty(Config.DBSNP);
        String genome = general.getProperty(Config.GENOME);
        String temp = new File(f, "temp").getAbsolutePath();
        Console console = new Console();
        recalibrator = new Recalibrator(console.getPrintStream(), input, output, mills, omni,
                hapmap, dbsnp, genome, temp);
        // What to do when finishing.
        recalibrator.setOnSucceeded((WorkerStateEvent event) -> {
            toolPane.setStatus(ToolPane.Status.GREEN);
            toolPane.hidePane();
        });
        recalibrator.setOnCancelled((WorkerStateEvent event) -> {
            toolPane.setStatus(ToolPane.Status.RED);
        });
        // Bind progress.
        recalibrator.progressProperty().addListener((
                ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            toolPane.updateProgress(recalibrator.getMessage(), recalibrator.getProgress());
        });
        recalibrator.messageProperty().addListener((
                ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            toolPane.updateProgress(recalibrator.getMessage(), recalibrator.getProgress());
        });
        toolPane.showPane(console.getView());
        new Thread(recalibrator).start();
    }

}
