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
 *
 * @author Pascual Lorente Arencibia
 */
public class BamAxisLayer extends BamLayer {

    public BamAxisLayer() {
        getGraphicsContext2D().setStroke(Color.BLACK);
    }

    @Override
    protected void draw(double width, double height) {
        // Y axis
        getGraphicsContext2D().strokeLine(getAxisMargin(), getAxisMargin(),
                getAxisMargin(), height - (getAxisMargin() - getTickLength()));
        // X axis
        getGraphicsContext2D().strokeLine(getAxisMargin() - getTickLength(), height - getAxisMargin(),
                width - getAxisMargin(), height - getAxisMargin());
    }

}
