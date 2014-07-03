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
package exomesuite.tsvreader;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class ReaderViewController {

    @FXML
    private Label file;
    @FXML
    private Label size;
    @FXML
    private Label lines;
    @FXML
    private TableView<String[]> table;
    @FXML
    private Label currentLines;
    @FXML
    private Button export;

    public Label getFile() {
        return file;
    }

    public Label getLines() {
        return lines;
    }

    public Label getSize() {
        return size;
    }

    public TableView<String[]> getTable() {
        return table;
    }

    public Label getCurrentLines() {
        return currentLines;
    }

    public Button getExport() {
        return export;
    }

}
