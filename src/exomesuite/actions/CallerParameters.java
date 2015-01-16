/*
 * Copyright (C) 2015 UICHUIMI
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

import exomesuite.graphic.SizableImage;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * FXML Controller class
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class CallerParameters {

    @FXML
    private ComboBox<String> alignments;
    @FXML
    private ComboBox<String> reference;
    @FXML
    private ComboBox<String> algorithm;
    @FXML
    private Button cancel;
    @FXML
    private Button accept;

    /**
     * true if user clicked on Accept
     */
    private boolean accepted = false;
    private EventHandler handler;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        accept.setOnAction(event -> {
            accepted = true;
            handler.handle(event);
        });
        cancel.setOnAction(event -> handler.handle(event));
        accept.setGraphic(new SizableImage("exomesuite/img/call.png", SizableImage.SMALL_SIZE));
        cancel.setGraphic(new SizableImage("exomesuite/img/cancel.png", SizableImage.SMALL_SIZE));
    }

    /**
     * Sets the default selected reference. Use first {@code setReferenceOptions()}
     *
     * @param reference
     */
    public void setReference(String reference) {
        this.reference.getSelectionModel().select(reference);
    }

    /**
     * Sets the options for the comboBox of reference genomes.
     *
     * @param references
     */
    public void setReferenceOptions(List<String> references) {
        this.reference.getItems().addAll(references);
    }

    /**
     * Sets the options for the comboBox of reference genomes.
     *
     * @param references
     */
    public void setReferenceOptions(String... references) {
        this.reference.getItems().addAll(references);
    }

    /**
     * Sets the default selected alignments. Use first {@code setAlignmentsOptions()}
     *
     * @param alignments
     */
    public void setAlignments(String alignments) {
        this.alignments.getSelectionModel().select(alignments);
    }

    /**
     * Sets the options for the comboBox of alignments.
     *
     * @param alignments
     */
    public void setAlignmentsOptions(List<String> alignments) {
        this.alignments.getItems().addAll(alignments);
    }

    /**
     * Sets the options for the comboBox of alignments.
     *
     * @param alignments
     */
    public void setAlignmentsOptions(String... alignments) {
        this.alignments.getItems().addAll(alignments);
    }

    /**
     * Sets the default algorithm. Use first {@code setAlgorithmOptions()}
     *
     * @param algorithm
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm.getSelectionModel().select(algorithm);
    }

    /**
     * Sets the options for the comboBox of algorithm.
     *
     * @param algorithm
     */
    public void setAlgorithmOptions(List<String> algorithm) {
        this.algorithm.getItems().addAll(algorithm);
    }

    /**
     * Sets the options for the comboBox of algorithm.
     *
     * @param algorithm
     */
    public void setAlgorithmOptions(String... algorithm) {
        this.algorithm.getItems().addAll(algorithm);
    }

    /**
     * Gets the selected reference.
     *
     * @return
     */
    public String getSelectedReference() {
        return reference.getValue();
    }

    /**
     * Gets the selected algorithm.
     *
     * @return
     */
    public String getSelectedAlgorithm() {
        return algorithm.getValue();
    }

    /**
     * Gets the selected alignments file.
     *
     * @return
     */
    public String getSelectedAlignments() {
        return alignments.getValue();
    }

    /**
     * Event to hear when the user clicked on accept or cancel.
     *
     * @param handler method to close the window and read parameters
     */
    public void setOnClose(EventHandler handler) {
        this.handler = handler;
    }

    /**
     * true if user clicked on the Accept button.
     *
     * @return
     */
    public boolean accepted() {
        return accepted;
    }

}
