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
import javafx.scene.paint.Color;

/**
 * Layer that prints bars.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public final class BamBarsLayer extends BamLayer {

    private double barRelativeWidth;

    /**
     * Creates a layer that displays the bars.
     */
    public BamBarsLayer() {
        this.barRelativeWidth = 0.8;
    }

    /**
     * A number between 0 and 1 with the maximum space the 4 bars can use on every base. For
     * instance, a barRelativeWidth of 0.6 will use 60% of the space and each bar will use 15%. By
     * default it is 0.8.
     *
     * @param barRelativeWidth the relative space to use
     */
    public void setBarRelativeWidth(double barRelativeWidth) {
        this.barRelativeWidth = barRelativeWidth;
    }

    /**
     * A number between 0 and 1 with the maximum space the 4 bars can use on every base. For
     * instance, a barRelativeWidth of 0.6 will use 60% of the space and each bar will use 15%. By
     * default it is 0.8.
     *
     * @return the realativeWidth
     */
    public double getBarRelativeWidth() {
        return barRelativeWidth;
    }

    @Override
    protected void draw(BamCanvas bamCanvas) {
        if (bamCanvas.getShowAlleles().get()) {
            drawWithAlleles(bamCanvas);
        } else {
            drawWithoutAlleles(bamCanvas);
        }
    }

    private void drawWithAlleles(BamCanvas bamCanvas) {
        /*
         4 bars: ATCG.
         We leave space of the base margins (barRelativeWidth) and divide the space in 4 bars:

         |______| baseWidth
         | ____ | baseWidth * barRelativeWidth
         | _    | barWidth = baseWidth * barRelativeWidth * 0.25
         | atcg |
         */
        final double height = bamCanvas.getHeight();
        final double width = bamCanvas.getWidth();
        final double baseWidth = bamCanvas.getBaseWidth().get();
        final double margin = bamCanvas.getAxisMargin().get();
        final double barwidth = barRelativeWidth * baseWidth * 0.25;
        final double y = height * 0.5;
        final double maxHeigth = 0.5 * height - margin;
        final boolean inPercentage = bamCanvas.getPercentageUnits().get();
        final boolean inColor = bamCanvas.getBaseColors().get();
        List<PileUp> list = bamCanvas.getAlignments();
        double x = margin + (1.0 - barRelativeWidth) * 0.5 * baseWidth;
        for (PileUp pileUp : list) {
            if (x < width - margin) {
                int A = pileUp.getDepth('A');
                int T = pileUp.getDepth('T');
                int C = pileUp.getDepth('C');
                int G = pileUp.getDepth('G');
                int a = pileUp.getDepth('a');
                int t = pileUp.getDepth('t');
                int c = pileUp.getDepth('c');
                int g = pileUp.getDepth('g');
                char reference = pileUp.getReference();
                // Percentage proportion
                final double sumF = A + T + G + C;
                final double sumR = a + c + g + t;
                final double unitF = maxHeigth / ((inPercentage) ? sumF : bamCanvas.getMaxYValue().get());
                final double unitR = maxHeigth / ((inPercentage) ? sumR : bamCanvas.getMaxYValue().get());

//            final double prop = maxHeigth / sum;
//            final double absProp = maxHeigth / bamCanvas.getMaxYValue().get();
                // By default, no color scheme
                // A
                double barheigth = A * unitF;
                if (inColor || reference != 'A') {
                    getGraphicsContext2D().setFill(A_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x, y - barheigth, barwidth, barheigth);
                // a
                barheigth = a * unitR;
                if (inColor || reference != 'A') {
                    getGraphicsContext2D().setFill(A_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x, y, barwidth, barheigth);
                // C
                barheigth = C * unitF;
                if (inColor || reference != 'C') {
                    getGraphicsContext2D().setFill(C_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + barwidth, y - barheigth, barwidth, barheigth);
                // c
                barheigth = c * unitR;
                if (inColor || reference != 'C') {
                    getGraphicsContext2D().setFill(C_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + barwidth, y, barwidth, barheigth);
                // G
                barheigth = G * unitF;
                if (inColor || reference != 'G') {
                    getGraphicsContext2D().setFill(G_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + 2 * barwidth, y - barheigth, barwidth, barheigth);
                // g
                barheigth = g * unitR;
                if (inColor || reference != 'G') {
                    getGraphicsContext2D().setFill(G_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + 2 * barwidth, y, barwidth, barheigth);
                // T
                barheigth = T * unitF;
                if (inColor || reference != 'T') {
                    getGraphicsContext2D().setFill(T_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + 3 * barwidth, y - barheigth, barwidth, barheigth);
                // t
                barheigth = t * unitR;
                if (inColor || reference != 'T') {
                    getGraphicsContext2D().setFill(T_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + 3 * barwidth, y, barwidth, barheigth);
                x += baseWidth;
            } else {
                break;
            }
        }
    }

    private void drawWithoutAlleles(BamCanvas bamCanvas) {
        /*
         4 bars: ATCG.
         We leave space of the base margins (barRelativeWidth) and divide the space in 4 bars:

         |______| baseWidth
         | ____ | baseWidth * barRelativeWidth
         | _    | barWidth = baseWidth * barRelativeWidth * 0.25
         | atcg |

         */
        final double height = bamCanvas.getHeight();
        final double width = bamCanvas.getWidth();
        final double baseWidth = bamCanvas.getBaseWidth().get();
        final double margin = bamCanvas.getAxisMargin().get();
        final double barwidth = barRelativeWidth * baseWidth * 0.25;
        final double y = height - margin;
        final double maxHeigth = height - 2 * margin;
        final boolean inPercentage = bamCanvas.getPercentageUnits().get();
        final boolean inColor = bamCanvas.getBaseColors().get();
//        List<Map<Character, Integer>> list = bamCanvas.getValues();
        List<PileUp> list = bamCanvas.getAlignments();
        double x = margin + (1.0 - barRelativeWidth) * 0.5 * baseWidth;
        for (PileUp pileUp : list) {
            if (x < width - margin) {
                int A = pileUp.getDepth('A') + pileUp.getDepth('a');
                int T = pileUp.getDepth('T') + pileUp.getDepth('t');
                int C = pileUp.getDepth('C') + pileUp.getDepth('c');
                int G = pileUp.getDepth('G') + pileUp.getDepth('g');
                char reference = pileUp.getReference();
                // Percentage proportion
                final double sum = A + T + G + C;
                final double unit = maxHeigth / ((inPercentage) ? sum : bamCanvas.getMaxYValue().get());
//            final double prop = maxHeigth / sum;
//            final double absProp = maxHeigth / bamCanvas.getMaxYValue().get();
                // By default, no color scheme
                // A
                double barheigth = A * unit;
                if (inColor || reference != 'A') {
                    getGraphicsContext2D().setFill(A_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x, y - barheigth, barwidth, barheigth);
                // C
                barheigth = C * unit;
                if (inColor || reference != 'C') {
                    getGraphicsContext2D().setFill(C_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + barwidth, y - barheigth, barwidth, barheigth);
                // G
                barheigth = G * unit;
                if (inColor || reference != 'G') {
                    getGraphicsContext2D().setFill(G_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + 2 * barwidth, y - barheigth, barwidth, barheigth);
                // T
                barheigth = T * unit;
                if (inColor || reference != 'T') {
                    getGraphicsContext2D().setFill(T_COLOR);
                } else {
                    getGraphicsContext2D().setFill(Color.DARKGRAY);
                }
                getGraphicsContext2D().fillRect(x + 3 * barwidth, y - barheigth, barwidth, barheigth);
                x += baseWidth;
            } else {
                break;
            }
        }
    }
}
