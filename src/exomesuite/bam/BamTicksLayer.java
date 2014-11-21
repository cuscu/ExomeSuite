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

import javafx.scene.paint.Color;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamTicksLayer extends BamLayer {

    public BamTicksLayer(GraphParameters parameters) {
        super(parameters);
        getGraphicsContext2D().setStroke(Color.GRAY);
    }

    @Override
    protected void draw(double width, double height) {
        final double margin = parameters.getAxisMargin().get();
        final double baseWidth = parameters.getBaseWidth().get();
        final double tickLength = parameters.getTickLength().get();
        final double maxValue = parameters.getPercentageUnits().get()
                ? 100.0 : parameters.getMaxYValue().get();
        final int yTicks = parameters.getyTicks().get();
        // X axis
        for (double x = margin + baseWidth; x < width - margin; x += baseWidth) {
            getGraphicsContext2D().strokeLine(x, margin, x, height - (margin - tickLength));
        }

        // Y axis
        final double graphHeight = (height - 2 * margin);
        final double divisions = maxValue / yTicks;
        final double step = graphHeight / divisions;
        double y = height - margin;
        while (y > margin) {
            getGraphicsContext2D().strokeLine(margin - tickLength, y, width - (margin - tickLength), y);
            y -= step;
        }
    }

}
