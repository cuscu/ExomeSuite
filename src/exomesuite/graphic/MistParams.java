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
import java.util.Properties;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * FXML Controller class. The controller of the windows with the parameters for the MIST call.
 * Parameters are passed out using a Properties element with keys "threshold", "input" and "length".
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class MistParams {

    @FXML
    private Parameter bamFile;
    @FXML
    private Parameter threshold;
    @FXML
    private Parameter length;
    @FXML
    private Button accept;

    private EventHandler closeEvent;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        accept.setDisable(true);
        bamFile.setOnValueChanged(e -> enableAccept());
        accept.setOnAction(e -> accept(e));
    }

    public void setBamOptions(List<String> options) {
        bamFile.setOptions(options);
    }

    public void enableAccept() {
        if (bamFile.getValue() != null && !bamFile.getValue().isEmpty()) {
            accept.setDisable(false);
        }
    }

    private void accept(Event e) {
        if (closeEvent != null) {
            closeEvent.handle(e);
        }
    }

    public void setOnAccept(EventHandler eventHandler) {
        closeEvent = eventHandler;
    }

    /**
     * Get the parameters values in a Properties object. Keys are "threshold", "length" and "input".
     *
     * @return a Properties object with the parameters
     */
    public Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("threshold", threshold.getValue());
        properties.setProperty("length", length.getValue());
        properties.setProperty("input", bamFile.getValue());
        return properties;
    }
}
