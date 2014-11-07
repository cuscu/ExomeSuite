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

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

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

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        cancel.setGraphic(new ImageView("/exomesuite/img/cancel4.png"));
        cancel.setTooltip(new Tooltip(cancel.getText()));
        cancel.setText(null);
    }

    public Label getMessage() {
        return message;
    }

    public ProgressBar getProgress() {
        return progress;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public Button getCancelButton() {
        return cancel;
    }

}
