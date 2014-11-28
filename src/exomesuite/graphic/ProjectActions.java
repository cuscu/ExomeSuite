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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    private HBox buttons;

    /**
     * The associated list of actions
     */
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

        Action align = new AlignAction("align.png", "Align genome", "Select FASTQ files first");
        Action call = new CallAction("call.png", "Call variants", "Align sequences first");
        Action mist = new MistAction("mist.png", "Mist analysis", "Align sequences first");
        actions.add(align);
        actions.add(call);
        actions.add(mist);
    }

    /**
     * Changes the project associated to the Actions, this will cause that some buttons become
     * active or inactive.
     *
     * @param project
     */
    public void setProject(Project project) {
        if (project != null) {
            this.project = project;
            project.addListener(this);
            refreshActions();
        }
    }

    /**
     * Puts the list of buttons. Associates actions and enables or disables buttons
     */
    private void refreshActions() {
        buttons.getChildren().clear();
        actions.forEach(action -> {
            FlatButton fb = new FlatButton(action.getIcon(), action.isDisabled(project)
                    ? action.getDisableDescription() : action.getDescription());
            fb.setDisable(action.isDisabled(project));
            fb.setOnAction(e -> call(action));
            buttons.getChildren().add(fb);
        });
    }

    /**
     *
     * @param a
     */
    private void call(Action a) {
        // Get the task form the action, getTask must configure using project
        SystemTask task = a.getTask(project);
        if (task == null) {
            return;
        }
        // Get a tab view
        FXMLLoader loader = new FXMLLoader(TaskPanel.class.getResource("TaskPanel.fxml"));
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectActions.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        final TaskPanel taskPanel = loader.getController();
        // Message and progress are easy
        taskPanel.getMessage().textProperty().bind(task.messageProperty());
        taskPanel.getProgress().progressProperty().bind(task.progressProperty());
        // PrintStream its a bit tricky
        task.setPrintStream(new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> taskPanel.getTextArea().appendText((char) b + ""));
            }
        }));
        // Title is binded to the tab
        Tab t = new Tab(task.getTitle());
        task.titleProperty().addListener((ObservableValue<? extends String> observable,
                String oldValue, String newValue) -> t.setText(newValue));
        // When closed, process is killed
        t.setOnCloseRequest(e -> {
//            System.err.println("Trying to cancel task");
            task.cancel();
        });
        taskPanel.getCancelButton().setOnAction(e -> {
//            System.err.println("Trying to cancel task");
            task.cancel();
        });
        // Fill the tab
        Parent parent = loader.getRoot();
        t.setContent(parent);
        // Put it on working area
        MainViewController.getWorkingArea().getTabs().add(t);
        MainViewController.getWorkingArea().getSelectionModel().select(t);
        // Launch the task
        try {
            new Thread(task).start();
        } catch (Exception e) {
            //MainViewController.printMessage("Error launching task", "error");
            MainViewController.showException(e);
//            Dialogs.create().title("Error").showException(e);
        }
    }

    /**
     * When a project property is changed.
     *
     * @param property
     */
    @Override
    public void projectChanged(Project.PropertyName property) {
        refreshActions();
    }

}
