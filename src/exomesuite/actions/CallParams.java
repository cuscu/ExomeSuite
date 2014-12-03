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
import java.util.List;
import java.util.Properties;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class. This is the windows when user wants to Call Variants in a project.
 *
 * @author Pascual Lorente Arencibia
 */
public class CallParams extends VBox {

    @FXML
    private ChoiceParam bamFile;
    @FXML
    private ChoiceParam algorithm;
    @FXML
    private Button accept;
    @FXML
    private Button cancel;
    private EventHandler acceptEvent;
    private boolean accepted = false;
    private Properties params;

    public CallParams(Properties properties) {
        this.params = properties;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CallParams.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            MainViewController.showException(e);
        }
    }

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        accept.setOnAction(event -> {
            accepted = true;
            acceptEvent.handle(event);
        });
        cancel.setOnAction(event -> acceptEvent.handle(event));
        bamFile.setOnValueChanged(event -> params.put("bamFile", bamFile.getValue()));
        algorithm.setOnValueChanged(event -> params.put("algorithm", algorithm.getValue()));
    }

    public void setBamOptions(List<String> bams) {
        bamFile.setOptions(bams);
    }

    public void setOnClose(EventHandler eventHandler) {
        this.acceptEvent = eventHandler;
    }

    public void setAlgorithmOptions(List<String> options) {
        algorithm.setOptions(options);
    }

    public boolean accept() {
        return accepted;
    }

    public Properties getParams() {
        params.setProperty("algorithm", algorithm.getValue());
        params.setProperty("bamFile", bamFile.getValue());
        return params;
    }

}
