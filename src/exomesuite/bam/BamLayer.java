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

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

/**
 * Manages the common information of every layer. A layer which overrides this class will have
 * access to axis margins, tickLength, baseWidth, genomicData ... If a data is changed in any of the
 * layers, it will be changed in the others.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
abstract class BamLayer extends Canvas {

    protected static final Color A_COLOR = Color.rgb(255, 227, 85);
    protected static final Color T_COLOR = Color.rgb(85, 255, 96);
    protected static final Color C_COLOR = Color.rgb(255, 89, 85);
    protected static final Color G_COLOR = Color.rgb(142, 104, 255);

    /**
     * Clears and paints the whole layer.
     *
     * @param bamCanvas the bamCanvas that contains the layer
     */
    public void repaint(BamCanvas bamCanvas) {
        getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        draw(bamCanvas);
    }

    /**
     * Paints the layer, but do not clear it previously.
     *
     * @param bamCanvas the bamCanvas that contains the layer
     */
    protected abstract void draw(BamCanvas bamCanvas);

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefHeight(double width) {
        return getHeight();
    }

    @Override
    public double prefWidth(double height) {
        return getWidth();
    }

}
