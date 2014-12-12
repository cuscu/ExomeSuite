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
package exomesuite.graphic;

import exomesuite.MainViewController;
import exomesuite.actions.AlignParams;
import exomesuite.actions.CallParams;
import exomesuite.actions.MistParams;
import exomesuite.project.Project;
import exomesuite.systemtask.Aligner;
import exomesuite.systemtask.Caller;
import exomesuite.systemtask.Mist;
import exomesuite.systemtask.SamtoolsCaller;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.Configuration;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Manages the actions panels. Align, Call and Mist Buttons.
 *
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class PActions extends HBox implements Configuration.ConfigurationListener {

    /**
     * Thec current selected project.
     */
    private Project project;

    private final Button align = new Button("Align", new SizableImage("exomesuite/img/align.png", 32));
    private final Button call = new Button("Call", new SizableImage("exomesuite/img/call.png", 32));
    private final Button mist = new Button("Mist", new SizableImage("exomesuite/img/mist.png", 32));

    public PActions() {
        align.setOnAction(event -> showAlingParams());
        call.setOnAction(event -> showCallParams());
        mist.setOnAction(event -> showMistParams());
        align.setTooltip(new Tooltip("Align sequencing data"));
        call.setTooltip(new Tooltip("Call variants from alignments"));
        mist.setTooltip(new Tooltip("Mist analysis"));
        align.setContentDisplay(ContentDisplay.TOP);
        call.setContentDisplay(ContentDisplay.TOP);
        mist.setContentDisplay(ContentDisplay.TOP);
        align.getStyleClass().add("graphic-button");
        call.getStyleClass().add("graphic-button");
        mist.getStyleClass().add("graphic-button");
        getChildren().addAll(align, call, mist);
    }

    public void setProject(Project project) {
        this.project = project;
        setButtons();
    }

    public Project getProject() {
        return project;
    }

    /**
     * Stablishes if buttons are enabled or disabled.
     */
    private void setButtons() {
        String files = project.getProperties().getProperty(Project.FILES, "");
        List<String> fs = Arrays.asList(files.split(";"));
        // Mist and call can only be done with a BAM file.
        mist.setDisable(true);
        call.setDisable(true);
        for (String file : fs) {
            if (file.endsWith(".bam")) {
                mist.setDisable(false);
                call.setDisable(false);
                break;
            }
        }
        // Align can be done with both sequences files
        align.setDisable(!project.getProperties().containsProperty(Project.FORWARD_FASTQ)
                || !project.getProperties().containsProperty(Project.REVERSE_FASTQ));
    }

    private void showAlingParams() {
        // Fill known properties.
        Properties properties = new Properties();
        if (project.getProperties().containsProperty(Project.FORWARD_FASTQ)) {
            properties.setProperty("forward", project.getProperties().getProperty(Project.FORWARD_FASTQ));
        }
        if (project.getProperties().containsProperty(Project.REVERSE_FASTQ)) {
            properties.setProperty("reverse", project.getProperties().getProperty(Project.REVERSE_FASTQ));
        }
        if (project.getProperties().containsProperty(Project.FASTQ_ENCODING)) {
            properties.setProperty("encoding", project.getProperties().getProperty(Project.FASTQ_ENCODING));
        }
        if (project.getProperties().containsProperty(Project.REFERENCE_GENOME)) {
            properties.setProperty("reference", project.getProperties().getProperty(Project.REFERENCE_GENOME));
        }
        // Show the screen with known Properties and call align(properties) when closed and accepted.
        Stage stage = new Stage();
        AlignParams params = new AlignParams(properties);
        Scene scene = new Scene(params);
        stage.setScene(scene);
        scene.getStylesheets().add("exomesuite/main.css");
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        params.setOnClose(event -> {
            stage.close();
            if (params.accept()) {
                align(params.getParams());
            }
        });
        stage.showAndWait();
    }

    private void showCallParams() {
        Properties properties = new Properties();
        if (project.getProperties().containsProperty(Project.REFERENCE_GENOME)) {
            properties.setProperty("reference", project.getProperties().getProperty(Project.REFERENCE_GENOME));
        }
        Stage stage = new Stage();
        CallParams params = new CallParams(properties);
        // Look for the bam files
        List<String> bams = new ArrayList<>();
        String[] files = project.getProperties().getProperty(Project.FILES, "").split(";");
        for (String s : files) {
            if (s.endsWith(".bam")) {
                bams.add(s);
            }
        }
        params.setBamOptions(bams);
        List<String> alg = new ArrayList<>();
        alg.add("GATK");
        alg.add("Samtools");
        params.setAlgorithmOptions(alg);
        Scene scene = new Scene(params);
        stage.setScene(scene);
        scene.getStylesheets().add("exomesuite/main.css");
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        params.setOnClose(event -> {
            stage.close();
            if (params.accept()) {
                call(params.getParams());
            }
        });
        stage.showAndWait();
    }

    private void showMistParams() {
        Properties properties = new Properties();
        Stage stage = new Stage();
        MistParams params = new MistParams(properties);
        // Look for the bam files
        List<String> bams = new ArrayList<>();
        String[] files = project.getProperties().getProperty(Project.FILES, "").split(";");
        for (String s : files) {
            if (s.endsWith(".bam")) {
                bams.add(s);
            }
        }
        params.setBamOptions(bams);
        Scene scene = new Scene(params);
        stage.setScene(scene);
        scene.getStylesheets().add("exomesuite/main.css");
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        params.setOnAccept(event -> {
            stage.close();
            if (params.accept()) {
                mist(params.getParams());
            }
        });
        stage.showAndWait();
    }

    /**
     * Creates an Aligner with the given properties, then call bindAndStart(aligner).
     *
     * @param properties
     */
    private void align(Properties properties) {
        String temp = OS.getTempDir();
        String forward = properties.getProperty("forward");
        String reverse = properties.getProperty("reverse");
        String reference = properties.getProperty("reference");
        String genome = OS.getProperties().getProperty(reference);
        String dbsnp = OS.getProperties().getProperty("dbsnp");
        String mills = OS.getProperties().getProperty("mills");
        String phase1 = OS.getProperties().getProperty("phase1");
        String output = project.getProperties().getProperty(Project.PATH) + File.separator
                + project.getProperties().getProperty(Project.CODE) + ".bam";
        String name = project.getProperties().getProperty(Project.NAME);
        boolean illumina = properties.getProperty("encoding").equals("phred+64");
        boolean refine = reference.equalsIgnoreCase("grch37");
        List<String> errors = new ArrayList<>();
        // Check that parameters are ok.
        if (refine) {
//            errors.addAll(FileManager.tripleCheck(dbsnp, mills, phase1));
            if (!FileManager.tripleCheck(dbsnp)) {
                errors.add("dbSNP");
            }
            if (!FileManager.tripleCheck(mills)) {
                errors.add("Mills database");
            }
            if (!FileManager.tripleCheck(phase1)) {
                errors.add("1000 genomes pahse 1 indels database");
            }
        }
//        errors.addAll(FileManager.tripleCheck(genome, forward, reverse, temp));
        if (!FileManager.tripleCheck(genome)) {
            errors.add("Reference genome");
        }
        if (!FileManager.tripleCheck(forward)) {
            errors.add("Forward sequences");
        }
        if (!FileManager.tripleCheck(reverse)) {
            errors.add("Reverse sequences");
        }
        if (!FileManager.tripleCheck(temp)) {
            errors.add("Temporary folder");
        }
        if (!errors.isEmpty()) {
            MainViewController.printMessage("There is one or more arguments not specifierd:\n" + errors, "warning");
            return;
        }
        SystemTask aligner = new Aligner(temp, forward, reverse, genome, dbsnp, mills, phase1,
                output, name, illumina, refine);
        bindAndStart(aligner);
    }

    /**
     * Creates a Caller with the given properties, then call bindAndStart(caller).
     *
     * @param properties
     */
    private void call(Properties params) {
        String reference = params.getProperty("reference");
        String genome = OS.getProperties().getProperty(reference);
        String dbsnp = OS.getProperties().getProperty("dbsnp");
        String input = params.getProperty("bamFile");
        String algorithm = params.getProperty("algorithm");
        List<String> errors = new ArrayList<>();
        if (!FileManager.tripleCheck(genome)) {
            errors.add("Reference genome");
        }
        if (!FileManager.tripleCheck(dbsnp)) {
            errors.add("dbSNP");
        }
        if (!FileManager.tripleCheck(input)) {
            errors.add("Input bam");
        }
        if (!errors.isEmpty()) {
            MainViewController.printMessage("There is one or more arguments not specifierd:\n" + errors, "warning");
            return;
        }
        // path/code.vcf
        String output = project.getProperties().getProperty(Project.PATH) + File.separator
                + project.getProperties().getProperty(Project.CODE) + ".vcf";
        SystemTask task;
        task = algorithm.toLowerCase().equals("samtools")
                ? new SamtoolsCaller(genome, input, output)
                : new Caller(genome, output, input, dbsnp);
        task.stateProperty().addListener((ObservableValue<? extends Worker.State> observable,
                Worker.State oldValue, Worker.State newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        project.addExtraFile(output);
                    }
                });
        bindAndStart(task);
    }

    private void mist(Properties params) {
//        String reference = params.getProperty("reference");
        String ensembl = OS.getProperties().getProperty("ensembl");
        String input = params.getProperty("bamFile");
        String threshold = params.getProperty("threshold");
        String length = params.getProperty("length");
        int intThreshold, intLength;
        try {
            intThreshold = Integer.valueOf(threshold);
            intLength = Integer.valueOf(length);
            // path/code.vcf
            String output = project.getProperties().getProperty(Project.PATH) + File.separator
                    + project.getProperties().getProperty(Project.CODE) + "_dp" + threshold + "_l" + length + ".mist";
            Mist task = new Mist(input, output, ensembl, intThreshold, intLength);
            task.stateProperty().addListener((ObservableValue<? extends Worker.State> observable,
                    Worker.State oldValue, Worker.State newValue) -> {
                        if (newValue == Worker.State.SUCCEEDED) {
                            project.addExtraFile(output);
                        }
                    });
            task.stateProperty().addListener((ObservableValue<? extends Worker.State> observable,
                    Worker.State oldValue, Worker.State newValue) -> {
                        if (newValue == Worker.State.SUCCEEDED) {
                            project.addExtraFile(output);
                        }
                    });
            bindAndStart(task);
        } catch (Exception e) {
            MainViewController.printException(e);
        }
    }

    /**
     * Creates a TaskPanel in a tab in the main view and binds progress, messages and printStream of
     * the task to the TaskPanel. Starts the task.
     *
     * @param task
     */
    private void bindAndStart(SystemTask task) {
        // Get a new TaskPanel
        FXMLLoader loader = new FXMLLoader(TaskPanel.class.getResource("TaskPanel.fxml"));
        try {
            loader.load();
        } catch (IOException ex) {
            MainViewController.printException(ex);
//            Logger.getLogger(ProjectActions.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        final TaskPanel taskPanel = loader.getController();

        // Message and progress are easy to bind
        taskPanel.getMessage().textProperty().bind(task.messageProperty());
        taskPanel.getProgress().progressProperty().bind(task.progressProperty());
        // PrintStream its a bit tricky
        task.setPrintStream(new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> taskPanel.getTextArea().appendText((char) b + ""));
            }
        }));
        // Title is binded to the tab text
        Tab t = new Tab(task.getTitle());
        task.titleProperty().addListener((ObservableValue<? extends String> observable,
                String oldValue, String newValue) -> t.setText(newValue));
        // When closed, process is killed
        t.setOnCloseRequest(e -> {
            if (task.isRunning()) {
                Dialog.Response response = new Dialog().showYesNo("The task is still running",
                        "If you close the tab, the task will be canceled.",
                        "Cancel task", "Continue task");
                if (response == Dialog.Response.YES) {
                    if (task.cancel(true)) {

                    }
                } else {
                    e.consume();
                }
            }

        });
        // inform user about the victory
        task.setOnSucceeded(event
                -> {
                    MainViewController.printMessage("Task " + task.getTitle() + " finished", "success");
                    taskPanel.getProgress().setVisible(false);
                    taskPanel.getCancelButton().setVisible(false);
                });
        taskPanel.getCancelButton().setOnAction(e -> {
            if (!task.cancel(true)) {
                MainViewController.printMessage("Imposible to stop task", "warning");
            }
        });
        task.setOnCancelled(event -> {
            MainViewController.printMessage("Task " + task.getTitle() + " canceled by user", "info");
        });
        // Fill the tab
        Parent parent = loader.getRoot();
        t.setContent(parent);
        // Put it on working area
        MainViewController.getWorkingArea().getTabs().add(t);
        MainViewController.getWorkingArea().getSelectionModel().select(t);
        // Launch the task
        MainViewController.printMessage("Task " + task.getTitle() + " started", "info");
        try {
            new Thread(task).start();
        } catch (Exception e) {
            MainViewController.printException(e);
        }
    }

    @Override
    public void configurationChanged(Configuration configuration, String keyChanged) {
        setButtons();
    }

}
