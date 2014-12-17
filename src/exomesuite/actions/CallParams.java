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
import exomesuite.graphic.SizableImage;
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

    /**
     * Creates a panel that lets the user select options to call variants.
     *
     * @param properties the initial properties
     */
    public CallParams(Properties properties) {
        this.params = properties;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CallParams.fxml"));
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
        accept.setOnAction(event -> {
            accepted = true;
            acceptEvent.handle(event);
        });
        cancel.setOnAction(event -> acceptEvent.handle(event));
        bamFile.setOnValueChanged(event -> params.put("bamFile", bamFile.getValue()));
        algorithm.setOnValueChanged(event -> params.put("algorithm", algorithm.getValue()));
        accept.setGraphic(new SizableImage("exomesuite/img/call.png", 32));
        cancel.setGraphic(new SizableImage("exomesuite/img/cancel.png", 32));
    }

    /**
     * Which BAMs should we show to the user.
     *
     * @param bams the BAM files to show
     */
    public void setBamOptions(List<String> bams) {
        bamFile.setOptions(bams);
    }

    /**
     * Method that must close this dialog and read return parameters.
     *
     * @param eventHandler the method which will close the dialog
     */
    public void setOnClose(EventHandler eventHandler) {
        this.acceptEvent = eventHandler;
    }

    /**
     * The possible algorithms to call variants (GATK or SAMTOOLS).
     *
     * @param options the possible algorithms to use for calling variants
     */
    public void setAlgorithmOptions(List<String> options) {
        algorithm.setOptions(options);
    }

    /**
     * accept is set to true when user clicks on accept button. Another way the user closed the
     * dialog, accept will be false.
     *
     * @return true if user clicked on accept
     */
    public boolean accept() {
        return accepted;
    }

    /**
     * Get the selected params of the user.
     *
     * @return the selected params
     */
    public Properties getParams() {
        params.setProperty("algorithm", algorithm.getValue());
        params.setProperty("bamFile", bamFile.getValue());
        return params;
    }

}
