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

/**
 *
 * @author Pascual Lorente Arencibia
 */
public final class ToolPane {

    public enum Status {

        RED, GREEN, RUNNING, DISABLED, OPEN
    }

    private final Map<Status, List<Button>> aButtons = new TreeMap<>();

    private Status status;

    ToolViewController controller;
    private Node view;

    public ToolPane(String name, Status status) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ToolView.fxml"));
            loader.load();
            controller = loader.getController();
            view = loader.getRoot();
            controller.getHeader().getStylesheets().add("exomesuite/styles/default.css");
        } catch (IOException ex) {
            Logger.getLogger(ToolPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        controller.getName().setText(name);
        setStatus(status);
        hidePane();
    }

    public void addButton(Status status, Button button) {
        if (aButtons.containsKey(status)) {
            aButtons.get(status).add(button);
        } else {
            List<Button> b = new ArrayList<>();
            b.add(button);
            aButtons.put(status, b);
        }
        updateButtons();
    }

    public void deleteButton(Button button) {
        aButtons.forEach((Status st, List<Button> bts) -> {
            bts.remove(button);
        });
    }

    public void showPane(Node node) {
        controller.show(node);
    }

    public void hidePane() {
        controller.hide();
    }

    public void setName(String name) {
        controller.getName().setText(name);
    }

    public String getName() {
        return controller.getName().getText();
    }

    public Status getStatus() {
        return status;
    }

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
    }

    public Node getView() {
        return view;
    }

    private void updateButtons() {
        controller.getButtons().getChildren().clear();
        if (aButtons.get(status) != null) {
            aButtons.get(status).forEach(controller.getButtons().getChildren()::add);
        }
    }

    public void updateProgress(String message, double d) {
        controller.setProgress(message, d);
    }
}
