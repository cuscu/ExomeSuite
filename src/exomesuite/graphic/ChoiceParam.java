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
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

/**
 * A Param with a combobox as editor.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ChoiceParam extends Param<String> {

    final ComboBox<String> options = new ComboBox<>();
    private final Button cancel = new Button(null, new SizableImage("exomesuite/img/cancel.png", 16));

    /**
     * Create a new ChoiceParam. Call {@code setOtions(List options)} to fill the combobox.
     *
     */
    public ChoiceParam() {
        cancel.setOnAction(e -> endEdit(false, null));
        options.setOnAction(e -> endEdit(true, options.getValue()));
        cancel.getStyleClass().add("graphic-button");
    }

    @Override
    protected Node getEditingPane() {
        options.getSelectionModel().select(getValue());
//        options.show();
        return new HBox(options, cancel);
    }

    /**
     * Set the list of options.
     *
     * @param options the new list of options
     */
    public void setOptions(List<String> options) {
        this.options.getItems().setAll(options);
        this.options.getSelectionModel().select(0);
        if (getValue() == null || getValue().isEmpty()) {
            setValue(options.get(0));
        }
    }

}
