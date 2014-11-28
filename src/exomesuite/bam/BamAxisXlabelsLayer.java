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

import java.util.List;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamAxisXlabelsLayer extends BamLayer {

    public BamAxisXlabelsLayer() {
        getGraphicsContext2D().setTextAlign(TextAlignment.CENTER);
    }

    @Override
    protected void draw(BamCanvas bamCanvas) {
        final double margin = bamCanvas.getAxisMargin().get();
        final double baseWidth = bamCanvas.getBaseWidth().get();
        final double tickLength = bamCanvas.getTickLength().get();
        final int start = bamCanvas.getGenomicPosition().get();
        final double textMargin = bamCanvas.getTextMargin().get();
        final double height = bamCanvas.getHeight();
        final double width = bamCanvas.getWidth();
        final List<PileUp> reference = bamCanvas.getAlignments();
        // X axis
        int gPos = start;
        int j = 0;
        final double h = height - 0.5 * (margin - tickLength);
        final double xTextWidth = baseWidth - 2 * textMargin;
        for (double x = margin + baseWidth; x < width - margin; x += baseWidth, j++) {
            // Labels
            if (j < reference.size() && reference.get(j).getReference() == '*') {
                getGraphicsContext2D().fillText("*", x - 0.5 * baseWidth, h, xTextWidth);
            } else {
                final int cropPos = gPos++ % 100;
                getGraphicsContext2D().fillText(String.format("%2d", cropPos), x - 0.5 * baseWidth, h, xTextWidth);
            }
        }
    }

}
