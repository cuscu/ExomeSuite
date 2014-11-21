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
public class BamReferencelabelLayer extends BamLayer {

    public BamReferencelabelLayer() {
        getGraphicsContext2D().setTextAlign(TextAlignment.CENTER);
        getGraphicsContext2D().setTextBaseline(VPos.BOTTOM);
    }

    @Override
    protected void draw(double width, double height) {
        final double textWidth = getBaseWidth() - 2 * getTextMargin();

        int i = 0;
        double pos = getAxisMargin() + getBaseWidth() * 0.5;
        while (i < getReference().size() && pos < width - getAxisMargin()) {
            String nucleotide = String.valueOf(getReference().get(i++));
            switch (nucleotide) {
                case "A":
                    getGraphicsContext2D().setFill(A_COLOR);
                    break;
                case "T":
                    getGraphicsContext2D().setFill(T_COLOR);
                    break;
                case "G":
                    getGraphicsContext2D().setFill(G_COLOR);
                    break;
                case "C":
                    getGraphicsContext2D().setFill(C_COLOR);
                    break;
            }
            getGraphicsContext2D().fillText(nucleotide, pos, getAxisMargin(), textWidth);
            pos += getBaseWidth();
        }
    }

}
