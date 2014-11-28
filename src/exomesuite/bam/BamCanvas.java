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
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gamil.com)
 */
public class BamCanvas extends StackPane {

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
     * If bars must be shown by alleles.
     */
    private final SimpleBooleanProperty showAlleles = new SimpleBooleanProperty(false);
    /**
     * List of alignments to show.
     */
    private List<PileUp> alignments = new ArrayList<>();

    /**
     * Per nucleotide background layer
     */
    private final BamBaseBackgroundLayer backgroundLayer = new BamBaseBackgroundLayer();
    private final BamAxisLayer axisLayer = new BamAxisLayer();
    private final BamBarsLayer barsLayer = new BamBarsLayer();
    private final BamSelectLayer selectLayer = new BamSelectLayer();
    private final BamAxisXLayer axisXLayer = new BamAxisXLayer();
    private final BamAxisYLayer axisYLayer = new BamAxisYLayer();
    private final BamBaseLabelLayer baseLabelLayer = new BamBaseLabelLayer();
    private final BamAxisXlabelsLayer axisXlabelsLayer = new BamAxisXlabelsLayer();
    private final BamAxisYLabelsLayer axisYLabelsLayer = new BamAxisYLabelsLayer();

    public BamCanvas() {
        widthProperty().addListener(object -> repaint());
        heightProperty().addListener(object -> repaint());
        addLayer(backgroundLayer);
        addLayer(selectLayer);
        addLayer(axisXLayer);
        addLayer(axisYLayer);
        addLayer(axisXlabelsLayer);
        addLayer(axisYLabelsLayer);
        addLayer(barsLayer);
        addLayer(axisLayer);
        addLayer(baseLabelLayer);
        // White background
        setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        /*
         * Mouse selection: all layers will be listening for mouse events, but only the top layer can
         * do this. So it must call others by modifing the selectedIndex property. Layers which want
         * to listen for index changing just need to add a listener to this property.
         */
        setOnMouseClicked((MouseEvent e) -> {
            /*
             * We translate X position of the mouse to an index in the nucleotide list.
             * (1) Proportional position of the X in the graph (0 -> left, 1-> right)
             *     position = (x - leftMargin) / (width - margins)
             * (2) Number of graph divisions or number of elements been displayed
             *     elements = (width - margins) / baseWidth
             * (3) Index of the selected item
             *     i = floor(position * elements) = (x - margins) / baseWidth
             */
            final double margin = axisMargin.doubleValue();
            final double width = baseWidth.doubleValue();
            final int index = (int) Math.floor((e.getX() - margin) / width);
//            System.out.println("Clicked on " + e.getX() + "," + e.getY() + " (" + index + ")");
            setSelectedIndex(index);

        });
    }

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

    public List<PileUp> getAlignments() {
        return alignments;
    }

    public SimpleBooleanProperty getShowAlleles() {
        return showAlleles;
    }

    /**
     * Margin from graphic border to any axis.
     *
     * @param margin the axisMargin to set
     */
    public void setAxisMargin(double margin) {
        axisMargin.set(margin);
        repaint();
    }

    /**
     * Length of ticks.
     *
     * @param length the tickLength to set
     */
    public void setTickLength(double length) {
        tickLength.set(length);
        repaint();
    }

    /**
     * Width of a column.
     *
     * @param width the baseWidth to set
     */
    public void setBaseWidth(double width) {
        baseWidth.set(width);
        repaint();
    }

    /**
     * Start position of the graph.
     *
     * @param position the genomicPosition to set
     */
    public void setGenomicPosition(int position) {
        genomicPosition.set(position);
        repaint();
    }

    /**
     * Left and right margin of text.
     *
     * @param margin the textMargin to set
     */
    public void setTextMargin(double margin) {
        textMargin.set(margin);
        repaint();
    }

    /**
     * Selected index. -1 i nothing selected.
     *
     * @param index the selectedIndex to set
     */
    public final void setSelectedIndex(int index) {
        selectedIndex.set(index);
        repaint();
    }

    /**
     * Number of divisions of y axis.
     *
     * @param ticks the ticks to set
     */
    public void setYTicks(int ticks) {
        yTicks.set(ticks);
        repaint();
    }

    /**
     * Show Y axis as percentage.
     *
     * @param percentage true if percentage units, false if absolute.
     */
    public void setPercentageUnits(boolean percentage) {
        percentageUnits.set(percentage);
        repaint();
    }

    /**
     * Max Y value.
     *
     * @param value the new value
     */
    public void setMaxYValue(double value) {
        maxYValue.set(value);
        repaint();
    }

    /**
     * List of pileups.
     *
     * @param alignments the alignments
     */
    public void setAlignments(List<PileUp> alignments) {
        this.alignments = alignments;
        computeMaxY();
        repaint();
    }

    public void setBaseColors(boolean activate) {
        baseColors.set(activate);
        repaint();
    }

    public void setShowBackground(boolean show) {
        backgroundLayer.setVisible(show);
    }

    public void setShowAxisX(boolean show) {
        axisXLayer.setVisible(show);
    }

    public void setShowAxisY(boolean show) {
        axisYLayer.setVisible(show);
    }

    public void setShowAxis(boolean show) {
        axisLayer.setVisible(show);
    }

    public void setShowBars(boolean show) {
        barsLayer.setVisible(show);
    }

    public void setShowSelect(boolean show) {
        selectLayer.setVisible(show);
    }

    void setShowLabelsX(boolean selected) {
        axisXlabelsLayer.setVisible(selected);
    }

    void setShowLabelsY(boolean selected) {
        axisYLabelsLayer.setVisible(selected);
    }

    void setShowAlleles(boolean selected) {
        showAlleles.set(selected);
        computeMaxY();
        repaint();
    }

    private void repaint() {
        getChildren().stream().forEachOrdered(layer -> ((BamLayer) layer).repaint(this));
    }

    private void addLayer(BamLayer layer) {
        getChildren().add(layer);
        layer.widthProperty().bind(widthProperty());
        layer.heightProperty().bind(heightProperty());
    }

    private void computeMaxY() {
        maxYValue.set(1);
        alignments.forEach(pileup -> {
            if (showAlleles.get()) {
                pileup.getDepths().forEach((Character t, Integer u) -> {
                    if (showAlleles.get()) {
                        if (u > maxYValue.get()) {
                            maxYValue.set(u);
                        }
                    }
                });
            } else {
                final double a = pileup.getDepth('A') + pileup.getDepth('a');
                final double c = pileup.getDepth('C') + pileup.getDepth('c');
                final double g = pileup.getDepth('G') + pileup.getDepth('g');
                final double t = pileup.getDepth('T') + pileup.getDepth('t');
                if (a > maxYValue.get()) {
                    maxYValue.set(a);
                }
                if (c > maxYValue.get()) {
                    maxYValue.set(c);
                }
                if (g > maxYValue.get()) {
                    maxYValue.set(g);
                }
                if (t > maxYValue.get()) {
                    maxYValue.set(t);
                }
            }
        });
    }

}
