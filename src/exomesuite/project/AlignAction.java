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

import exomesuite.systemtask.Aligner;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.OS;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class AlignAction extends Action {

    private String output;
    List<String> errors;

    public AlignAction(String icon, String description, String disableDescription) {
        super(icon, description, disableDescription);
    }

    @Override
    public boolean isDisabled(Project project) {
        return project == null ? true
                : !project.contains(Project.PropertyName.FORWARD_FASTQ)
                || !project.contains(Project.PropertyName.REVERSE_FASTQ);
    }

    @Override
    public SystemTask getTask(Project project) {
        errors = new ArrayList<>();
        final String name = project.getProperty(Project.PropertyName.CODE);
        final String forward = project.getProperty(Project.PropertyName.FORWARD_FASTQ);
        final String reverse = project.getProperty(Project.PropertyName.REVERSE_FASTQ);
        final boolean illumina = project.
                getProperty(Project.PropertyName.FASTQ_ENCODING).equals("phred+64");
        final String temp = OS.getTempDir();
        final String genomeVersion = project.getProperty(Project.PropertyName.REFERENCE_GENOME);
        String genome = null;
        if (genomeVersion == null) {
            errors.add("Not genome specified.");
        } else {
            genome = OS.getProperty(genomeVersion);
        }
        final String dbsnp = OS.getProperty("dbsnp");
        if (dbsnp == null) {
            errors.add("Not dbSNP specified.");
        }
        final String mills = OS.getProperty("mills");
        if (mills == null) {
            errors.add("Not MILLS database specified.");
        }
        final String phase1 = OS.getProperty("phase1");
        if (phase1 == null) {
            errors.add("Not Phase 1 database specified.");
        }
        final String path = project.getProperty(Project.PropertyName.PATH);
        output = path + File.separator + name + ".bam";
        if (errors.isEmpty()) {
            return new Aligner(temp, forward, reverse, genome, dbsnp, mills, phase1, output,
                    name, illumina);
        } else {
            String msg = "";
            msg = errors.stream().map((s) -> s + "\n").reduce(msg, String::concat);
            Dialogs.create().title("Alignment parameters errors").message(msg).showError();
            return null;
        }
    }

    @Override
    public void onSucceeded(Project p, SystemTask t) {
        if (t.getValue() == 0) {
            p.setProperty(Project.PropertyName.BAM_FILE, output);
        }
    }

}
