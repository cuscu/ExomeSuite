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

import exomesuite.vcf.VCFFilter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class VCFFilterPane extends VBox {

    private final Label staticInfo = new Label();
    private final ComboBox<VCFFilter.Field> field = new ComboBox<>();
    private final ComboBox<VCFFilter.Connector> connector = new ComboBox<>();
    private final TextField value = new TextField();
    private final FlatButton accept = new FlatButton("apply.png", "Accept");
    private final FlatButton cancel = new FlatButton("cancel4.png", "Cancel");
    private final FlatButton delete = new FlatButton("delete.png", "Delete");
    private EventHandler onAccept, onDelete;

    {
        field.getItems().setAll(VCFFilter.Field.values());
        connector.getItems().setAll(VCFFilter.Connector.values());
        accept.setOnAction(e -> accept());
        cancel.setOnAction(e -> toPassive());
        setOnMouseClicked(e -> startEdit());
        value.setOnAction(e -> accept());
        delete.setOnAction(e -> delete());
        value.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                accept();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                toPassive();
            }
        });
        staticInfo.setText("Click to set the filter");
        getStyleClass().add("parameter");
        setAlignment(Pos.CENTER);
        toPassive();
    }

    private VCFFilter filter;

    public VCFFilterPane() {
        filter = new VCFFilter();
    }

    public VCFFilterPane(VCFFilter filter) {
        this.filter = filter;
        value.setText(filter.getValue());
        field.getSelectionModel().select(filter.getField());
        connector.getSelectionModel().select(filter.getConnector());
        setStaticInfo();
    }

    public VCFFilter getFilter() {
        return filter;
    }

    public void setFilter(VCFFilter filter) {
        this.filter = filter;
    }

    private void accept() {
        filter.setField(field.getSelectionModel().getSelectedItem());
        filter.setConnector(connector.getSelectionModel().getSelectedItem());
        filter.setValue(value.getText());
        getChildren().clear();
        setStaticInfo();
        toPassive();
        if (onAccept != null) {
            onAccept.handle(new ActionEvent());
        }
    }

    private void toPassive() {
        Separator separator = new Separator(Orientation.HORIZONTAL);
        separator.setVisible(false);
        HBox.setHgrow(separator, Priority.ALWAYS);
        HBox box = new HBox(staticInfo, separator, delete);
        box.setAlignment(Pos.CENTER);
        getChildren().setAll(box);
    }

    private void startEdit() {
        HBox up = new HBox(field, connector);
        HBox down = new HBox(accept, cancel);
        getChildren().setAll(up, value, down);
    }

    private void setStaticInfo() {
        staticInfo.setText(filter.getField()
                + " " + filter.getConnector()
                + " " + filter.getValue());
    }

    public void setOnAccept(EventHandler onAccept) {
        this.onAccept = onAccept;
    }

    public void setOnDelete(EventHandler onDelete) {
        this.onDelete = onDelete;
    }

    private void delete() {
        if (onDelete != null) {
            onDelete.handle(new ActionEvent());
        }
    }

}
