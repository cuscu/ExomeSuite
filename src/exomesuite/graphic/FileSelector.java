/*
 * Copyright (C) 2014 uichuimi03
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

import exomesuite.utils.FileManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * A GUI to open Files or Directories. It overrides an HBox, which contains a VBox which contains a
 * HBox which contains a TextField and a Button. A label can be
 *
 * @author Pascual Lorente Arencibia
 */
public class FileSelector extends VBox {

    /**
     * a label with the File property name. It can be on the left or in top of the textField.
     */
    @FXML
    private Label label;
    /**
     * the textField where the file name is displayed. It is not editable.
     */
    @FXML
    private TextField textField;
    /**
     * the button to select a new File.
     */
    @FXML
    private FlatButton button;
//    /**
//     * A tricky VBox when the label is on top.
//     */
//    @FXML
//    private VBox vBox;
    /**
     * A tricky HBox when the label is on the left.
     */
    @FXML
    private HBox hBox;
    /**
     * Whether this FileSelector is used to open a path (a directory) or a File.
     */
    private boolean openPath;
    /**
     * The fired event when a File is selected.
     */
    private EventHandler event;

    /**
     * Where is the label.
     */
    public enum Position {

        LEFT, TOP
    }

    public Position textPosition;

    /**
     * Filters of the File. Fetch them from FileManager or create new ones.
     */
    private final List<FileChooser.ExtensionFilter> filters = new ArrayList<>();

    /**
     * File selector view load.
     */
    public FileSelector() {
        openPath = false;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FileSelector.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(FileSelector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Initialization.
     */
    @FXML
    public void initialize() {
        button.setOnAction((ActionEvent e) -> open());
    }

    /**
     * Make text a property by providing a Getter and a Setter.
     *
     * @param text changes the text of the label.
     */
    public void setText(String text) {
        label.setText(text);
    }

    /**
     * Gets the text from the label.
     *
     * @return the text from the label
     */
    public String getText() {
        return label.getText();
    }

    /**
     * Setter and Getter to make file a property.
     *
     * @param file changes the file.
     */
    public void setFile(String file) {
        textField.setText(file);
    }

    /**
     * Gets the current File.
     *
     * @return the current File
     */
    public String getFile() {
        return textField.getText();
    }

    /**
     * Tries to open the File or the Path depending on the openPath property.
     */
    private void open() {
        if (openPath) {
            openPath();
        } else {
            openFile();
        }
    }

    /**
     * Specify and EventHandler called when a new File or Directory is selected.
     *
     * @param event
     */
    public void setOnFileChange(EventHandler event) {
        this.event = event;
    }

    /**
     * Add an ExtensionFilter for the File. This will not have effect if openPath is true.
     *
     * @param filter the filter for the File.
     */
    public void addExtensionFilter(FileChooser.ExtensionFilter filter) {
        filters.add(filter);
    }

    /**
     * Add one or more Filters.
     *
     * @param filters
     */
    public void addExtensionFilters(FileChooser.ExtensionFilter... filters) {
        this.filters.addAll(Arrays.asList(filters));
    }

    /**
     * Whether this FileSelector is used to open a File or to open a Directory.
     *
     * @return true if for a Directory.
     */
    public boolean isOpenPath() {
        return openPath;
    }

    /**
     * Change the use of this FileSelector to open a File or a Directory.
     *
     * @param openPath true for Directory, false for File.
     */
    public void setOpenPath(boolean openPath) {
        this.openPath = openPath;
    }

    /**
     * Calls @code{FileManager.openDirectory()}.
     */
    private void openPath() {
        // Try to use the current selected directory as initial directory.
        File parent = null;
        if (!textField.getText().isEmpty()) {
            parent = new File(textField.getText());
        }
        File f = FileManager.openDirectory(label.getText(), parent);
        if (f != null) {
            textField.setText(f.getAbsolutePath());
            if (event != null) {
                event.handle(new ActionEvent());
            }
        }
    }

    private void openFile() {
        // Try to use the parent of the current selected file as initial directory.
        File parent = null;
        if (!textField.getText().isEmpty()) {
            parent = new File(textField.getText()).getParentFile();
        }
        File f = FileManager.openFile(textField, label.getText(), parent,
                filters.toArray(new FileChooser.ExtensionFilter[filters.size()]));
        if (f != null && event != null) {
            event.handle(new ActionEvent());
        }
    }

    /**
     * Change the position of the label text. LEFT or TOP.
     *
     * @param textPosition the new TextPosition: LEFT or TOP.
     */
    public void setTextPosition(Position textPosition) {
        this.textPosition = textPosition;
        this.getChildren().remove(label);
        hBox.getChildren().remove(label);
        switch (textPosition) {
            case LEFT:
                hBox.getChildren().add(0, label);
                break;
            case TOP:
                this.getChildren().add(0, label);
                break;

        }
    }

    /**
     * Gets the current textPosition.
     *
     * @return
     */
    public Position getTextPosition() {
        return textPosition;
    }

    /**
     * Set a prompt text.
     *
     * @param text the new prompt text
     */
    public void setPromptText(String text) {
        textField.setPromptText(text);
    }

    /**
     * Gets the current promptText.
     *
     * @return the current promptText
     */
    public String getPromptText() {
        return textField.getPromptText();
    }

}
