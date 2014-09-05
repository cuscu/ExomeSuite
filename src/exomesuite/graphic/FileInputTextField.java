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

import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class FileInputTextField extends HBox {

    @FXML
    private TextField textField;
    @FXML
    private Button button;
    private String title;
    private FileChooser.ExtensionFilter[] filters;
    private boolean openDir = false;
    private File file;

    private EventHandler t;

    public FileInputTextField() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FileInputTextField.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        textField.setEditable(false);
//        textField.setOnMouseClicked((MouseEvent event) -> openFile());
        button.setGraphic(new ImageView("exomesuite/img/browse.png"));
        button.setOnAction((ActionEvent event) -> openFile());
//        button.setBackground(Background.EMPTY);
    }

    public void setFilters(FileChooser.ExtensionFilter... filters) {
        this.filters = filters;
    }

    public void setTitle(String title) {
        this.title = title;
        button.setTooltip(new Tooltip(title));
    }

    public void setPromptText(String text) {
        textField.setPromptText(text);
    }

    /**
     * If dir is true, a directory will be selected, otherwise a file will be selected.
     *
     * @param dir
     */
    public void setDirSelection(boolean dir) {
        openDir = dir;
    }

    private void openFile() {
        if (openDir) {
            file = OS.selectFolder(title);
            if (file != null) {
                textField.setText(file.getAbsolutePath());
            }
        } else {
            file = OS.openFile(textField, title, filters);
        }
        if (t != null) {
            t.handle(new ActionEvent());
        }
    }

    public File getFile() {
        return file;
    }

    public void setOnSelect(EventHandler t) {
        this.t = t;
    }
}
