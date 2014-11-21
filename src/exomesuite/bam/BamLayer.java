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

    protected final GraphParameters parameters;

    public BamLayer(GraphParameters parameters) {
        this.parameters = parameters;
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
            final double margin = parameters.getAxisMargin().doubleValue();
            final double width = parameters.getBaseWidth().doubleValue();
            final int index = (int) Math.floor((e.getX() - margin) / width);
            System.out.println("Clicked on " + e.getX() + "," + e.getY() + " (" + index + ")");
            parameters.setSelectedIndex(index);
        });
    }
}
