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
package exomesuite.tool;

import exomesuite.MainViewController;
import exomesuite.Project;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.Config;
import exomesuite.utils.FlatButton;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * Manages a single step into a Project. Steps are all those single tasks that can be done to each
 * project. A Step holds a {@link ToolPane} to communicate with the user. A configuration pane
 * and/or a SystemTask can be set on constructor or afterward. Properties of the Step are explained
 * into constructors.
 *
 * @author Pascual Lorente Arencibia
 */
public final class Step {

    /**
     * The view of the Step. The communication with the user.
     */
    private final ToolPane toolPane;
    /**
     * The title of the Step.
     */
    private final String title;
    /**
     * A code to set all the properties.
     */
    private final String code;
    /**
     * The configs of the project and the application.
     */
    private final Config projectConfig, mainConfig;
    /**
     * The SystemTask of the Step.
     */
    private SystemTask task;
    /**
     * The keys to be stored in the Project configuration to enable it.
     */
    private final List<String> projectRequisites;
    /**
     * The keys to be stored in the Application configuration to enable it.
     *
     */
    private final List<String> mainRequisites;
    /**
     *
     */
    private final DateFormat df = new SimpleDateFormat("yyMMdd HHmmss");

    /**
     * Creates a directory within the project and a Config file within the directory.
     *
     * @param project The project where this step belongs.
     * @param code A code for this tool.
     * @param mkdir If true, a folder with code name will be created.
     * @param title Name of the tool.
     * @param ico Some nice 16x16 image.
     * @param projectReqs The requisites from the project to enable this tool.
     * @param appReqs The requisites from the application to enable this tool.
     * @param configPane The configuration pane.
     * @param task The task.
     *
     * @throws NullPointerException if configPane or task are null.
     */
    public Step(Project project, String code, boolean mkdir, String title, ImageView ico,
            List<String> projectReqs, List<String> appReqs, Node configPane, SystemTask task) {
        if (configPane == null || task == null) {
            throw new NullPointerException();
        }
        this.projectRequisites = projectReqs;
        this.mainRequisites = appReqs;
        this.title = title;
        this.code = code;
        mainConfig = MainViewController.getConfig();
        projectConfig = project.getConfig();
        if (mkdir && !projectConfig.containsKey(code + "_path")) {
            File dir = new File(project.getPath(), code);
            dir.mkdirs();
            projectConfig.setProperty(code + "_path", dir.getAbsolutePath());
        }
        // Default toolPane do nothing spectacular.
        toolPane = new ToolPane(title, getProperStatus(), ico);
        toolPane.addButton(ToolPane.Status.GREEN, new Button(null, new ImageView(
                "exomesuite/img/blank.png")));
        setConfigPane(configPane);
        setTask(task);
    }

    /**
     * Creates a new Step without setting a ConfigPane or a task.
     *
     * @param project The project where this step belongs.
     * @param code A code for this tool.
     * @param mkdir If true, a folder with code name will be created.
     * @param title Name of the tool.
     * @param ico Some nice 16x16 image.
     * @param projectReqs The requisites from the project to enable this tool.
     * @param appReqs The requisites from the application to enable this tool.
     */
    public Step(Project project, String code, boolean mkdir, String title, ImageView ico,
            List<String> projectReqs, List<String> appReqs) {
        this.projectRequisites = projectReqs;
        this.mainRequisites = appReqs;
        this.title = title;
        this.code = code;
        mainConfig = MainViewController.getConfig();
        projectConfig = project.getConfig();
        if (mkdir && !projectConfig.containsKey(code + "_path")) {
            File dir = new File(project.getPath(), code);
            dir.mkdirs();
            projectConfig.setProperty(code + "_path", dir.getAbsolutePath());
        }
        // Default toolPane do nothing spectacular.
        toolPane = new ToolPane(title, getProperStatus(), ico);
        toolPane.addButton(ToolPane.Status.GREEN, new Button(null, new ImageView(
                "exomesuite/img/blank.png")));
    }

    /**
     * Returns the status of the tool based on the config files.
     * <table>
     * <thead>
     * <tr>
     * <th>Status</th>
     * <th>Description</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>{@code ToolPane.Status.RUNNING}</td>
     * <td>SystemTask is running.</td>
     * </tr>
     * <tr>
     * <td>{@code ToolPane.Status.GREEN}</td>
     * <td>code_date key is contained into project configuration.</td>
     * </tr>
     * <tr>
     * <td>{@code ToolPane.Status.DISABLED}</td>
     * <td>Configuration files do not contain all the needed keys.</td>
     * </tr>
     * <tr>
     * <td>{@code ToolPane.Status.RED}</td>
     * <td>Rest of cases: configuration keys are present, the task is not running and there is no
     * code_date.</td>
     * </tr>
     * </tbody>
     * <caption>Status description.</caption>
     * </table>
     *
     * @return a value of {@link ToolPane.Status} that represent the Status of this Step.
     */
    public ToolPane.Status getProperStatus() {
        // Just in case tool is called when running.
        if (toolPane != null && toolPane.getStatus() == ToolPane.Status.RUNNING) {
            return ToolPane.Status.RUNNING;
        }
        if (projectConfig.containsKey(code + "_date")) {
            return ToolPane.Status.GREEN;
        } else if (projectRequisites == null && mainRequisites == null) {
            return ToolPane.Status.RED;
        } else {
            AtomicBoolean ok = new AtomicBoolean(true);
            projectRequisites.forEach((String t) -> {
                String p = projectConfig.getProperty(t);
                if (p == null || p.isEmpty()) {
                    ok.set(false);
                }
            });
            mainRequisites.forEach((String t) -> {
                String p = mainConfig.getProperty(t);
                if (p == null || p.isEmpty()) {
                    ok.set(false);
                }
            });
            return ok.get() ? ToolPane.Status.RED : ToolPane.Status.DISABLED;
        }
    }

    /**
     * Sets the config panel for this tool. It will create the proper buttons in the tool.
     *
     * @param node The Node to set Configuration.
     * @throws NullPointerException if node is null.
     */
    public final void setConfigPane(Node node) throws NullPointerException {
        if (node == null) {
            throw new NullPointerException();
        }
        // The config button is shown when the tool is red or green.
        Button settings = new FlatButton("settings.png", "Settings");
        Button cancel = new FlatButton("cancel.png", "Cancel");
        settings.setOnAction((ActionEvent event) -> {
            toolPane.setStatus(ToolPane.Status.OPEN);
            toolPane.showPane(node);
        });
        cancel.setOnAction((ActionEvent event) -> {
            toolPane.setStatus(getProperStatus());
            toolPane.hidePane();
        });
        toolPane.addButton(ToolPane.Status.RED, settings);
        toolPane.addButton(ToolPane.Status.GREEN, settings);
        toolPane.addButton(ToolPane.Status.OPEN, cancel);
    }

    /**
     * Call this method if the tool perfoms a SystemTask. Buttons are automatically generated. A go
     * button into RED and OPEN status, and a cancel Button for RUNNING.
     *
     * @param task the SystemTask of this Step.
     *
     * @throws NullPointerException if task is null
     */
    public void setTask(SystemTask task) {
        if (task == null) {
            throw new NullPointerException();
        }
        this.task = task;
        Button go = new FlatButton("start.png", "Start task");
        Button stop = new FlatButton("stop.png", "Stop task");
        Button ok = new FlatButton("accept.png", "Accept");
        ok.setOnAction((ActionEvent event) -> {
            toolPane.hidePane();
        });
        go.setOnAction((ActionEvent event) -> {
            go();
        });
        stop.setOnAction((ActionEvent event) -> {
            this.task.cancel(true);
        });
        toolPane.addButton(ToolPane.Status.RED, go);
        toolPane.addButton(ToolPane.Status.OPEN, go);
        toolPane.addButton(ToolPane.Status.RUNNING, stop);

    }

    /**
     * Adds a button to the Step. Use this method if {@code setTask} and {@code setConfigPane} are
     * not enough.
     *
     * @param status In which status.
     * @param button The button. Please, make sure it it a 16x16 ImageView to accomplish the
     * standard.
     */
    public void addButton(ToolPane.Status status, Button button) {
        toolPane.addButton(status, button);
    }

    /**
     * Bring the action!!!. Launches the SystemTask of the Step. Creates a Console, puts the
     * ToolPane into RUNNING Status, and binds progress to the ToolPane and output to the Console.
     */
    private void go() {
        try {
            task = task.getClass().newInstance();
            if (task.configure(mainConfig, projectConfig)) {

                Console console = new Console();
                toolPane.showPane(console.getView()); // Show console.
                task.setPrintStream(console.getPrintStream()); // Bind to task.
                // What to do when the task finishes.
                task.setOnSucceeded((WorkerStateEvent event) -> {
                    taskSucceeded();
                });
                task.setOnCancelled((WorkerStateEvent event) -> {
                    taskCancelled();
                });
                // Bind progress.
                task.progressProperty().addListener(
                        (ObservableValue<? extends Number> obs, Number o,
                                Number n) -> {
                    toolPane.updateProgress(task.getMessage(), task.getProgress());
                });
                task.messageProperty().addListener((ObservableValue<? extends String> obs, String o,
                        String n) -> {
                    toolPane.updateProgress(task.getMessage(), task.getProgress());
                });
                // Go go go! Fire in the hole!!
                toolPane.setStatus(ToolPane.Status.RUNNING);
                new Thread(task).start();
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Step.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Called when task is succeeded.
     */
    private void taskSucceeded() {
        try {
            if (task.get() == 0) {
                setCompleted();
            } else {
                toolPane.setStatus(ToolPane.Status.RED);
            }
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(Step.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Called when task is canceled, interrupted...
     */
    private void taskCancelled() {
        toolPane.setStatus(ToolPane.Status.RED);
        projectConfig.removeProperty(code + "_date");
    }

    /**
     * Gets the Project configuration.
     *
     * @return
     */
    public Config getConfig() {
        return projectConfig;
    }

    /**
     * This method should be called from a Config object, to tell this Step that there are changes
     * into config. In fact, this method will do big magic. When a task is finished, it changes the
     * configuration, in that moment the rest of Steps are notified and can change their status.
     */
    public void configChanged() {
        toolPane.setStatus(getProperStatus());
    }

    /**
     * If this Step can be completed without using a SystemTask, the Step will not change the Status
     * to GREEN automatically, so it is necessary to call this method manually.
     */
    public void setCompleted() {
        projectConfig.setProperty(code + "_date", df.format(System.currentTimeMillis()));
        toolPane.setStatus(ToolPane.Status.GREEN);
        Button accept = new FlatButton("accept.png", "Accept");
        toolPane.addButton(ToolPane.Status.GREEN, accept);
        accept.setOnAction((ActionEvent event) -> {
            toolPane.hidePane();
            toolPane.removeButton(ToolPane.Status.GREEN, accept);
        });
    }

    /**
     * Tells if the task of this Step is running.
     *
     * @return true if task is running, false if it is null or ins not running.
     */
    public boolean isRunning() {
        return task != null ? task.isRunning() : false;
    }

    /**
     * Stops the execution of the task. Of course it must be running.
     */
    public void stop() {
        task.cancel(true);
    }

    /**
     * Gets the ToolPane.
     *
     * @return the toolPane,
     */
    public ToolPane getToolPane() {
        return toolPane;
    }

}
