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
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;

/**
 * FXML Controller class. Node of a process in the working area.
 *
 * @author Pascual Lorente Arencibia {pasculorente@gmail.com}
 */
public class TaskPanel {

    @FXML
    private Label message;
    @FXML
    private ProgressBar progress;
    @FXML
    private TextArea textArea;
    @FXML
    private Button cancel;
    @FXML
    private Label progressLabel;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        cancel.setGraphic(new SizableImage("exomesuite/img/cancel.png", SizableImage.SMALL_SIZE));
        cancel.setTooltip(new Tooltip(ExomeSuite.getResources().getString("cancel")));
        progress.progressProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.doubleValue() >= 0 && newValue.doubleValue() <= 1) {
                progressLabel.setText(String.format("%.2f%%", newValue.doubleValue() * 100.0));
            } else {
                progressLabel.setText(null);
            }
        });
        progress.visibleProperty().addListener((obs, oldValue, newValue) -> {
            progressLabel.setVisible(newValue);
        });
    }

    /**
     * Gets the message label.
     *
     * @return the message
     */
    public Label getMessage() {
        return message;
    }

    /**
     * Gets the progress bar.
     *
     * @return the progress bar
     */
    public ProgressBar getProgress() {
        return progress;
    }

    /**
     * Gets the text area
     *
     * @return the text area
     */
    public TextArea getTextArea() {
        return textArea;
    }

    /**
     * Gets the cancel button
     *
     * @return the cancel button
     */
    public Button getCancelButton() {
        return cancel;
    }

}
