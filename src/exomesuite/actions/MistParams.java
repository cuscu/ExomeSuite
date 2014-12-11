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
package exomesuite.actions;

import exomesuite.MainViewController;
import exomesuite.graphic.ChoiceParam;
import exomesuite.graphic.NumberParam;
import exomesuite.graphic.SizableImage;
import exomesuite.graphic.TextParam;
import java.util.List;
import java.util.Properties;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class. The controller of the windows with the parameters for the MIST call.
 * Parameters are passed out using a Properties element with keys "threshold", "input" and "length".
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class MistParams extends VBox {

    @FXML
    private ChoiceParam bamFile;
    @FXML
    private TextParam threshold;
    @FXML
    private NumberParam length;
    @FXML
    private Button accept;
    @FXML
    private Button cancel;

    private EventHandler closeEvent;

    private final Properties properties;

    private boolean accepted = false;

    public MistParams(Properties properties) {
        this.properties = properties;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MistParams.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            MainViewController.printException(e);
        }
    }

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        bamFile.setOnValueChanged(e -> enableAccept());
        if (properties.containsKey("bamFile")) {
            bamFile.setValue(properties.getProperty("bamFile"));
        }
        if (properties.containsKey("threshold")) {
            threshold.setValue(properties.getProperty("threshold"));
        }
        if (properties.containsKey("length")) {
            length.setValue(Double.valueOf(properties.getProperty("length")));
        }
        accept.setOnAction(event -> {
            accepted = true;
            closeEvent.handle(event);
        });
        cancel.setOnAction(event -> closeEvent.handle(event));
        accept.setGraphic(new SizableImage("exomesuite/img/mist.png", 32));
        cancel.setGraphic(new SizableImage("exomesuite/img/cancel.png", 32));
    }

    /**
     * Provide options for the bam param.
     *
     * @param options
     */
    public void setBamOptions(List<String> options) {
        bamFile.setOptions(options);
    }

    public void enableAccept() {
        if (bamFile.getValue() != null && !bamFile.getValue().isEmpty()) {
            accept.setDisable(false);
        }
    }

    public void setOnAccept(EventHandler eventHandler) {
        closeEvent = eventHandler;
    }

    public boolean accept() {
        return accepted;
    }

    /**
     * Get the parameters values in a Properties object. Keys are "threshold", "length" and "input".
     *
     * @return a Properties object with the parameters
     */
    public Properties getParams() {
        properties.setProperty("threshold", threshold.getValue());
        properties.setProperty("length", String.valueOf(length.getValue()));
        properties.setProperty("bamFile", bamFile.getValue());
        return properties;
    }
}
