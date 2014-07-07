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

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class ToolViewController {

    @FXML
    private HBox header;
    @FXML
    private HBox buttons;
    @FXML
    private Label title;
    @FXML
    private Label progressText;
    @FXML
    private Region progress;

    private double progressValue;
    private Node node;

    private String message = "";
    @FXML
    private VBox tool;

    public void initialize() {
        tool.getStyleClass().add("tool");
        header.getStyleClass().add("header");
        progress.getStyleClass().add("progress");
        title.setMaxWidth(Double.MAX_VALUE);
        progressText.setMaxWidth(Double.MAX_VALUE);
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.widthProperty().addListener((ObservableValue<? extends Number> observable,
                Number oldValue, Number newValue) -> {
            setProgress(message, progressValue);
        });
    }

    public HBox getButtons() {
        return buttons;
    }

    public HBox getHeader() {
        return header;
    }

    public Label getName() {
        return title;
    }

    public void show(Node node) {
        tool.getChildren().remove(this.node);
        tool.getChildren().add(node);
        this.node = node;
    }

    public void hide() {
        tool.getChildren().remove(node);
    }

    public void setProgress(String message, double d) {
        if (d < 0.01 || d > 0.99) {
            progressValue = 0.0;
            this.message = "";
            progress.setBackground(Background.EMPTY);
            progressText.setText("");
            return;
        }
        progressValue = d;
        this.message = message;
        double i = progress.getWidth() * (1.0 - d);
        progress.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY,
                new Insets(0, i, 0, 0))));
        int p = (int) (d * 100);
//        progressText.setText(message + " (" + p + "%) ");
        progressText.setText(String.format("%s (%d%%)", message, p));
    }
}
