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
 * Displays a blue box in the selected position.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamSelectLayer extends BamLayer {

    /**
     * Creates a layer that displays a blue box in the selected position.
     */
    public BamSelectLayer() {
        getGraphicsContext2D().setFill(Color.LIGHTCYAN);
    }

    @Override
    protected void draw(BamCanvas bamCanvas) {
        final double height = bamCanvas.getHeight();
        final int index = bamCanvas.getSelectedIndex().get();
        final double baseWidth = bamCanvas.getBaseWidth().get();
        final double margin = bamCanvas.getAxisMargin().get();

        if (0 <= index && index < bamCanvas.getAlignments().size()) {
            final double barHeight = height - 2 * margin;
            final double barwidth = baseWidth;
            final double pos = margin + index * baseWidth;
            final double h = height - margin - barHeight;
            getGraphicsContext2D().fillRect(pos, h, barwidth, barHeight);
        }
    }

}
