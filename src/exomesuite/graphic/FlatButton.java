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

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

/**
 * A FlatButton is the standard Button in ExomeSuite. They are styled in the same way.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class FlatButton extends Button {

    private StringProperty icon = new SimpleStringProperty();

    public FlatButton() {
        getStyleClass().add("flat-button");
    }

    /**
     * Creates a new FlatButton. iconName is the name of a file in the exomesuite/img Folder.
     *
     * @param iconName the icon from the exomesuite/img folder
     * @param tooltip a text for the tooltip or null if no tooltip wanted
     */
    public FlatButton(String iconName, String tooltip) {
        super(null, new ImageView("exomesuite/img/" + iconName));
        if (tooltip != null && !tooltip.isEmpty()) {
            setTooltip(new Tooltip(tooltip));
        }
        getStyleClass().add("flat-button");
    }

    /**
     * Changes the icon of the button.
     *
     * @param iconName the icon from exomesuite/img
     */
    public void setIcon(String iconName) {
        setGraphic(new ImageView("exomesuite/img/" + iconName));
        icon.set(iconName);
    }

    /**
     * Returns the property of the name of the icon.
     *
     * @return the property of the name of the icon.
     */
    public StringProperty getIconProperty() {
        return icon;
    }

    /**
     * Gets the name of the icon.
     *
     * @return the name of the icon
     */
    public String getIcon() {
        return icon.get();
    }

}
