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
package exomesuite.project;

import exomesuite.systemtask.SystemTask;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class Action {

    String icon, description, disableDescription;
    boolean running;

    public Action(String icon, String description, String disableDescription) {
        this.icon = icon;
        this.description = description;
        this.disableDescription = disableDescription;
        this.running = false;
    }

    public abstract boolean isDisabled(Project project);

    public String getDescription() {
        return description;
    }

    public String getDisableDescription() {
        return disableDescription;
    }

    public String getIcon() {
        return icon;
    }

    /**
     * Returns a ready-to-start task, taking parameters from project.
     *
     * @param project the Project to which task is performed
     * @return a SystemTask ready to launch
     */
    public abstract SystemTask getTask(Project project);

    public void onSucceeded(Project p, SystemTask t) {

    }

    public void onCancelled(Project p, SystemTask t) {

    }

}
