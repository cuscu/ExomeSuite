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

import exomesuite.systemtask.Recalibrator;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.OS;
import java.io.File;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class RecalibrateAction extends Action {

    private String output;

    public RecalibrateAction(String icon, String description, String disableDescription) {
        super(icon, description, disableDescription);
    }

    @Override
    public boolean isDisabled(Project project) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SystemTask getTask(Project project) {
        final String omni = OS.getProperty("omni");
        final String mills = OS.getProperty("mills");
        final String hapmap = OS.getProperty("hapmap");
        final String dbsnp = OS.getProperty("dbsnp");
        final String genome = OS.getProperty(project.getProperty(
                Project.PropertyName.REFERENCE_GENOME));
        final String temp = OS.getTempDir();
        String name = project.getProperty(Project.PropertyName.CODE);
        // Input = path/code.vcf
        final String input = project.getProperty(Project.PropertyName.CODE) + ".vcf";
        // Output = path/recal/name.vcf
        output = project.getProperty(Project.PropertyName.PATH) + File.separator
                + project.getProperty(Project.PropertyName.CODE) + "_recal.vcf";
        return new Recalibrator(input, output, omni, mills, hapmap, dbsnp, genome, temp);
    }

    @Override
    public void onSucceeded(Project p, SystemTask t) {
        p.setProperty(Project.PropertyName.RECAL_VCF_FILE, output);
    }

}
