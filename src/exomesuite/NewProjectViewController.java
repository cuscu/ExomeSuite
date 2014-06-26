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

import exomesuite.utils.OS;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class NewProjectViewController {

    @FXML
    private TextField name;
    @FXML
    private TextField path;
    @FXML
    private Button acceptButton;
    @FXML
    private Label finalPath;

    private File pathDir;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        path.setEditable(false);
        path.setOnAction((ActionEvent event) -> {
            selectPath();
        });
        path.setOnMouseClicked((MouseEvent event) -> {
            selectPath();
        });
        name.setOnKeyTyped((KeyEvent event) -> {
            updateFolder();
        });
    }

    private void selectPath() {
        File f = OS.selectFolder("Select project path");
        if (f != null) {
            path.setText(f.getAbsolutePath());
            pathDir = f;
            if (!name.getText().isEmpty()) {
                updateFolder();
            }
        }
    }

    public File getPath() {
        return pathDir;
    }

    public String getName() {
        return name.getText();
    }

    private void updateFolder() {
        if (pathDir != null) {
            finalPath.setText(pathDir + File.separator + name.getText() + " will be created.");
        }
    }

    public Button getAcceptButton() {
        return acceptButton;
    }

    public void clear() {
        name.setText("");
        path.setText("");
        finalPath.setText("");
        pathDir = null;

    }

}