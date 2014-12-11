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

import exomesuite.utils.FileManager;
import java.io.File;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class PathParam extends Param<File> {

    @Override
    protected File editPassive() {
        File parent = null;
        if (getValue() != null) {
            parent = getValue().getParentFile();
        }
        File file = FileManager.openDirectory("Select " + getTitle(), parent);
        return file;
    }

}
