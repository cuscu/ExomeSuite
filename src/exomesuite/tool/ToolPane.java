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

import exomesuite.phase.Step;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * Manages the view of a Step. When creating a new Step, override {@link Step}, it will contain a
 * ToolPane, but it is possible to use it outside a Step.
 * <p>
 * This view is made of two parts. A header, which contains the title, the icon, the status bar and
 * the buttons, and a content pane, which shows the configuration pane or the console.
 *
 * @author Pascual Lorente Arencibia
 */
public final class ToolPane {

    /**
     * enum with all the possible status of ToolPane.
     */
    public enum Status {

        /**
         * the tool is ready to be executed.
         */
        RED,
        /**
         * the tool has been completed.
         */
        GREEN,
        /**
         * The tool has a task running.
         */
        RUNNING,
        /**
         * the tool cannot be executed, because there are some missing configuration.
         */
        DISABLED,
        /**
         * The tool is displaying the configuration pane.
         */
        OPEN
    }

    /**
     * The lists of Buttons.
     */
    private final Map<Status, List<Button>> aButtons = new TreeMap<>();

    /**
     * Current status of the ToolPane.
     */
    private Status status;

    /**
     * The controller of the view.
     */
    ToolViewController controller;

    /**
     * The view.
     */
    private Node view;

    /**
     * Creates an empty toolPane which does nothing but being pretty useless.
     *
     * @param name name to be displayed
     * @param status initial status
     * @param icon a beautiful 16x16 ImageView that represents this Tool
     */
    public ToolPane(String name, Status status, ImageView icon) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ToolView.fxml"));
            loader.load();
            controller = loader.getController();
            view = loader.getRoot();
        } catch (IOException ex) {
            Logger.getLogger(ToolPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        controller.getName().setText(name);
        if (icon != null) {
            controller.getName().setGraphic(icon);
        }
        setStatus(status);
        hidePane();
    }

    /**
     * Adds a button to the desired Status.
     *
     * @param status the status
     * @param button the button
     */
    public void addButton(Status status, Button button) {
        if (aButtons.containsKey(status)) {
            aButtons.get(status).add(button);
        } else {
            List<Button> b = new ArrayList<>();
            b.add(button);
            aButtons.put(status, b);
        }
        // Don't forget to update the view.
        updateButtons();
    }

    /**
     * Removes a button from the desired status. If the button is not in the status, does nothing.
     *
     * @param status the status
     * @param button the button
     */
    public void removeButton(Status status, Button button) {
        aButtons.get(status).remove(button);
        updateButtons();
    }

    /**
     * Displays the node in the content pane.
     *
     * @param node a node
     */
    public void showPane(Node node) {
        controller.show(node);
    }

    /**
     * Hides the content.
     */
    public void hidePane() {
        controller.hide();
    }

    /**
     * Sets the name of the ToolPane.
     *
     * @param name the name
     */
    public void setName(String name) {
        controller.getName().setText(name);
    }

    /**
     * Gets the name of the ToolPane.
     *
     * @return the name
     */
    public String getName() {
        return controller.getName().getText();
    }

    /**
     * Gets the current status of the ToolPane.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Sets the status of the ToolPane.
     *
     * @param status the status
     */
    public void setStatus(Status status) {
        this.status = status;
        updateButtons();
        controller.getHeader().getStyleClass().clear();
        switch (status) {
            case DISABLED:
                controller.getHeader().getStyleClass().add("disabled");
                break;
            case GREEN:
                controller.getHeader().getStyleClass().add("green");
                break;
            case RED:
                controller.getHeader().getStyleClass().add("red");
                break;
            case OPEN:
                controller.getHeader().getStyleClass().add("open");
                break;
            case RUNNING:
                controller.getHeader().getStyleClass().add("progress");
                break;
        }
        if (status != Status.RUNNING) {
            updateProgress("", -1);
        }
    }

    /**
     * Returns the view.
     *
     * @return the view
     */
    public Node getView() {
        return view;
    }

    /**
     * Updates the view of the buttons.
     */
    private void updateButtons() {
        controller.getButtons().getChildren().clear();
        if (aButtons.get(status) != null) {
            aButtons.get(status).forEach(controller.getButtons().getChildren()::add);
        }
    }

    /**
     * If the task is running, updates the progress bar and the message.
     *
     * @param message A message to keep the user busy while you do your staff.
     * @param progress A number between 0 and 1.
     */
    public void updateProgress(String message, double progress) {
        controller.setProgress(message, progress);
    }
}
