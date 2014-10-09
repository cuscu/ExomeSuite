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

import exomesuite.systemtask.Mist;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.OS;
import java.io.File;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class MistAction extends Action {

    public MistAction(String icon, String description, String disableDescription) {
        super(icon, description, disableDescription);
    }

    @Override
    public boolean isDisabled(Project project) {
        return project == null ? true
                : !project.contains(Project.PropertyName.BAM_FILE);
    }

    @Override
    public SystemTask getTask(Project project) {
        int threshold = Integer.valueOf(project.getProperty(Project.PropertyName.THRESHOLD));
        String input = project.getProperty(Project.PropertyName.BAM_FILE);
        String output = project.getProperty(Project.PropertyName.PATH) + File.separator
                + project.getProperty(Project.PropertyName.CODE) + ".mist";
        String ensembl = OS.getProperty("ensembl");
        return new Mist(input, output, ensembl, threshold);
    }

}
