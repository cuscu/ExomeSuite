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
import java.util.ArrayList;
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
import javafx.stage.FileChooser;

/**
 *
 * @author uichuimi03
 */
public class FileSelector extends HBox {

    @FXML
    private Label label;
    @FXML
    private TextField textField;
    @FXML
    private FlatButton button;

    private boolean openPath;

    private EventHandler event;

    private final List<FileChooser.ExtensionFilter> filters = new ArrayList<>();

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

    @FXML
    public void initialize() {
        button.setOnAction((ActionEvent e) -> open());
    }

    public void setText(String text) {
        label.setText(text);
    }

    public String getText() {
        return label.getText();
    }

    public void setFile(String file) {
        textField.setText(file);
    }

    public String getFile() {
        return textField.getText();
    }

    private void open() {
        if (openPath) {
            openPath();
        } else {
            openFile();
        }
    }

    public void setOnFileChange(EventHandler event) {
        this.event = event;
    }

    public void addExtensionFilter(FileChooser.ExtensionFilter filter) {
        filters.add(filter);
    }

    public void addExtensionFilters(FileChooser.ExtensionFilter... filters) {
        for (FileChooser.ExtensionFilter filter : filters) {
            this.filters.add(filter);
        }
    }

    public boolean isOpenPath() {
        return openPath;
    }

    public void setOpenPath(boolean openPath) {
        this.openPath = openPath;
    }

    private void openPath() {
        File f = OS.selectFolder(label.getText());
        if (f != null) {
            textField.setText(f.getAbsolutePath());
            if (event != null) {
                event.handle(new ActionEvent());
            }
        }
    }

    private void openFile() {
        File f = OS.openFile(textField, label.getText(),
                filters.toArray(new FileChooser.ExtensionFilter[filters.size()]));
        if (f != null && event != null) {
            event.handle(new ActionEvent());
        }
    }
}
