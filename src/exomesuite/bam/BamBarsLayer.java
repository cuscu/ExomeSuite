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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

/**
 * Layer that prints bars.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public final class BamBarsLayer extends BamLayer {

    private double barRelativeWidth;

    public BamBarsLayer(GraphParameters parameters) {
        super(parameters);
        this.barRelativeWidth = 0.8;
        parameters.getMaxYValue().addListener((ObservableValue<? extends Number> obs, Number p,
                Number n) -> repaint());
    }

    @Override
    protected void draw(double width, double height) {
        /*
         4 bars: ATCG.
         We leave space of the base margins (barRelativeWidth) and divide the space in 4 bars:

         |______| baseWidth
         | ____ | baseWidth * barRelativeWidth
         | _    | barWidth = baseWidth * barRelativeWidth * 0.25
         | atcg |

         */
        final double baseWidth = parameters.getBaseWidth().get();
        final double margin = parameters.getAxisMargin().get();
        final double barwidth = barRelativeWidth * baseWidth * 0.25;
        final double y = height - margin;
        final double maxHeigth = height - 2 * margin;
        List<Map<Character, Integer>> list = parameters.getValues();
        int i = 0;
        double x = margin + (1.0 - barRelativeWidth) * 0.5 * baseWidth;
        while (i < list.size() && x < width - margin) {
            if (list.get(i) == null) {
                i++;
                continue;
            }
            int A = list.get(i).getOrDefault('A', 0);
            int T = list.get(i).getOrDefault('T', 0);
            int C = list.get(i).getOrDefault('C', 0);
            int G = list.get(i).getOrDefault('G', 0);
            SortedMap<Integer, Character> m = new TreeMap<>(Collections.reverseOrder());
            // Sort values
            m.put(A, 'A');
            m.put(G, 'G');
            m.put(C, 'C');
            m.put(T, 'T');
            // Percentage proportion
            final double sum = A + T + G + C;
            final double prop = maxHeigth / sum;
            final double absProp = maxHeigth / parameters.getMaxYValue().get();
            // Select values ordered
            Character base = 'N';
            if (parameters.getReference().size() > i) {
                base = parameters.getReference().get(i);
            }
            // By default, no color.
            getGraphicsContext2D().setFill(Color.DARKGRAY);
            for (Map.Entry<Integer, Character> entry : m.entrySet()) {
                double barHeight = parameters.getPercentageUnits().get()
                        ? entry.getKey() * prop : entry.getKey() * absProp;
                // Select paint
                switch (entry.getValue()) {
                    case 'A':
                        // Color if set on options or if variant.
                        if (parameters.getBaseColors().get() || base != 'A') {
                            getGraphicsContext2D().setFill(A_COLOR);
                        }
                        getGraphicsContext2D().fillRect(x, y - barHeight, barwidth, barHeight);
                        break;
                    case 'T':
                        if (parameters.getBaseColors().get() || base != 'T') {
                            getGraphicsContext2D().setFill(T_COLOR);
                        }
                        getGraphicsContext2D().fillRect(x + barwidth, y - barHeight, barwidth, barHeight);
                        break;
                    case 'C':
                        if (parameters.getBaseColors().get() || base != 'C') {
                            getGraphicsContext2D().setFill(C_COLOR);
                        }
                        getGraphicsContext2D().fillRect(x + 2 * barwidth, y - barHeight, barwidth, barHeight);
                        break;
                    case 'G':
                        if (parameters.getBaseColors().get() || base != 'G') {
                            getGraphicsContext2D().setFill(G_COLOR);
                        }
                        getGraphicsContext2D().fillRect(x + 3 * barwidth, y - barHeight, barwidth, barHeight);
                        break;
                }
            }
            x += baseWidth;
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
