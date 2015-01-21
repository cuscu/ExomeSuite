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

import exomesuite.ExomeSuite;
import exomesuite.MainViewController;
import exomesuite.actions.AlignLongAction;
import exomesuite.actions.CallLongAction;
import exomesuite.actions.LongAction;
import exomesuite.actions.MistLongAction;
import exomesuite.project.Project;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.Configuration;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;

/**
 * Manages the actions panels. Align, Call and Mist Buttons.
 *
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class PActions extends HBox implements Configuration.ConfigurationListener {

    List<LongAction> actions = new ArrayList();

    {
        actions.add(new AlignLongAction());
        actions.add(new CallLongAction());
        actions.add(new MistLongAction());
    }
    /**
     * The current selected project.
     */
    private Project project;

    /**
     * Creates a new Project Actions pane.
     *
     */
    public PActions() {
    }

    /**
     * Changes the project.
     *
     * @param project the project of the panel
     */
    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            setButtons();
        }
    }

    /**
     * Gets the project.
     *
     * @return the current project
     */
    public Project getProject() {
        return project;
    }

    /**
     * Stablishes if buttons are enabled or disabled.
     */
    private void setButtons() {
        getChildren().clear();
        actions.stream().forEach((action) -> {
            Button button = new Button(action.getName(),
                    new SizableImage(action.getIconPath(), SizableImage.MEDIUM_SIZE));
            button.setOnAction(event -> bindAndStart(action.getTask(project)));
            button.setContentDisplay(ContentDisplay.TOP);
            button.getStyleClass().add("graphic-button");
            getChildren().add(button);
        });
    }

    /**
     * Creates a TaskPanel in a tab in the main view and binds progress, messages and printStream of
     * the task to the TaskPanel. Starts the task.
     *
     * @param task
     */
    private void bindAndStart(SystemTask task) {
        if (task == null) {
            return;
        }
        // Get a new TaskPanel
        FXMLLoader loader = new FXMLLoader(TaskPanel.class.getResource("TaskPanel.fxml"), ExomeSuite.getResources());
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
        task.titleProperty().addListener((observable, oldValue, newValue) -> t.setText(newValue));
        // When closed, process is killed
        t.setOnCloseRequest(e -> {
            if (task.isRunning()) {
                String title = ExomeSuite.getResources().getString("cancel.task.title");
                String message = ExomeSuite.getResources().getString("cancel.task.message");
                String yes = ExomeSuite.getResources().getString("cancel.task.yes");
                String no = ExomeSuite.getResources().getString("cancel.task.no");

                Dialog.Response response = new Dialog().showYesNo(title, message, yes, no);
                if (response == Dialog.Response.YES) {
                    if (task.cancel(true)) {
                        // Nothing, perhaps print a message
                    }
                } else {
                    e.consume();
                }
            }

        });
        // inform user about the victory
        task.setOnSucceeded(event -> {
            if (task.getValue() == 0) {
                String message = ExomeSuite.getStringFormatted("task.finished.ok", task.getTitle());
                MainViewController.printMessage(message, "success");
            } else {
                String message = ExomeSuite.getStringFormatted("task.finished.error", task.getTitle());
                MainViewController.printMessage(message, "error");
            }
            taskPanel.getProgress().setVisible(false);
            taskPanel.getCancelButton().setVisible(false);
        });
        taskPanel.getCancelButton().setOnAction(e -> {
            if (!task.cancel(true)) {
                MainViewController.printMessage(ExomeSuite.getResources().getString("impossible.stop.task"), "warning");
            }
        });
        task.setOnCancelled(event -> {
            taskPanel.getProgress().setVisible(false);
            taskPanel.getCancelButton().setVisible(false);
            String message = ExomeSuite.getStringFormatted("user.canceled.task", task.getTitle());
            MainViewController.printMessage(message, "info");
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
            String message = ExomeSuite.getStringFormatted("task.started", task.getTitle());
            MainViewController.printMessage(message, "info");
        } catch (Exception e) {
            MainViewController.printException(e);
        }
    }

    @Override
    public void configurationChanged(Configuration configuration, String keyChanged) {
        setButtons();
    }

}
