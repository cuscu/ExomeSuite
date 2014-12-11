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

import exomesuite.graphic.ChoiceParam;
import exomesuite.graphic.FileParam;
import exomesuite.graphic.PathParam;
import exomesuite.graphic.TextParam;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class NewProjectViewController {

    @FXML
    private TextParam name;
    @FXML
    private PathParam path;
    @FXML
    private Button acceptButton;
    @FXML
    private Label finalPath;
    @FXML
    private FileParam forward;
    @FXML
    private FileParam reverse;
    @FXML
    private TextParam code;
    @FXML
    private ChoiceParam genome;
    @FXML
    private ChoiceParam encoding;
    @FXML
    private VBox root;
    private EventHandler handler;

    private boolean codeChangedManually;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        root.getStylesheets().add("/exomesuite/main.css");
        // Updates the created folder name as user types the name
        code.setOnKeyTyped(e -> updateFolder());
        forward.addFilter(FileManager.FASTQ_FILTER);
        reverse.addFilter(FileManager.FASTQ_FILTER);
        path.setOnValueChanged((EventHandler) (Event event) -> updateFolder());
        // Set genome
        genome.setOptions(OS.getSupportedReferenceGenomes());
        // Set encoding
        encoding.setOptions(OS.getSupportedEncodings());
        acceptButton.setOnAction((ActionEvent event) -> handler.handle(event));
        name.setOnValueChanged((EventHandler) (Event event) -> {
            if (!codeChangedManually) {
                code.setValue(name.getValue().replace(" ", "_").toLowerCase());
            }
        });
    }

    /**
     * Returns the selected path.
     *
     * @return the path
     */
    public File getPath() {
        return path.getValue();
    }

    /**
     * Gets the name of the project.
     *
     * @return the name
     */
    public String getName() {
        return name.getValue();
    }

    /**
     * Updates the text of the whole path label.
     */
    private void updateFolder() {
        codeChangedManually = true;
        if (path.getValue() != null) {
            finalPath.
                    setText(path.getValue() + File.separator + code.getValue() + " will be created.");
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
    public File getForward() {
        return forward.getValue();
    }

    /**
     * Gets the reverse file.
     *
     * @return the reverse file
     */
    public File getReverse() {
        return reverse.getValue();
    }

    public String getReference() {
        return genome.getValue();
    }

    public String getEncoding() {
        return encoding.getValue();
    }

    public void setHandler(EventHandler handler) {
        this.handler = handler;
    }

    String getCode() {
        return code.getValue();
    }

}
