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
package exomesuite.phase;

import javafx.scene.Node;

/**
 *
 * Phase is only a temporary name to package all the tools for a single project. I don't want to use
 * the word tool because it has been used too much.
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class Phase {

    /**
     * When a property is added, removed or updated in the config, this method will be called.
     *
     */
    public void configChanged() {

    }

    public abstract Node getView();

    public abstract boolean isRunning();

    public void stop() {

    }
}
