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
import java.text.SimpleDateFormat;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class Step {

    private final ToolPane toolPane;
    private final String name;
    private Config projectConfig, mainConfig, stepConfig;
    private ToolPane.Status previousStatus;
    private SystemTask task;

    /**
     * This will create a directory within the project and a Config file within the directory.
     *
     * @param project The project where this step belongs.
     * @param directory A directory to create within the project. If null, no directory will be
     * created.
     * @param name Name of the tool.
     */
    public Step(Project project, String directory, String name) {
        this.name = name;
        if (directory != null) {
            File dir = new File(project.getPath(), directory);
            dir.mkdirs();
            stepConfig = new Config(new File(dir, directory + ".config"));
            mainConfig = MainViewController.getConfig();
            projectConfig = project.getConfig();
        }
        toolPane = initializeToolPane(name);
    }

    /**
     * Gets in which status should be the tool. This method can be call at any time.
     *
     * @return
     */
    abstract ToolPane.Status getProperStatus(Config projectConfig, Config mainConfig,
            Config stepConfig);

    abstract SystemTask getSystemTask(Config projectConfig, Config mainConfig, Config stepConfig);

    /**
     * Sets the config panel for this tool. It will create the proper buttons in the tool.
     *
     * @param node
     */
    public void setConfigPane(Node node) {
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

    /**
     * Call this method if the tool perfoms a SystemTask.
     *
     * @param task
     */
    public void setTask(SystemTask task) {
        if (task != null) {
            this.task = task;
            Button go = new Button(null, new ImageView("exomesuite/img/r_arrow.png"));
            Button cancel = new Button(null, new ImageView("exomesuite/img/cancel.png"));
            go.setOnAction((ActionEvent event) -> {
                go();
            });
            cancel.setOnAction((ActionEvent event) -> {
                cancel();
            });
            toolPane.addButton(ToolPane.Status.RED, go);
            toolPane.addButton(ToolPane.Status.OPEN, go);
            toolPane.addButton(ToolPane.Status.RUNNING, cancel);

        }
    }

    private ToolPane initializeToolPane(String name) {
        ToolPane pane = new ToolPane(name, ToolPane.Status.DISABLED, name);
        return pane;
    }

    private void go() {
        task.configure(mainConfig, projectConfig, stepConfig);
        Console console = new Console();
        task.setPrintStream(console.getPrintStream());
        task.setOnSucceeded((WorkerStateEvent event) -> {
            taskSucceeded();
        });
        task.setOnCancelled((WorkerStateEvent event) -> {
            taskCancelled();
        });

        new Thread(task).start();
    }

    private void cancel() {
        task.cancel();
        toolPane.setStatus(ToolPane.Status.RED);
    }

    private void taskSucceeded() {
        String timestamp = new SimpleDateFormat("yyMMdd").format(System.currentTimeMillis());
        stepConfig.setProperty("end_date", timestamp);
        projectConfig.setProperty(name + "_date", timestamp);
        toolPane.setStatus(ToolPane.Status.GREEN);
    }

    private void taskCancelled() {
        toolPane.setStatus(ToolPane.Status.RED);
        stepConfig.removeProperty("end_date");
        projectConfig.removeProperty(name + "_date");
    }
}
