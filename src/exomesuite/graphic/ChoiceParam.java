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

import java.util.List;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class ChoiceParam extends Param {

    final ComboBox<String> options = new ComboBox<>();
    private final FlatButton accept = new FlatButton("apply.png", "Accept");
    private final FlatButton cancel = new FlatButton("cancel4.png", "Cancel");

    public ChoiceParam() {
        // Accept by button
        accept.setOnAction(e -> endEdit(true, options.getValue()));
        // Cancel by button
        cancel.setOnAction(e -> endEdit(false, null));
    }

    @Override
    protected Node getEditingPane() {
        return new HBox(options, accept, cancel);
    }

    public void setOptions(List<String> options) {
        this.options.getItems().setAll(options);
        this.options.getSelectionModel().select(0);
    }

}
