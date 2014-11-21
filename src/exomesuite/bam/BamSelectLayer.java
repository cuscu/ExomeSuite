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
import javafx.scene.paint.Color;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamSelectLayer extends BamLayer {

    public BamSelectLayer() {
        getGraphicsContext2D().setFill(Color.LIGHTCYAN);
        getSelectedIndex().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            repaint();
        });
    }

    @Override
    protected void draw(double width, double height) {
        int index = getSelectedIndex().get();
        if (0 <= index && index < getValues().size()) {
            final double barHeight = getHeight() - 2 * getAxisMargin();
            final double barwidth = getBaseWidth();
            final double pos = getAxisMargin() + index * getBaseWidth();
            final double h = getHeight() - getAxisMargin() - barHeight;
            getGraphicsContext2D().fillRect(pos, h, barwidth, barHeight);
        }
    }

}
