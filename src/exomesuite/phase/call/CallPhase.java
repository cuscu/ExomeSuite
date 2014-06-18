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
package exomesuite.phase.call;

import exomesuite.MainViewController;
import exomesuite.Project;
import exomesuite.systemtask.SystemTask;
import exomesuite.tool.Console;
import exomesuite.tool.ToolPane;
import exomesuite.utils.Config;
import exomesuite.utils.OS;
import exomesuite.utils.Phase;
import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
public class CallPhase extends Phase {

    private final ToolPane toolPane;
    private Caller caller;
    private final Config config;
    private final Project project;

    private final DateFormat df = new SimpleDateFormat("yyMMdd");

    public CallPhase(Project project) {
        this.project = project;
        config = project.getConfig();
        toolPane = new ToolPane("Call variants", ToolPane.Status.RED);
        Button go = new Button(null, new ImageView("exomesuite/img/r_arrow.png"));
        go.setOnAction((ActionEvent event) -> {
            call();
        });
        Button stop = new Button(null, new ImageView("exomesuite/img/stop.png"));
        stop.setOnAction((ActionEvent event) -> {
            stop();
        });
        Button close = new Button(null, new ImageView("exomesuite/img/stop.png"));
        close.setOnAction((ActionEvent event) -> {
            close();
        });
        toolPane.addButton(ToolPane.Status.RED, go);
        toolPane.addButton(ToolPane.Status.RUNNING, stop);
        toolPane.addButton(ToolPane.Status.GREEN, go);
        toolPane.addButton(ToolPane.Status.GREEN, close);
        selectProperStatus();
    }

    /**
     * When user presses the go button.
     */
    private void call() {
        // path/alignment/name.bam
        String input = project.getPath() + File.separator + Project.PATH_ALIGNMENT + File.separator
                + project.getName() + ".bam";
        File variant = new File(project.getPath(), Project.PATH_VARIANTS);
        variant.mkdirs();
        // path/variants/name.vcf
        String output = new File(variant, project.getName() + ".vcf").getAbsolutePath();
        String genome = MainViewController.getConfig().getProperty("genome");
        String dbsnp = MainViewController.getConfig().getProperty("dbsnp");
        Console console = new Console();
        toolPane.showPane(console.getView());
        toolPane.setStatus(ToolPane.Status.RUNNING);
        caller = new Caller(genome, output, input, dbsnp, console.getPrintStream());
        caller.setOnCancelled((WorkerStateEvent event) -> {
            endCall();
            toolPane.setStatus(ToolPane.Status.RED);
        });
        caller.setOnSucceeded((WorkerStateEvent event) -> {
            endCall();
        });
        caller.progressProperty().addListener((
                ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            toolPane.updateProgress(caller.getMessage(), newValue.doubleValue());
        });
        caller.messageProperty().addListener((ObservableValue<? extends String> observable,
                String oldValue, String newValue) -> {
            toolPane.updateProgress(newValue, caller.getProgress());
        });
        new Thread(caller).start();
    }

    /**
     * When user stops the task.
     */
    @Override
    public void stop() {
        if (caller != null) {
            caller.cancel();
        }
    }

    @Override
    public Node getView() {
        return toolPane.getView();
    }

    private void close() {
        toolPane.hidePane();
    }

    private void endCall() {
        if (caller.getValue() == 0) {
            config.setProperty("call_date", df.format(System.currentTimeMillis()));
            toolPane.setStatus(ToolPane.Status.GREEN);
        } else {
            toolPane.setStatus(ToolPane.Status.RED);
            config.removeProperty("call_date");
        }
    }

    @Override
    protected void configChanged() {
        selectProperStatus();
    }

    private void selectProperStatus() {
        final Config prConfig = project.getConfig();
        final Config appConfig = MainViewController.getConfig();
        // Green if yet called.
        // Red if aligned and there is genome and dbsnp.
        // disabled otherwise.
        if (prConfig.containsKey(Config.CALL_DATE)) {
            toolPane.setStatus(ToolPane.Status.GREEN);
        } else if (prConfig.containsKey(Config.ALIGN_DATE) && appConfig.containsKey(Config.GENOME)
                && appConfig.containsKey(Config.DBSNP)) {
            toolPane.setStatus(ToolPane.Status.RED);
        } else {
            toolPane.setStatus(ToolPane.Status.DISABLED);
        }
    }

    /**
     * The task.
     */
    private static class Caller extends SystemTask {

        private final String genome, output, input, dbsnp;

        public Caller(String genome, String output, String input, String dbsnp,
                PrintStream printStream) {
            super(printStream);
            this.genome = genome;
            this.output = output;
            this.input = input;
            this.dbsnp = dbsnp;
        }

        @Override
        protected Integer call() throws Exception {
            // So easy, only one command.
            updateTitle("Calling " + new File(output).getName());
            String gatk = "software" + File.separator + "gatk" + File.separator
                    + "GenomeAnalysisTK.jar";
            updateProgress(50, 100);
            updateMessage("Calling SNPs and indels...");
            int ret = execute(OS.scanJava7(), "-jar", gatk,
                    "-T", "HaplotypeCaller", "-R", genome,
                    "-I", input, "-o", output,
                    "--dbsnp", dbsnp);
            updateMessage("Done.");
            updateProgress(1, 1);
            return ret;
        }
    }

    @Override
    public boolean isRunning() {
        return toolPane.getStatus() == ToolPane.Status.RUNNING;
    }

}
