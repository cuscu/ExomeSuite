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
import exomesuite.systemtask.SystemTask;
import exomesuite.tool.Console;
import exomesuite.tool.ToolPane;
import exomesuite.utils.Config;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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
public class Step {

    private final ToolPane toolPane;
    private final String title;
    private final String code;
    private final Config projectConfig, mainConfig;
    private ToolPane.Status previousStatus;
    private SystemTask task;
    private final List<String> projectRequisites;
    private final List<String> mainRequisites;
    private final DateFormat df = new SimpleDateFormat("yyMMdd");

    /**
     * This will create a directory within the project and a Config file within the directory.
     * Default tool do nothing, add an action and/or a config.
     *
     * @param project The project where this step belongs.
     * @param code A code for this tool.
     * @param mkdir If true, a folder with code name will be created.
     * @param title Name of the tool.
     * @param ico Some nice 16x16 image.
     * @param projectReqs
     * @param appReqs
     * @param configPane
     * @param task
     */
    public Step(Project project, String code, boolean mkdir, String title, ImageView ico,
            List<String> projectReqs, List<String> appReqs, Node configPane, SystemTask task) {
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
        setConfigPane(configPane);
        setTask(task);
    }

    private ToolPane.Status getProperStatus() {
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
     * @param node
     */
    private void setConfigPane(Node node) {
        if (node != null) {
            // The config button is shown when the tool is red or green.
            Button button = new Button(null, new ImageView("exomesuite/img/gear.png"));
            Button cancel = new Button(null, new ImageView("exomesuite/img/accept.png"));
            button.setOnAction((ActionEvent event) -> {
                previousStatus = toolPane.getStatus(); //Red or green?
                toolPane.setStatus(ToolPane.Status.OPEN);
                toolPane.showPane(node);
            });
            cancel.setOnAction((ActionEvent event) -> {
                toolPane.setStatus(previousStatus);
                toolPane.hidePane();
            });
            toolPane.addButton(ToolPane.Status.RED, button);
            toolPane.addButton(ToolPane.Status.GREEN, button);
            toolPane.addButton(ToolPane.Status.OPEN, cancel);
        }
    }

    /**
     * Call this method if the tool perfoms a SystemTask.
     *
     * @param task
     */
    private void setTask(SystemTask task) {
        if (task != null) {
            this.task = task;
            Button go = new Button(null, new ImageView("exomesuite/img/r_arrow.png"));
            Button cancel = new Button(null, new ImageView("exomesuite/img/cancel.png"));
            Button ok = new Button(null, new ImageView("exomesuite/img/accept.png"));
            ok.setOnAction((ActionEvent event) -> {
                toolPane.hidePane();
            });
            go.setOnAction((ActionEvent event) -> {
                go();
            });
            cancel.setOnAction((ActionEvent event) -> {
                task.cancel(true);
            });
            toolPane.addButton(ToolPane.Status.RED, go);
            toolPane.addButton(ToolPane.Status.OPEN, go);
            toolPane.addButton(ToolPane.Status.RUNNING, cancel);
            toolPane.addButton(ToolPane.Status.GREEN, ok);
        }
    }

    /**
     * Bring the action!!!.
     */
    private void go() {
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
            task.progressProperty().addListener((ObservableValue<? extends Number> obs, Number o,
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
    }

    /**
     * Called when task is succeeded.
     */
    private void taskSucceeded() {
        setCompleted();
    }

    /**
     * Called when task is canceled, interrupted...
     */
    private void taskCancelled() {
        toolPane.setStatus(ToolPane.Status.RED);
        projectConfig.removeProperty(code + "_date");
    }

    public Config getConfig() {
        return projectConfig;
    }

    public ToolPane getToolPane() {
        return toolPane;
    }

    public void configChanged() {
        toolPane.setStatus(getProperStatus());
    }

    public void setCompleted() {
        projectConfig.setProperty(code + "_date", df.format(System.currentTimeMillis()));
        toolPane.setStatus(ToolPane.Status.GREEN);
    }

    public boolean isRunning() {
        if (task != null) {
            return task.isRunning();
        }
        return false;
    }

    public void stop() {
        task.cancel(true);
    }
}
