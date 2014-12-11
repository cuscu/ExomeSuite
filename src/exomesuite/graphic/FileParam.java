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
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class FileParam extends Param<File> {

    private final List<FileChooser.ExtensionFilter> filters = new ArrayList<>();
    private boolean showFullPath = false;
    private Behaviour behaviour = Behaviour.OPEN;

    @Override
    protected File editPassive() {
        File parent = null;
        if (getValue() != null) {
            parent = getValue().getParentFile();
        }
        File file;
        switch (behaviour) {
            case OPEN:
                if (parent != null) {
                    file = FileManager.openFile("Select " + getTitle(), parent, filters);
                } else {
                    file = FileManager.openFile("Select " + getTitle(), filters);
                }
                return file;
            case SAVE:
                if (parent != null) {
                    file = FileManager.saveFile("Select " + getTitle(), parent, filters);
                } else {
                    file = FileManager.saveFile("Select " + getTitle(), filters);
                }
                return file;
        }
        return null;
    }

    @Override
    protected String toLabel(File value) {
        if (value == null) {
            return getPromptText();
        }
        return showFullPath ? value.getAbsolutePath() : value.getName();
    }

    public void addFilter(FileChooser.ExtensionFilter filter) {
        filters.add(filter);
    }

    public void setShowFullPath(boolean showFullPath) {
        this.showFullPath = showFullPath;
    }

    public boolean isShowFullPath() {
        return showFullPath;
    }

    public Behaviour getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(Behaviour behaviour) {
        this.behaviour = behaviour;
    }

    public enum Behaviour {

        OPEN, SAVE
    }
}
