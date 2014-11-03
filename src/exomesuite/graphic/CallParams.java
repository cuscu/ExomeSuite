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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class CallParams {

    @FXML
    private Parameter bamFile;
    @FXML
    private Parameter algorithm;
    @FXML
    private Button accept;
    private EventHandler acceptEvent;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        accept.setDisable(true);
        bamFile.setOnValueChanged(e -> accept.setDisable(false));
        accept.setOnAction((ActionEvent event) -> {
            acceptEvent.handle(event);
        });
    }

    public void setBamOptions(List<String> bams) {
        bamFile.setOptions(bams);
    }

    public String getSelectedBam() {
        return bamFile.getValue();
    }

    public void setOnAccept(EventHandler eventHandler) {
        this.acceptEvent = eventHandler;
    }

    public void setAlgorithmOptions(List<String> options) {
        algorithm.setOptions(options);
    }

    public String getAlgorithm() {
        return algorithm.getValue();
    }

}
