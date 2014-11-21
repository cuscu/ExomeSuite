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

    public BamTickLabelLayer() {
        getGraphicsContext2D().setTextAlign(TextAlignment.CENTER);
        getGraphicsContext2D().setTextBaseline(VPos.CENTER);
        getGenomicPosition().addListener((ObservableValue<? extends Number> obs, Number o, Number n) -> {
            repaint();
        });
    }

    @Override
    protected void draw(double width, double height) {
        // X axis
        int i = getGenomicPosition().get();
        double x = getAxisMargin() + getBaseWidth() * 0.5;
        final double y = height - 0.5 * (getAxisMargin() - getTickLength());
        final double textWidth = getBaseWidth() - 0.5 * getTextMargin();
        while (x < width - getAxisMargin()) {
            // Only 2 digits
            final int cropPos = i++ % 100;
            getGraphicsContext2D().fillText(String.valueOf(cropPos), x, y, textWidth);
            x += getBaseWidth();
        }
    }

}
