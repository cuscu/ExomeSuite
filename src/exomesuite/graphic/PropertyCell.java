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

import exomesuite.project.Project;
import exomesuite.project.Project.PropertyName;
import exomesuite.utils.FileManager;
import java.io.File;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public final class PropertyCell extends TableCell<Map.Entry<PropertyName, String>, PropertyName> {

    private final Project project;
    private final TextField editField = new TextField();
    private final ComboBox<String> comboBox = new ComboBox();

    private final FlatButton edit = new FlatButton("edit.png", "Edit");
    private final FlatButton browse = new FlatButton("browse.png", "Select file/folder");
    private final FlatButton cancel = new FlatButton("cancel4.png", "Cancel edit");
    private final FlatButton accept = new FlatButton("apply.png", "Apply changes");
    private final FlatButton clear = new FlatButton("rubber.png", "Clear");

    private PropertyName currentProperty;
    private File fileSelected;

    public PropertyCell(Project project) {
        this.project = project;
        clear.setOnAction((ActionEvent event) -> clear());
        setContentDisplay(ContentDisplay.RIGHT);
        setAlignment(Pos.CENTER_RIGHT);
    }

    @Override
    protected void updateItem(PropertyName name, boolean empty) {
        super.updateItem(name, empty);
        if (!empty) {
            currentProperty = name;
            updateCell();
        }
    }

    @Override
    public void commitEdit(PropertyName prop) {
        switch (prop) {
            case NAME:
            case CODE:
            case DESCRIPTION:
                if (!editField.getText().isEmpty()) {
                    project.setProperty(prop, editField.getText());
                }
                break;
            case PATH:
            case FORWARD_FASTQ:
            case REVERSE_FASTQ:
                if (fileSelected != null) {
                    project.setProperty(prop, fileSelected.getAbsolutePath());
                }
                break;
            case FASTQ_ENCODING:
            case REFERENCE_GENOME:
                final String value = comboBox.getSelectionModel().getSelectedItem();
                if (value != null) {
                    project.setProperty(prop, value);
                }
        }
        cancelEdit();
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (cancel.getOnAction() == null) {
            cancel.setOnAction((ActionEvent event) -> cancelEdit());
        }
        if (accept.getOnAction() == null) {
            accept.setOnAction((ActionEvent event) -> commitEdit(currentProperty));
        }
        if (editField.getOnAction() == null) {
            editField.setOnAction((ActionEvent event) -> commitEdit(currentProperty));
        }
        switch (currentProperty) {
            case NAME:
            case CODE:
            case DESCRIPTION:
                editField.setText(project.getProperty(currentProperty));
                setGraphic(new HBox(editField, cancel, accept));
                setText(null);
                break;
            case PATH:
                fileSelected = FileManager.selectFolder("Select Path");
                commitEdit(currentProperty);
                break;
            case FORWARD_FASTQ:
            case REVERSE_FASTQ:
                fileSelected = FileManager.openFile("Select FASTQ file", FileManager.FASTQ_FILTER);
                commitEdit(currentProperty);
                break;
            case FASTQ_ENCODING:
                comboBox.getItems().setAll(Project.encondingValues());
                setGraphic(new HBox(comboBox, cancel, accept));
                setText(null);
                break;
            case REFERENCE_GENOME:
                comboBox.getItems().setAll(Project.referenceGenomes());
                setGraphic(new HBox(comboBox, cancel, accept));
                setText(null);
            default:
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        updateCell();
    }

    private void updateCell() {
        if (edit.getOnAction() == null) {
            edit.setOnAction((ActionEvent event) -> startEdit());
        }
        if (browse.getOnAction() == null) {
            browse.setOnAction((ActionEvent event) -> startEdit());
        }
        setText(project.getProperty(currentProperty));
        switch (currentProperty) {
            case NAME:
            case CODE:
            case DESCRIPTION:
            case FASTQ_ENCODING:
            case REFERENCE_GENOME:
                setGraphic(edit);
                break;
            case PATH:
            case FORWARD_FASTQ:
            case REVERSE_FASTQ:
                setGraphic(browse);
        }
    }

    private void clear() {
        project.setProperty(currentProperty, "");
    }

}
