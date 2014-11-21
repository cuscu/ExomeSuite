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

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamBaseBackgroundLayer extends BamLayer {

    @Override
    protected void draw(double width, double height) {
        final double barwidth = getBaseWidth();
        final double barHeight = height - 2 * getAxisMargin();
        final double y = height - getAxisMargin() - barHeight;
        final double opacity = 0.1;
        int i = 0;
        double x = getAxisMargin();
        while (i < getReference().size() && x < width - getAxisMargin()) {
            switch (getReference().get(i)) {
                case 'A':
//                    getGraphicsContext2D().setFill(Color.PALEGREEN);
                    getGraphicsContext2D().setFill(A_COLOR.deriveColor(0, 1, 1, opacity));
                    getGraphicsContext2D().fillRect(x, y, barwidth, barHeight);
                    break;
                case 'C':
//                    getGraphicsContext2D().setFill(Color.LIGHTCYAN);
                    getGraphicsContext2D().setFill(C_COLOR.deriveColor(0, 1, 1, opacity));
                    getGraphicsContext2D().fillRect(x, y, barwidth, barHeight);
                    break;
                case 'T':
//                    getGraphicsContext2D().setFill(Color.LIGHTPINK);
                    getGraphicsContext2D().setFill(T_COLOR.deriveColor(0, 1, 1, opacity));
                    getGraphicsContext2D().fillRect(x, y, barwidth, barHeight);
                    break;
                case 'G':
                    getGraphicsContext2D().setFill(G_COLOR.deriveColor(0, 1, 1, opacity));
//                    getGraphicsContext2D().setFill(Color.LIGHTGRAY);
                    getGraphicsContext2D().fillRect(x, y, barwidth, barHeight);
                    break;
            }
            i++;
            x += getBaseWidth();
        }
    }

}
