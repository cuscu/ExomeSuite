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
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class AlignAction extends Action {

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
        final List<String> errors = new ArrayList<>();
        final String name = project.getProperty(Project.PropertyName.CODE);
        final String forward = project.getProperty(Project.PropertyName.FORWARD_FASTQ);
        final String reverse = project.getProperty(Project.PropertyName.REVERSE_FASTQ);
        String dbsnp = null, mills = null, phase1 = null;
        final boolean illumina = project.
                getProperty(Project.PropertyName.FASTQ_ENCODING).equals("phred+64");
        final String temp = OS.getTempDir();
        final String genomeVersion = project.getProperty(Project.PropertyName.REFERENCE_GENOME);
        String genome = null;
        if (genomeVersion == null) {
            errors.add("Not genome specified in project.");
        } else {
            genome = OS.getProperty(genomeVersion);
        }
        if (!FileManager.tripleCheck(genome)) {
            errors.add("Genome " + genomeVersion + " is not in databases.");
        }
        final boolean gatkRefine = genomeVersion != null && genomeVersion.equalsIgnoreCase("grch37");
        if (gatkRefine) {
            dbsnp = OS.getProperty("dbsnp");
            if (!FileManager.tripleCheck(dbsnp)) {
                errors.add("Not dbSNP specified.");
            }
            mills = OS.getProperty("mills");
            if (!FileManager.tripleCheck(mills)) {
                errors.add("Not MILLS database specified.");
            }
            phase1 = OS.getProperty("phase1");
            if (!FileManager.tripleCheck(phase1)) {
                errors.add("Not Phase 1 database specified.");
            }
        }
        final String path = project.getProperty(Project.PropertyName.PATH);
        if (!FileManager.tripleCheck(path)) {
            errors.add("Not path specified in project.");
        }
        String output = path + File.separator + name + ".bam";
        File out = new File(output);
        if (out.exists()) {
            org.controlsfx.control.action.Action ret = Dialogs.create().title("Repeat Call?")
                    .message("File " + out.getAbsolutePath() + " exists."
                            + " Do you want to repeat the alignment?").showConfirm();
            if (ret == Dialog.ACTION_CANCEL || ret == Dialog.ACTION_NO) {
                return null;
            }
        }
        if (errors.isEmpty()) {
            Aligner aligner = new Aligner(temp, forward, reverse, genome, dbsnp, mills, phase1,
                    output, name, illumina, gatkRefine);
            aligner.setOnSucceeded(e -> {
                if (aligner.getValue() == 0) {
                    project.addExtraFile(output);
                }
            });
            return aligner;
        } else {
            String msg = "";
            msg = errors.stream().map((s) -> s + "\n").reduce(msg, String::concat);
            Dialogs.create().title("Alignment parameters errors").message(msg).showError();
            return null;
        }
    }

}
