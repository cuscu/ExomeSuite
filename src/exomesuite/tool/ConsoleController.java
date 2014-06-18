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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class ConsoleController {

    @FXML
    private TextArea console;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
    }

    public void addText(String text) {
        Platform.runLater(() -> {
            console.appendText(text);
        });
    }

    public void clear() {
        console.setText("");
    }

}
