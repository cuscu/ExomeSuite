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
package exomesuite.bam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gamil.com)
 */
public class GraphParameters {

    /**
     * Margin from graphic border to any axis.
     */
    private final SimpleDoubleProperty axisMargin = new SimpleDoubleProperty(30.0);
    /**
     * Length of ticks.
     */
    private final SimpleDoubleProperty tickLength = new SimpleDoubleProperty(5.0);
    /**
     * Width of a column.
     */
    private final SimpleDoubleProperty baseWidth = new SimpleDoubleProperty(20.0);
    /**
     * Start position of the graph.
     */
    private final SimpleIntegerProperty genomicPosition = new SimpleIntegerProperty(100);
    /**
     * Left and right margin of text.
     */
    private final SimpleDoubleProperty textMargin = new SimpleDoubleProperty(2.0);
    /**
     * Selected index. -1 i nothing selected.
     */
    private final SimpleIntegerProperty selectedIndex = new SimpleIntegerProperty(-1);
    /**
     * Number of divisions of y axis.
     */
    private final SimpleIntegerProperty yTicks = new SimpleIntegerProperty(5);
    /**
     * Y axis as percentage.
     */
    private final SimpleBooleanProperty percentageUnits = new SimpleBooleanProperty(true);
    /**
     * Activate colors per base.
     */
    private final SimpleBooleanProperty baseColors = new SimpleBooleanProperty(true);
    /**
     * Y max value.
     */
    private final SimpleDoubleProperty maxYValue = new SimpleDoubleProperty();
    /**
     * List of reference bases. First character must be nucleotide on genomicPosition.
     */
    private List<Character> reference = new ArrayList<>();
    /**
     * List of depths for each base in each position.
     */
    private List<Map<Character, Integer>> values = new ArrayList<>();

    /**
     * Margin from graphic border to any axis.
     *
     * @return the axisMargin
     */
    public SimpleDoubleProperty getAxisMargin() {
        return axisMargin;
    }

    /**
     * Length of ticks.
     *
     * @return the tickLength
     */
    public SimpleDoubleProperty getTickLength() {
        return tickLength;
    }

    /**
     * Width of a column.
     *
     * @return the baseWidth
     */
    public SimpleDoubleProperty getBaseWidth() {
        return baseWidth;
    }

    /**
     * Start position of the graph.
     *
     * @return the genomicPosition
     */
    public SimpleIntegerProperty getGenomicPosition() {
        return genomicPosition;
    }

    /**
     * Left and right margin of text.
     *
     * @return the textMargin
     */
    public SimpleDoubleProperty getTextMargin() {
        return textMargin;
    }

    /**
     * Selected index. -1 i nothing selected.
     *
     * @return the selectedIndex
     */
    public SimpleIntegerProperty getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Number of divisions of y axis.
     *
     * @return the yTicks
     */
    public SimpleIntegerProperty getyTicks() {
        return yTicks;
    }

    /**
     * Show Y axis as percentage.
     *
     * @return the percentageUnits
     */
    public SimpleBooleanProperty getPercentageUnits() {
        return percentageUnits;
    }

    /**
     * Activate base colors.
     *
     * @return true if base colors activated.
     */
    public SimpleBooleanProperty getBaseColors() {
        return baseColors;
    }

    /**
     * Max Y value.
     *
     * @return the maxYvalue
     */
    public SimpleDoubleProperty getMaxYValue() {
        return maxYValue;
    }

    /**
     * List of reference bases. First character must be nucleotide on genomicPosition.
     *
     * @return the reference
     */
    public List<Character> getReference() {
        return reference;
    }

    /**
     * List of depths for each base in each position.
     *
     * @return the values
     */
    public List<Map<Character, Integer>> getValues() {
        return values;
    }

    /**
     * Margin from graphic border to any axis.
     *
     * @param margin the axisMargin to set
     */
    public void setAxisMargin(double margin) {
        axisMargin.set(margin);
    }

    /**
     * Length of ticks.
     *
     * @param length the tickLength to set
     */
    public void setTickLength(double length) {
        tickLength.set(length);
    }

    /**
     * Width of a column.
     *
     * @param width the baseWidth to set
     */
    public void setBaseWidth(double width) {
        baseWidth.set(width);
    }

    /**
     * Start position of the graph.
     *
     * @param position the genomicPosition to set
     */
    public void setGenomicPosition(int position) {
        genomicPosition.set(position);
    }

    /**
     * Left and right margin of text.
     *
     * @param margin the textMargin to set
     */
    public void setTextMargin(double margin) {
        textMargin.set(margin);
    }

    /**
     * Selected index. -1 i nothing selected.
     *
     * @param index the selectedIndex to set
     */
    public void setSelectedIndex(int index) {
        selectedIndex.set(index);
    }

    /**
     * Number of divisions of y axis.
     *
     * @param ticks the ticks to set
     */
    public void setYTicks(int ticks) {
        yTicks.set(ticks);
    }

    /**
     * Show Y axis as percentage.
     *
     * @param percentage true if percentage units, false if absolute.
     */
    public void setPercentageUnits(boolean percentage) {
        percentageUnits.set(percentage);
    }

    /**
     * Max Y value.
     *
     * @param value the new value
     */
    public void setMaxYValue(double value) {
        maxYValue.set(value);
    }

    /**
     * List of reference bases. First character must be nucleotide on genomicPosition.
     *
     * @param reference the reference to set
     */
    public void setReference(List<Character> reference) {
        this.reference = reference;
    }

    /**
     * List of depths for each base in each position.
     *
     * @param values the values to set
     */
    public void setValues(List<Map<Character, Integer>> values) {
        this.values = values;
        maxYValue.set(1);
        values.forEach((Map<Character, Integer> position) -> {
            if (position != null) {
                position.forEach((Character base, Integer dp) -> {
                    if (dp > maxYValue.get()) {
                        maxYValue.set(dp);
                    }
                });
            }
        });
    }

    public void setBaseColors(boolean activate) {
        baseColors.set(activate);
    }

}
