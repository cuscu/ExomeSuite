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

import javafx.scene.text.TextAlignment;

/**
 * Vertical lines (X ticks)
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamAxisXLayer extends BamLayer {

    /**
     * Creates a layer that shows X ticks in vertical.
     */
    public BamAxisXLayer() {
        getGraphicsContext2D().setTextAlign(TextAlignment.CENTER);
    }

    @Override
    protected void draw(BamCanvas bamCanvas) {
        final double margin = bamCanvas.getAxisMargin().get();
        final double baseWidth = bamCanvas.getBaseWidth().get();
        final double tickLength = bamCanvas.getTickLength().get();
        final double height = bamCanvas.getHeight();
        final double width = bamCanvas.getWidth();
        final boolean showAlleles = bamCanvas.getShowAlleles().get();
        for (double x = margin + baseWidth; x < width - margin; x += baseWidth) {
            if (showAlleles) {
                getGraphicsContext2D().strokeLine(x, margin, x, height - margin);
            } else {
                getGraphicsContext2D().strokeLine(x, margin, x, height - (margin - tickLength));
            }
        }
    }
}
