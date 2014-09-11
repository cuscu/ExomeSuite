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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class FlatButton extends Button {

    private StringProperty icon = new SimpleStringProperty();

    public FlatButton() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FlatButton.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(FlatButton.class.getName()).log(Level.SEVERE, null, ex);
        }
        setBackground(Background.EMPTY);
    }

    public FlatButton(String iconName, String tooltip) {
        super(null, new ImageView("exomesuite/img/" + iconName));
        if (tooltip != null && !tooltip.isEmpty()) {
            setTooltip(new Tooltip(tooltip));
        }
        setBackground(Background.EMPTY);
    }

//    public String getIcon() {
//        return this.getGraphic().toString();
//    }
    public void setIcon(String iconName) {
        setGraphic(new ImageView("exomesuite/img/" + iconName));
        icon.set(iconName);
    }

    public StringProperty getIconProperty() {
        return icon;
    }

    public String getIcon() {
        return icon.get();
    }

}
