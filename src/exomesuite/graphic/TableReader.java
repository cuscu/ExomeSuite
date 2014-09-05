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
package exomesuite.graphic;

import java.util.BitSet;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class TableReader {

    private VBox view;
    private TableView<String[]> table;

    private final String[] headers;
    private final String[][] data;
    private final BitSet bitSet;
    private final Label currentLines;

    public TableReader(String[] headers, String[][] data) {
        this.headers = headers;
        this.data = data;
        bitSet = new BitSet(data.length);
        table = new TableView<>();
        view = initView();
        currentLines = new Label(data.length + "");

    }

    public Node getView() {
        return view;
    }

    private VBox initView() {
        return new VBox(5, getGrid(), getTable());
    }

    private GridPane getGrid() {
        GridPane grid = new GridPane();
        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.SOMETIMES);
        grid.getColumnConstraints().addAll(new ColumnConstraints(), cc);
        grid.add(new Label("Lines: "), 0, 0);
        grid.add(new Label("Current lines: "), 0, 1);
        grid.add(new Label(data.length + ""), 1, 0);
        grid.add(currentLines, 1, 1);
        return grid;
    }

    private TableView getTable() {
//        table.getItems().addAll(data);
        return table;
    }
}
