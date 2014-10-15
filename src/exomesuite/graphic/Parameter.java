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

import exomesuite.utils.FileManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Parameter extends VBox {

    @FXML
    private Node content;
    @FXML
    private Label name;

    private Type type;

    private TextField textField;

    private FlatButton accept, cancel;

    private String oldText;

    private boolean onEditMode;

    private Label label;

    private String value;

    private List<String> options;

    /**
     * The fired event when a File is selected.
     */
    private EventHandler changeEvent;

    /**
     * Filters of the File. Fetch them from FileManager or create new ones.
     */
    private final List<FileChooser.ExtensionFilter> filters = new ArrayList<>();

    public Parameter() {
        try {
            FXMLLoader loader = new FXMLLoader(Parameter.class.getResource("Parameter.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (Exception e) {
            Dialogs.create().title("Error loading Parameter").showException(e);
        }
    }

    @FXML
    private void initialize() {
        setOnMouseClicked(e -> startEdit());
        textField = new TextField();
        HBox.setHgrow(textField, Priority.ALWAYS);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.setPromptText("Enter value");
//        textField.setDisable(true);
        accept = new FlatButton("apply.png", "Accept... Enter");
        cancel = new FlatButton("cancel4.png", "Cancel... Esc");
        textField.focusedProperty().addListener((
                ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                -> {
            if (newValue) {
                startEdit();
            } else {
                textField.requestFocus();
                //endEdit();
            }
        });
        textField.setOnKeyReleased((KeyEvent event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                endEdit(true, textField.getText());
            } else if (event.getCode() == KeyCode.ESCAPE) {
                endEdit(false, null);
            }
        });
        accept.setOnAction(event -> endEdit(true, textField.getText()));
        cancel.setOnAction(event -> endEdit(false, null));
        label = new Label("Enter value");
        label.setDisable(true);
        options = new ArrayList<>();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        setContent(label);
        switch (type) {
            case TEXT:
                label.setText("Enter value");
                break;
            case FILE:
                label.setText("Select file");
                break;
            case DIRECTORY:
                label.setText("Select folder");
                break;

        }
    }

    private void startEdit() {
        if (onEditMode) {
            return;
        }
        oldText = value;
        onEditMode = true;
        switch (type) {
            case TEXT:
                textField.setText(value);
                setContent(new HBox(textField, accept, cancel));
                //textField.setDisable(false);
                textField.requestFocus();
                break;
            case FILE:
                File prev;
                FileChooser.ExtensionFilter[] arrayFilters
                        = new FileChooser.ExtensionFilter[filters.size()];
                for (int i = 0; i < filters.size(); i++) {
                    arrayFilters[i] = filters.get(i);
                }
                File f;
                if (value != null && (prev = new File(value)).exists()) {
                    f = FileManager.openFile("Select file", prev.getParentFile(), arrayFilters);
                } else {
                    f = FileManager.openFile("Select file", arrayFilters);
                }
                if (f != null) {
                    value = f.getAbsolutePath();
                    endEdit(true, f.getAbsolutePath());
                } else {
                    endEdit(false, null);
                }
                break;
            case DIRECTORY:
                File pre;
                if (value != null && (pre = new File(value)).exists()) {
                    f = FileManager.selectFolder("Select folder", pre);
                } else {
                    f = FileManager.selectFolder("Select folder");
                }
                if (f != null) {
                    endEdit(true, f.getAbsolutePath());
                } else {
                    endEdit(false, null);
                }
                break;
            case OPTIONS:
                ComboBox<String> opt = new ComboBox<>(FXCollections.observableArrayList(options));
                opt.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(opt, Priority.ALWAYS);
                HBox box = new HBox(opt, cancel);
                box.setMaxWidth(Double.MAX_VALUE);
                box.setAlignment(Pos.CENTER);
                setContent(box);
                opt.getSelectionModel().select(value);
                opt.getSelectionModel().selectedItemProperty().addListener((
                        ObservableValue<? extends String> obs, String previous, String current) -> {
                    endEdit(true, current);
                });
                break;
        }
    }

    private void endEdit(boolean change, String value) {
        if (!onEditMode) {
            return;
        }
        onEditMode = false;
        if (change) {
            setValue(value);
            if (changeEvent != null) {
                changeEvent.handle(new ActionEvent());
            }
        }
        setContent(label);

    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        label.setText(value);
    }

    public List<FileChooser.ExtensionFilter> getFilters() {
        return filters;
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public String getName() {
        return name.getText();
    }

    public void setContent(Node content) {
        getChildren().remove(this.content);
        this.content = content;
        getChildren().add(content);
    }

    public Node getContent() {
        return content;
    }

    /**
     * Specify and EventHandler called when a new File or Directory is selected.
     *
     * @param event
     */
    public void setOnValueChanged(EventHandler event) {
        this.changeEvent = event;
    }

    public void addExtensionFilter(FileChooser.ExtensionFilter filter) {
        filters.add(filter);
    }

    public void addOption(String option) {
        options.add(option);
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public enum Type {

        FILE, DIRECTORY, TEXT, OPTIONS
    }
}
