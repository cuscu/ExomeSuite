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

import exomesuite.graphic.ResizableCanvas;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;

/**
 * Manages the common information of every layer. A layer which overrides this class will have
 * access to axis margins, tickLength, baseWidth, genomicData ... If a data is changed in any of the
 * layers, it will be changed in the others.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public abstract class BamLayer extends ResizableCanvas {

    protected static final Color A_COLOR = Color.rgb(255, 227, 85);
    protected static final Color T_COLOR = Color.rgb(85, 255, 96);
    protected static final Color C_COLOR = Color.rgb(255, 89, 85);
    protected static final Color G_COLOR = Color.rgb(142, 104, 255);

    private static double axisMargin = 30;
    private static double tickLength = 5;
    private static double baseWidth = 20;
    private static final SimpleIntegerProperty genomicPosition = new SimpleIntegerProperty(100);
    private static int textMargin = 2;
    private static List<Character> reference = new ArrayList<>();
    private static List<Map<Character, Integer>> values = new ArrayList<>();
    private static SimpleIntegerProperty selectedIndex = new SimpleIntegerProperty(-1);

    private static Character[] bases = {'A', 'G', 'C', 'T', 'C', 'A', 'G', 'C', 'T', 'C', 'A', 'G',
        'C', 'T', 'C', 'A', 'G', 'C'};

    public BamLayer() {

        /*
         Mouse selection: all layers will be listening for mouse events, but only the top layer can
         do this. So it must call others by modifing the selectedIndex property. Layers which want
         to listen for index changing just need to add a listener to this property.
         */
        setOnMouseClicked(e -> {
            /*
             We translate X position of the mouse to an index in the nucleotide list.
             -> Proportional position of the X in the graph (0 -> left, 1-> right)
             position = (x - leftMargin) / (width - margins)
             -> Number of graph divisions or number of elements been displayed
             elements = (width - margins) / baseWidth
             -> Index of the selected item
             i = floor(position * elements) = (x - margins) / baseWidth
             */
            int i = (int) Math.floor((e.getX() - axisMargin) / baseWidth);
            System.out.println("Clicked on " + e.getX() + "," + e.getY() + " (" + i + ")");
            selectedIndex.setValue(i);
        });
    }

    /**
     * @return the textMargin
     */
    public static int getTextMargin() {
        return textMargin;
    }

    /**
     * @param aTextMargin the textMargin to set
     */
    public static void setTextMargin(int aTextMargin) {
        textMargin = aTextMargin;
    }

    /**
     * @return the values
     */
    public static List<Map<Character, Integer>> getValues() {
        return values;
    }

    /**
     * @param aValues the values to set
     */
    public static void setValues(List<Map<Character, Integer>> aValues) {
        values = aValues;
    }

    /**
     * Gets the distance between the border and the X-Y axis.
     *
     * @return the distance or margin of the aixs
     */
    public static double getAxisMargin() {
        return axisMargin;
    }

    /**
     * Sets the distance between the X-Y axis and the border.
     *
     * @param axisMargin the new margin of the axis
     */
    public static void setAxisMargin(double axisMargin) {
        BamLayer.axisMargin = axisMargin;
    }

    /**
     * Gets the length of the ticks.
     *
     * @return the length of the ticks.
     */
    public double getTickLength() {
        return tickLength;
    }

    /**
     * Sets the length of the ticks.
     *
     * @param tickLength the length of the ticks.
     */
    public void setTickLength(double tickLength) {
        BamLayer.tickLength = tickLength;
    }

    /**
     * Gets the width of each base.
     *
     * @return the width of each base.
     */
    public static double getBaseWidth() {
        return baseWidth;
    }

    /**
     * Sets the width of each base.
     *
     * @param baseWidth the width of each base.
     */
    public static void setBaseWidth(double baseWidth) {
        BamLayer.baseWidth = baseWidth;
    }

    /**
     * Gets the current initial genomic position.
     *
     * @return the current genomic position of the origin.
     */
    public static SimpleIntegerProperty getGenomicPosition() {
        return genomicPosition;
    }

    /**
     * Sets the initial genomic position.
     *
     * @param genomicPosition the new origin genomic position.
     */
    public static void setGenomicPosition(int genomicPosition) {
        BamLayer.genomicPosition.set(genomicPosition);
    }

    /**
     * Sets the list of reference bases.
     *
     * @param reference the new list of reference bases
     */
    public static void setReference(List<Character> reference) {
        BamLayer.reference = reference;
    }

    /**
     * Gets the list of refrence bases.
     *
     * @return the list of reference bases
     */
    public static List<Character> getReference() {
        return reference;
    }

    public static SimpleIntegerProperty getSelectedIndex() {
        return selectedIndex;
    }

    public static void setSelectedIndex(SimpleIntegerProperty selectedIndex) {
        BamLayer.selectedIndex = selectedIndex;
    }

}
