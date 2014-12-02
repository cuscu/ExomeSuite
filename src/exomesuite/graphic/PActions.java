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
import exomesuite.project.Project;
import exomesuite.project.ProjectListener;
import exomesuite.systemtask.Aligner;
import exomesuite.systemtask.Caller;
import exomesuite.systemtask.SamtoolsCaller;
import exomesuite.systemtask.SystemTask;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Manages the actions panels.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class PActions extends HBox implements ProjectListener {

    /**
     * Thec current selected project.
     */
    private Project project;

    private final Button align = new FlatButton("align.png", "Align sequences");
    private final Button call = new FlatButton("call.png", "Call variants");
    private final Button mist = new FlatButton("mist.png", "MIST analysis");

    public PActions() {
        align.setOnAction(event -> showAlingParams());
        call.setOnAction(event -> showCallParams());
        mist.setOnAction(event -> mist());
        align.setText("Align");
        call.setText("Call");
        mist.setText("Mist");
        align.setContentDisplay(ContentDisplay.TOP);
        call.setContentDisplay(ContentDisplay.TOP);
        mist.setContentDisplay(ContentDisplay.TOP);
        getChildren().addAll(align, call, mist);
    }

    @Override
    public void projectChanged(Project.PropertyName property) {
        setButtons();
    }

    public void setProject(Project project) {
        this.project = project;
        setButtons();
    }

    public Project getProject() {
        return project;
    }

    private void showAlingParams() {
        // Fill known properties.
        Properties properties = new Properties();
        if (project.contains(Project.PropertyName.FORWARD_FASTQ)) {
            properties.setProperty("forward", project.getProperty(Project.PropertyName.FORWARD_FASTQ));
        }
        if (project.contains(Project.PropertyName.REVERSE_FASTQ)) {
            properties.setProperty("reverse", project.getProperty(Project.PropertyName.REVERSE_FASTQ));
        }
        if (project.contains(Project.PropertyName.FASTQ_ENCODING)) {
            properties.setProperty("encoding", project.getProperty(Project.PropertyName.FASTQ_ENCODING));
        }
        if (project.contains(Project.PropertyName.REFERENCE_GENOME)) {
            properties.setProperty("reference", project.getProperty(Project.PropertyName.REFERENCE_GENOME));
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
        Stage stage = new Stage();
        CallParams params = new CallParams(properties);
        // Look for the bam files
        List<String> bams = new ArrayList<>();
        String[] files = project.getProperty(Project.PropertyName.FILES, "").split(";");
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

    private void mist() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Stablishes if buttons are enabled or disabled.
     */
    private void setButtons() {
        String files = project.getProperty(Project.PropertyName.FILES, "");
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
        align.setDisable(!project.contains(Project.PropertyName.FORWARD_FASTQ)
                || !project.contains(Project.PropertyName.REVERSE_FASTQ));
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
        String genome = OS.getProperty(reference);
        String dbsnp = OS.getProperty("dbsnp");
        String mills = OS.getProperty("mills");
        String phase1 = OS.getProperty("phase1");
        String output = project.getProperty(Project.PropertyName.PATH) + File.separator
                + project.getProperty(Project.PropertyName.CODE) + ".bam";
        String name = project.getProperty(Project.PropertyName.NAME);
        boolean illumina = properties.getProperty("encoding").equals("phred+64");
        boolean refine = reference.equalsIgnoreCase("grch37");
        SystemTask aligner = new Aligner(temp, forward, reverse, genome, dbsnp, mills, phase1,
                output, name, illumina, refine);
        bindAndStart(aligner);
    }

    private void call(Properties params) {

        if (params.getProperty("algorithm").toLowerCase().equals("samtools")) {
            SamtoolsCaller caller = new SamtoolsCaller(null, null, null, null);

        } else {
            Caller caller = new Caller(null, null, null, null);
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
            MainViewController.showException(ex);
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
                    MainViewController.printMessage("Task " + task.getTitle() + " canceled by user", "info");
                    task.cancel();
                } else {
                    e.consume();
                }
            }

        });
        taskPanel.getCancelButton().setOnAction(e -> {
            task.cancel();
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
            MainViewController.showException(e);
        }
    }

}
