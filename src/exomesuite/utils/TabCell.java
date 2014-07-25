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
package exomesuite.utils;

import javafx.geometry.Insets;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class TabCell extends TableCell<String[], String> {

    TextField textField;

    public TabCell() {
        textField = new TextField();
        textField.setEditable(false);
        textField.setBackground(Background.EMPTY);
        textField.setPadding(new Insets(1));
        textField.setOnMouseClicked((MouseEvent event) -> {
            textField.selectAll();
        });
        textField.setTooltip(new Tooltip());
    }

    @Override
    protected void updateItem(String t, boolean bln) {
        super.updateItem(t, bln);
        textField.setText(getItem());
        setGraphic(textField);
        textField.getTooltip().setText(t);
    }

}
