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

import exomesuite.ExomeSuite;
import exomesuite.graphic.SizableImage;
import exomesuite.utils.FileManager;
import java.io.File;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class AlignerParameters {

    @FXML
    private TextField forward;
    @FXML
    private TextField reverse;
    @FXML
    private ComboBox<String> encoding;
    @FXML
    private ComboBox<String> reference;
    @FXML
    private Button cancel;
    @FXML
    private Button accept;
    @FXML
    private Button browseForward;
    @FXML
    private Button browseReverse;

    /**
     * If user clicked on accpet.
     */
    private boolean accepted = false;
    /**
     * What happens whe user click on accept or cancel.
     */
    private EventHandler handler;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        browseForward.setOnAction(event -> {
            File f = FileManager.openFile(ExomeSuite.getResources().getString("select.file"),
                    FileManager.FASTQ_FILTER);
            if (f != null) {
                forward.setText(f.getAbsolutePath());
            }
        });
        browseReverse.setOnAction(event -> {
            File f = FileManager.openFile(ExomeSuite.getResources().getString("select.file"),
                    FileManager.FASTQ_FILTER);
            if (f != null) {
                reverse.setText(f.getAbsolutePath());
            }
        });
        accept.setOnAction(event -> {
            accepted = true;
            handler.handle(event);
        });
        cancel.setOnAction(event -> handler.handle(event));
        accept.setGraphic(new SizableImage("exomesuite/img/align.png", SizableImage.SMALL_SIZE));
        cancel.setGraphic(new SizableImage("exomesuite/img/cancel.png", SizableImage.SMALL_SIZE));

    }

    /**
     * Set the text for the forward file.
     *
     * @param file
     */
    public void setForward(String file) {
        forward.setText(file);
    }

    /**
     * Set the text for the reverse file.
     *
     * @param file
     */
    public void setReverse(String file) {
        reverse.setText(file);
    }

    /**
     * You must specify first encoding options. This method selects the default encoding.
     *
     * @param encoding
     */
    public void setEncoding(String encoding) {
        this.encoding.getSelectionModel().select(encoding);
    }

    /**
     * Set the options of the encodings comboBox.
     *
     * @param options
     */
    public void setEncodingOptions(List<String> options) {
        encoding.getItems().setAll(options);
    }

    /**
     * Set the options of the encodings comboBox.
     *
     * @param options
     */
    public void setEncodingOptions(String... options) {
        encoding.getItems().setAll(options);
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
     * Gets the selected forward file.
     *
     * @return
     */
    public String getSelectedForward() {
        return forward.getText();
    }

    /**
     * Gets the selected reverse file.
     *
     * @return
     */
    public String getSelectedReverse() {
        return reverse.getText();
    }

    /**
     * Gets the selected encoding.
     *
     * @return
     */
    public String getEncoding() {
        return encoding.getValue();
    }

    /**
     * Gets the selected reference.
     *
     * @return
     */
    public String getReference() {
        return reference.getValue();
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
