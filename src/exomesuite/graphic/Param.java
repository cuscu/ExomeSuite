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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Parent class for Parameters in ExomeSuite. A param has a title and a value. Value must be
 * modified using a subclass. When value is modified, an event is fired, so subscribing to this
 * class, by <code>setOnValueChanged</code> allows listening when a Parameter is changed.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class Param extends VBox {

    private String value;
    private String title;
    private String promptText;
    private EventHandler handler;
    private final Label titleLabel = new Label();
    private final Label valueLabel = new Label();
    private final Tooltip tooltip = new Tooltip();

    /**
     * Creates a new Param that responds to mouse click event.
     */
    public Param() {
        showPassive();
        setOnMouseClicked(e -> edit());
        getStyleClass().add("parameter");
        valueLabel.setDisable(true);
        Tooltip.install(this, tooltip);
        tooltip.setAutoHide(false);
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title in GUI.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
        titleLabel.setText(title);
    }

    /**
     * Gets the current value.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value. Calls <code>labelValue()</code> to show in GUI. value is set as a tooltip.
     *
     * @param value the new value
     */
    public void setValue(String value) {
        this.value = value;
        valueLabel.setText(labelValue());
        tooltip.setText(value);
    }

    /**
     * Take a look at the current promptText
     *
     * @return
     */
    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    private void showPassive() {
        getChildren().clear();
        getChildren().add(titleLabel);
        getChildren().add(valueLabel);
    }

    private void showActive(Node node) {
        getChildren().clear();
        getChildren().add(titleLabel);
        getChildren().add(new HBox(node));
    }

    private void edit() {
        Node node = getEditingPane();
        if (node != null) {
            showActive(node);
        } else {
            // Edit on the run
            String val = editPassive();
            if (val != null) {
                setValue(val);
                //showPassive();
                // Call valueChanged event
                handler.handle(new ActionEvent());
            }
        }
    }

    /**
     * Call this method if you want to end editting. If accept is true, value will be modified to
     * value, else it will be ignored.
     *
     * @param accept true if value has changed, false if no changes to value
     * @param value the new value
     */
    protected final void endEdit(boolean accept, String value) {
        if (accept) {
            setValue(value);
            if (handler != null) {
                handler.handle(new ActionEvent());
            }
        }
        showPassive();
    }

    /**
     * Override if you want to provide an editing panel, like a ComboBox or a TextField.
     *
     * @return null by default
     */
    protected Node getEditingPane() {
        return null;
    }

    /**
     * Override this if you want to modify the value on the run, without showing a pane. If
     * <code>getEditingPane()</code> is overriden, this method will not have any effect.
     *
     * @return null by default
     */
    protected String editPassive() {
        return null;
    }

    /**
     * Override this if you want to show a different value to user than the real value. For
     * instance, the file name instead of the whole path.
     *
     * @return
     */
    protected String labelValue() {
        if (value == null) {
            return promptText;
        }
        return value;
    }

    /**
     * Register event when value is modified.
     *
     * @param handler
     */
    public void setOnValueChanged(EventHandler handler) {
        this.handler = handler;
    }

}
