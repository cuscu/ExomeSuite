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
package exomesuite;

import exomesuite.graphic.FileSelector;
import exomesuite.utils.OS;
import java.io.File;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class NewProjectViewController {

    @FXML
    private TextField name;
    @FXML
    private FileSelector path;
    @FXML
    private Button acceptButton;
    @FXML
    private Label finalPath;
    @FXML
    private FileSelector forward;
    @FXML
    private FileSelector reverse;
    @FXML
    private TextField code;
    @FXML
    private ComboBox<String> reference;
    @FXML
    private ComboBox<String> encoding;

    private EventHandler handler;

    private Map<String, String> genomes;
    private Map<String, String> encodings;

    private boolean codeChangedManually;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        code.setOnKeyTyped((KeyEvent event) -> updateFolder());
        forward.addExtensionFilters(OS.FASTQ_FILTER);
        reverse.addExtensionFilter(OS.FASTQ_FILTER);
        path.setOnFileChange((EventHandler) (Event event) -> updateFolder());
        // Set genome
        genomes = OS.getSupportedReferenceGenomes();
        reference.getItems().addAll(genomes.keySet());
        // Set encoding
        encodings = OS.getSupportedEncodings();
        encoding.getItems().addAll(encodings.keySet());
        acceptButton.setOnAction((ActionEvent event) -> handler.handle(event));
        name.setOnKeyReleased((KeyEvent event) -> {
            if (!codeChangedManually) {
                code.setText(name.getText().replace(" ", "_").toLowerCase());
            }
        });
    }

    /**
     * Returns the selected path.
     *
     * @return the path
     */
    public String getPath() {
        return path.getFile();
    }

    /**
     * Gets the name of the project.
     *
     * @return the name
     */
    public String getName() {
        return name.getText();
    }

    /**
     * Updates the text of the whole path label.
     */
    private void updateFolder() {
        codeChangedManually = true;
        if (path.getFile() != null) {
            finalPath.
                    setText(path.getFile() + File.separator + code.getText() + " will be created.");
        }
    }

    /**
     * Gets the accept button.
     *
     * @return the accept button.
     */
    public Button getAcceptButton() {
        return acceptButton;
    }

    /**
     * Gets the forward file.
     *
     * @return the forward file
     */
    public String getForward() {
        return forward.getFile();
    }

    /**
     * Gets the reverse file.
     *
     * @return the reverse file
     */
    public String getReverse() {
        return reverse.getFile();
    }

    public String getReference() {
        return reference.getValue() != null ? genomes.get(reference.getValue()) : null;
    }

    public String getEncoding() {
        return encoding.getValue() != null ? encodings.get(encoding.getValue()) : null;
    }

    public void setHandler(EventHandler handler) {
        this.handler = handler;
    }

    String getCode() {
        return code.getText();
    }

}
