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

import exomesuite.systemtask.Caller;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.OS;
import java.io.File;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class CallAction extends Action {

    private String output;

    public CallAction(String icon, String description, String disableDescription) {
        super(icon, description, disableDescription);
    }

    @Override
    public boolean isDisabled(Project project) {
        return project == null ? true
                : !project.contains(Project.PropertyName.BAM_FILE);
    }

    @Override
    public SystemTask getTask(Project project) {
        final String genome
                = OS.getProperty(project.getProperty(Project.PropertyName.REFERENCE_GENOME));
        output = project.getProperty(Project.PropertyName.PATH) + File.separator
                + project.getProperty(Project.PropertyName.CODE) + ".vcf";
        final String input = project.getProperty(Project.PropertyName.BAM_FILE);
        final String dbsnp = OS.getProperty("dbsnp");
        return new Caller(genome, output, input, dbsnp);
    }

    @Override
    public void onSucceeded(Project p, SystemTask t) {
        if (t.getValue() == 0) {
            String files = p.getProperty(Project.PropertyName.FILES, "");
            files += output + ";";
            p.setProperty(Project.PropertyName.VCF_FILE, output);
            p.setProperty(Project.PropertyName.FILES, files);
        }
    }

}
