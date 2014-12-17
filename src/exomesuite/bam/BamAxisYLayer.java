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

import javafx.geometry.VPos;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 * Displays the Y lines (horizontal)
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamAxisYLayer extends BamLayer {

    /**
     * Creates a layer that displays the Y lines (horizontal dividers).
     */
    public BamAxisYLayer() {
        getGraphicsContext2D().setStroke(Color.GRAY);
        getGraphicsContext2D().setTextAlign(TextAlignment.LEFT);
        getGraphicsContext2D().setTextBaseline(VPos.CENTER);
    }

    @Override
    protected void draw(BamCanvas bamCanvas) {
        final double height = bamCanvas.getHeight();
        final double width = bamCanvas.getWidth();
        final double margin = bamCanvas.getAxisMargin().get();
        final double tickLength = bamCanvas.getTickLength().get();
        final double maxValue = bamCanvas.getPercentageUnits().get()
                ? 100.0 : bamCanvas.getMaxYValue().get();
        final int yTicks = bamCanvas.getyTicks().get();
        final boolean showAlleles = bamCanvas.getShowAlleles().get();

        // Y axis
        final double graphHeight = (height - 2 * margin);
        final double divisions = maxValue / yTicks;

        if (showAlleles) {
            final double step = graphHeight / (2 * divisions);
            for (double y = height / 2; y > margin; y -= step) {
                final double antiY = height - y;
                getGraphicsContext2D().strokeLine(margin - tickLength, y, width - (margin - tickLength), y);
                getGraphicsContext2D().strokeLine(margin - tickLength, antiY, width - (margin - tickLength), antiY);

            }
        } else {
            final double step = graphHeight / divisions;
            for (double y = height - margin; y > margin; y -= step) {
                getGraphicsContext2D().strokeLine(margin - tickLength, y, width - (margin - tickLength), y);
            }
        }
    }

}
