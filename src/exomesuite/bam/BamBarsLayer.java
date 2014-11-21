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

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Layer that prints bars.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public final class BamBarsLayer extends BamLayer {

    private double barRelativeWidth;

    public BamBarsLayer() {
        this.barRelativeWidth = 0.8;
    }

    @Override
    protected void draw(double width, double height) {
        final double barwidth = barRelativeWidth * getBaseWidth() * 0.25;
        final double y = height - getAxisMargin();
        final double maxHeigth = height - 2 * getAxisMargin();

        int i = 0;
        double pos = getAxisMargin() + (1.0 - barRelativeWidth) * 0.5 * getBaseWidth();
        while (i < getValues().size() && pos < width - getAxisMargin()) {
            if (getValues().get(i) == null) {
                i++;
                continue;
            }
            int A = getValues().get(i).getOrDefault('A', 0);
            int T = getValues().get(i).getOrDefault('T', 0);
            int C = getValues().get(i).getOrDefault('C', 0);
            int G = getValues().get(i).getOrDefault('G', 0);
            SortedMap<Integer, Character> m = new TreeMap<>(Collections.reverseOrder());
            // Sort values
            m.put(A, 'A');
            m.put(G, 'G');
            m.put(C, 'C');
            m.put(T, 'T');
            final double sum = A + T + G + C;
            final double prop = 1.0 / sum * maxHeigth;

            // Select values ordered
            for (Map.Entry<Integer, Character> entry : m.entrySet()) {
                double barHeight = entry.getKey() * prop;
                // Select paint
                switch (entry.getValue()) {
                    case 'A':
                        getGraphicsContext2D().setFill(A_COLOR);
                        getGraphicsContext2D().fillRect(pos, y - barHeight, barwidth, barHeight);
                        break;
                    case 'T':
                        getGraphicsContext2D().setFill(T_COLOR);
                        getGraphicsContext2D().fillRect(pos + barwidth, y - barHeight, barwidth, barHeight);
                        break;
                    case 'C':
                        getGraphicsContext2D().setFill(C_COLOR);
                        getGraphicsContext2D().fillRect(pos + 2 * barwidth, y - barHeight, barwidth, barHeight);
                        break;
                    case 'G':
                        getGraphicsContext2D().setFill(G_COLOR);
                        getGraphicsContext2D().fillRect(pos + 3 * barwidth, y - barHeight, barwidth, barHeight);
                        break;
                }
            }

            pos += getBaseWidth();
            i++;
        }
    }

    public void setBarRelativeWidth(double barRelativeWidth) {
        this.barRelativeWidth = barRelativeWidth;
    }

    public double getBarRelativeWidth() {
        return barRelativeWidth;
    }

}
