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
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamAxisYLabelsLayer extends BamLayer {

    public BamAxisYLabelsLayer() {
        getGraphicsContext2D().setTextAlign(TextAlignment.LEFT);
        getGraphicsContext2D().setTextBaseline(VPos.CENTER);
    }

    @Override
    protected void draw(BamCanvas bamCanvas) {
        final double height = bamCanvas.getHeight();
        final double margin = bamCanvas.getAxisMargin().get();
        final double maxValue = bamCanvas.getPercentageUnits().get()
                ? 100.0 : bamCanvas.getMaxYValue().get();
        final int yTicks = bamCanvas.getyTicks().get();
        final double textMargin = bamCanvas.getTextMargin().get();
        final boolean showAlleles = bamCanvas.getShowAlleles().get();
        final double graphHeight = (height - 2 * margin);
        final double textWidth = margin - 2 * textMargin;

        final double divisions = maxValue / yTicks;
        if (showAlleles) {
            final double step = graphHeight / (2 * divisions);
            int i = 0;
            for (double y = height / 2; y > margin; y -= step) {
                final double value = i * yTicks;
                final double antiHeigth = height - y;
                if (bamCanvas.getPercentageUnits().get()) {
                    getGraphicsContext2D().fillText(String.format("%3.0f%%", value), 0, y, textWidth);
                    getGraphicsContext2D().fillText(String.format("%3.0f%%", value), 0, antiHeigth, textWidth);
                } else {
                    getGraphicsContext2D().fillText(String.format("%5.2f", value), 0, y, textWidth);
                    getGraphicsContext2D().fillText(String.format("%5.2f", value), 0, antiHeigth, textWidth);
                }
                i++;
            }
        } else {
            int i = 0;
            final double step = graphHeight / divisions;
            for (double y = height - margin; y > margin; y -= step) {
                final double value = i * yTicks;
                if (bamCanvas.getPercentageUnits().get()) {
                    getGraphicsContext2D().fillText(String.format("%3.0f%%", value), 0, y, textWidth);
                } else {
                    getGraphicsContext2D().fillText(String.format("%5.2f", value), 0, y, textWidth);
                }
                i++;
            }
        }
    }

}
