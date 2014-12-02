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

import javafx.scene.image.ImageView;

/**
 * You can specify a custom size for the graphic. Only the width is resized and the ratio is
 * preserved.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class SizableImage extends ImageView {

    public SizableImage(String url, double size) {
        super(url);
        setFitWidth(size);
        setPreserveRatio(true);
        setSmooth(true);
    }

}
