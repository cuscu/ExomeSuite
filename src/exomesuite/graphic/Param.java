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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 * Parent class for Parameters in ExomeSuite. A param has a title and a value. Value must be
 * modified using a subclass. When value is modified, an event is fired, so subscribing to this
 * class, by <code>setOnValueChanged</code> allows listening when a Parameter is changed.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 * @param <T> The class of the value
 */
public class Param<T> extends HBox {

    private T value;
    private String promptText;
    private EventHandler handler;
    private final Label titleLabel = new Label();
    private final Label valueLabel = new Label();
    private final Tooltip tooltip = new Tooltip();

    /**
     * Creates a new Param that responds to mouse click event.
     */
    public Param() {
        initialize();
    }

    public Param(String title, T value) {
        this.titleLabel.setText(title);
        this.value = value;
        initialize();
    }

    private void initialize() {
        // Whe user clicks, start editing
        setOnMouseClicked(e -> edit());
        // The nice blue box
        getStyleClass().add("parameter");
        // The black/grey effect of title/value
        valueLabel.setDisable(true);
        // Force the tooltip to the whole region
        Tooltip.install(this, tooltip);
        tooltip.setAutoHide(false);
        if (value != null) {
            tooltip.setText(value.toString());
        }

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(5);

        // Title will hide last when no space
        titleLabel.setMinWidth(USE_PREF_SIZE);
        titleLabel.setLabelFor(valueLabel);

        valueLabel.setText(toLabel(value));

        // Start with the passive view
        showPassive();
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return titleLabel.getText();
    }

    /**
     * Sets the title in GUI.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Gets the current value.
     *
     * @return value
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value. Calls <code>toLabel()</code> to show in GUI. value is set as a tooltip.
     *
     * @param value the new value
     */
    public void setValue(T value) {
        this.value = value;
        if (value != null) {
            valueLabel.setText(toLabel(value));
            tooltip.setText(String.valueOf(value));
        }
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
            T val = editPassive();
            if (val != null) {
                setValue(val);
                //showPassive();
                // Call valueChanged event
                if (handler != null) {
                    handler.handle(new ActionEvent());
                }
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
    protected final void endEdit(boolean accept, T value) {
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
     * @return the new value or null to ignore
     */
    protected T editPassive() {
        return null;
    }

    /**
     * Override this if you want to show a different value to user than the real value. For
     * instance, the file name instead of the whole path.
     *
     * @param value the value
     * @return a String for the label of the param
     */
    protected String toLabel(T value) {
        if (value == null) {
            return promptText;
        }
        return String.valueOf(value);
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
