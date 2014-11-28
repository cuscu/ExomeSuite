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
package exomesuite.graphic;

import javafx.scene.canvas.Canvas;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public abstract class ResizableCanvas extends Canvas {

    public ResizableCanvas() {
        widthProperty().addListener(e -> repaint());
        heightProperty().addListener(e -> repaint());
    }

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

    protected abstract void draw(double width, double height);

    /**
     * Clears layer and repaints it.
     */
    public void repaint() {
        getGraphicsContext2D().clearRect(0, 0, getWidth(), getHeight());
        draw(getWidth(), getHeight());
    }
}
