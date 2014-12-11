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

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

/**
 * Text Parameter. Uses a TextField to modify it.
 *
 * @author Pascual Lorente Arencibia
 */
public class TextParam extends Param<String> {

    private final TextField field = new TextField();
    private final Button accept = new Button(null, new SizableImage("exomesuite/img/accept.png", 16));
    private final Button cancel = new Button(null, new SizableImage("exomesuite/img/cancel.png", 16));

    public TextParam() {
        // Accept by button
        accept.setOnAction(e -> endEdit(true, field.getText()));
        // Cancel by button
        cancel.setOnAction(e -> endEdit(false, null));
        // Accept by Enter
        field.setOnAction(e -> endEdit(true, field.getText()));
        // Cancel by Esc
        field.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                endEdit(false, null);
            }
        });
    }

    @Override
    protected Node getEditingPane() {
        if (getValue() != null) {
            field.setText(getValue());
        }
        field.setPromptText(getPromptText());
        Platform.runLater(() -> field.requestFocus());
        return new HBox(field, accept, cancel);
    }

}
