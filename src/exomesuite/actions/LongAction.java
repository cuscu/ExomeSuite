/*
 * Copyright (C) 2015 unidad03
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
package exomesuite.actions;

import exomesuite.project.Project;
import exomesuite.systemtask.SystemTask;

/**
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public abstract class LongAction {

    public abstract String getName();

    /**
     * Only the path to the icon. The size will be determined by PActions.
     *
     * @return
     */
    public abstract String getIconPath();

    public abstract boolean isDisable(Project project);

    /**
     * Null if parameters are wrong or user canceled.
     *
     * @param project
     * @return the task or null if you don't want to create a tab panel.
     */
    public abstract SystemTask getTask(Project project);

}
