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

import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamTickLabelLayer extends BamLayer {

    public BamTickLabelLayer(GraphParameters parameters) {
        super(parameters);
        getGraphicsContext2D().setTextAlign(TextAlignment.LEFT);
        getGraphicsContext2D().setTextBaseline(VPos.CENTER);
        parameters.getGenomicPosition().addListener((ObservableValue<? extends Number> obs,
                Number o, Number n) -> repaint());
        parameters.getMaxYValue().addListener((ObservableValue<? extends Number> obs,
                Number o, Number n) -> repaint());
    }

    @Override
    protected void draw(double width, double height) {
        int position = parameters.getGenomicPosition().get();
        final double baseWidth = parameters.getBaseWidth().get();
        final double margin = parameters.getAxisMargin().get();
        final double textMargin = parameters.getTextMargin().get();
        final double tickLength = parameters.getTickLength().get();
        final double maxValue = parameters.getPercentageUnits().get()
                ? 100.0 : parameters.getMaxYValue().get();
        final int yTicks = parameters.getyTicks().get();

        // X axis
        double x = margin;
        final double h = height - 0.5 * (margin - tickLength);
        final double textWidth = margin - 2 * textMargin;
        while (x < width - margin) {
            // Only 2 digits
            final int cropPos = position++ % 100;
            getGraphicsContext2D().fillText(String.valueOf(cropPos), x, h, textWidth);
            x += baseWidth;
        }

        // Y axis
        final double graphHeight = (height - 2 * margin);
        final double divisions = maxValue / yTicks;
        final double step = graphHeight / divisions;
        double y = height - margin;
        int i = 0;
        while (y > margin) {
            if (parameters.getPercentageUnits().get()) {
                final double percentage = i * yTicks;
                getGraphicsContext2D().fillText(String.format("%3.0f%%", percentage), 0, y, textWidth);
            } else {
                final double value = i * yTicks;
                getGraphicsContext2D().fillText(String.format("%5.2f", value), 0, y, textWidth);
            }
            y -= step;
            i++;
        }
    }

}
