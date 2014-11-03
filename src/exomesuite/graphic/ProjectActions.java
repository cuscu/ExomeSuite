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
import exomesuite.project.Action;
import exomesuite.project.AlignAction;
import exomesuite.project.CallAction;
import exomesuite.project.MistAction;
import exomesuite.project.Project;
import exomesuite.project.ProjectListener;
import exomesuite.systemtask.SystemTask;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * An hbox which contains buttons that perform actions.
 *
 * @author Pascual Lorente Arencibia
 */
public class ProjectActions extends VBox implements ProjectListener {

    private Project project;

    @FXML
    private ProgressBar progressBar;
    @FXML
    private HBox buttons;
    @FXML
    private Label message;
    @FXML
    private FlatButton cancel;

    private SystemTask task;

    private final List<Action> actions = new ArrayList<>();

    public ProjectActions() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectActions.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectActions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    public void initialize() {
        cancel.setOnAction((ActionEvent event) -> {
            if (task != null && task.isRunning()) {
                task.cancel(true);
            }
        });
        cancel.setDisable(true);
        progressBar.setProgress(0);
        progressBar.setVisible(false);
        cancel.setVisible(false);

        Action align = new AlignAction("align.png", "Align genome", "Select FASTQ files first");
        Action call = new CallAction("call.png", "Call variants", "Align sequences first");
        Action mist = new MistAction("mist.png", "Mist analysis", "Align sequences first");
        actions.add(align);
        actions.add(call);
        actions.add(mist);
    }

    public void setProject(Project project) {
        if ((task == null || !task.isRunning()) && project != null) {
            this.project = project;
            project.addListener(this);
            refreshActions();
        }
    }

    private void refreshActions() {
        buttons.getChildren().clear();
        actions.forEach((Action a) -> {
            FlatButton fb = new FlatButton(a.getIcon(), a.isDisabled(project)
                    ? a.getDisableDescription() : a.getDescription());
            fb.setDisable(a.isDisabled(project));
            fb.setOnAction((ActionEvent event) -> call(a));
            buttons.getChildren().add(fb);
        });
    }

    private void call(Action a) {
        // Get the task form the action, getTask must configure using project
        task = a.getTask(project);
        if (task == null) {
            return;
        }
        // Get the view
        FXMLLoader loader = new FXMLLoader(TaskPanel.class.getResource("TaskPanel.fxml"));
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectActions.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        final TaskPanel taskPanel = loader.getController();
        // Message and progress are easy
        //taskPanel.getTitle().textProperty().bind(task.titleProperty());
        taskPanel.getMessage().textProperty().bind(task.messageProperty());
        taskPanel.getProgress().progressProperty().bind(task.progressProperty());
        // PrintStream its a bit tricky
        task.setPrintStream(new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> {
                    taskPanel.getTextArea().appendText((char) b + "");
                });
            }
        }));
        // Title is binded to the tab
        Tab t = new Tab(task.getTitle());
        task.titleProperty().addListener((ObservableValue<? extends String> observable,
                String oldValue, String newValue) -> t.setText(newValue));
        // When closed, process is killed
        t.setOnCloseRequest(e -> {
            System.err.println("Trying to cancel task");
            task.cancel();
        });
        taskPanel.getCancelButton().setOnAction(e -> {
            System.err.println("Trying to cancel task");
            task.cancel();
        });
        // Fill the tab
        Parent parent = loader.getRoot();
        t.setContent(parent);
        // Put it on working area
        MainViewController.getWorkingArea().getTabs().add(t);
        MainViewController.getWorkingArea().getSelectionModel().select(t);
        // Bind progress
//        progressBar.progressProperty().bind(task.progressProperty());
//        message.textProperty().bind(task.messageProperty());
        // Bind end actions, cancelled or succeded
        task.setOnCancelled(e -> cancelled(a, task));
        task.setOnSucceeded(e -> succeded(a, task));
        // Disable action buttons, only one action at a time
//        buttons.getChildren().forEach(node -> ((FlatButton) node).setDisable(true));
        // Launch the task
        new Thread(task).start();
        // Enable cancel button and show progress
//        cancel.setDisable(false);
//        progressBar.setVisible(true);
//        cancel.setVisible(true);
    }

    private void cancelled(Action a, SystemTask t) {
        a.onCancelled(project, t);
        unbind();
        message.setText(a.getDescription() + " cancelled: code " + t.getValue());
        cancel.setDisable(true);
        refreshActions();
    }

    private void succeded(Action a, SystemTask t) {
        a.onSucceeded(project, t);
        unbind();
        message.setText(a.getDescription() + " terminated with code " + t.getValue());
        cancel.setDisable(true);
        refreshActions();
    }

    private void unbind() {
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        message.textProperty().unbind();
        progressBar.setVisible(false);
        cancel.setVisible(false);
    }

    @Override
    public void projectChanged(Project.PropertyName property) {
        refreshActions();
    }

    private FXMLLoader loadTaskView() {

        return null;
    }

}
