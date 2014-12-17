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

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Param to select a number inside a range using a slider. It has a textField associated.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class NumberParam extends Param<Double> {

    private final Slider slider = new Slider();
    private final TextField textField = new TextField();
    private final Button accept = new Button(null, new SizableImage("exomesuite/img/accept.png", 16));
    private final Button cancel = new Button(null, new SizableImage("exomesuite/img/cancel.png", 16));
    private final Button up = new Button(null, new SizableImage("exomesuite/img/up.png", 16));
    private final Button down = new Button(null, new SizableImage("exomesuite/img/down.png", 16));

    private final HBox pane;
    private boolean onlyInteger = true;
    private boolean fromText = false;

    /**
     * Creates a parameter that let the user to select a number using a slider or a textField.
     */
    public NumberParam() {
        accept.setOnAction(e -> endEdit(true, accept()));
        cancel.setOnAction(e -> endEdit(false, null));
        slider.setBlockIncrement(1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        up.setOnAction(e -> slider.increment());
        down.setOnAction(e -> slider.decrement());
        VBox buttons = new VBox(up, down);
        VBox values = new VBox(textField, slider);
        pane = new HBox(buttons, values, accept, cancel);
        slider.valueProperty().addListener((ObservableValue<? extends Number> observable,
                Number oldValue, Number newValue) -> {
                    if (onlyInteger) {
                        slider.valueProperty().set(Math.round(newValue.doubleValue()));
                    }
                    if (!fromText) {
                        textField.setText(String.valueOf(slider.getValue()));
                    } else {
                        fromText = false;
                    }
                });
        textField.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                // Enter key terminates editing with true
                accept();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                // Escape key terminates editing with false
                textField.setText(slider.getValue() + "");
            } else {
                // Rest of keys, try to determine new slider value on the run
                try {
                    fromText = true;
                    slider.setValue(Double.valueOf(textField.getText()));
                } catch (NumberFormatException ex) {
                }
            }
        });
    }

    @Override
    protected Node getEditingPane() {
        return pane;
    }

    private Double accept() {
        return slider.getValue();
    }

    /**
     * Set the max value.
     *
     * @param maxValue the max value
     */
    public void setMax(double maxValue) {
        slider.setMax(maxValue);
    }

    /**
     * Get the max value.
     *
     * @return the max value
     */
    public double getMax() {
        return slider.getMax();
    }

    /**
     * Sets the min value.
     *
     * @param minValue the min value
     */
    public void setMin(double minValue) {
        slider.setMin(minValue);
    }

    /**
     * Gets the min value.
     *
     * @return themin value
     */
    public double getMin() {
        return slider.getMin();
    }

    /**
     * The onlyInteger property allows you to force user to have only Integer values.
     *
     * @param onlyInteger true to accept only integer values
     */
    public void setOnlyInteger(boolean onlyInteger) {
        this.onlyInteger = onlyInteger;
    }

    /**
     * The onlyInteger property allows you to force user to have only Integer values.
     *
     * @return true if number is forced to be integer
     */
    public boolean isOnlyInteger() {
        return onlyInteger;
    }

}
