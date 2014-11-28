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
package exomesuite.systemtask;

import exomesuite.MainViewController;
import exomesuite.utils.FileManager;
import java.io.File;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Caller extends SystemTask {

    private final String genome, output, input, dbsnp;

    public Caller(String genome, String output, String input, String dbsnp) {
        this.genome = genome;
        this.output = output;
        this.input = input;
        this.dbsnp = dbsnp;
    }

    @Override
    protected Integer call() throws Exception {
        String msg = "";
        if (!FileManager.tripleCheck(genome)) {
            msg += "Reference genome\n";
        }
        if (!FileManager.tripleCheck(input)) {
            msg += "Alignments\n";
        }
        if (!FileManager.tripleCheck(dbsnp)) {
            msg += "dbSNP\n";
        }
        if (!msg.isEmpty()) {
            MainViewController.printMessage("Some arguments are missing:\n" + msg, "warning");
            return 1;
        }
        // Check if genome is already indexed.
        if (!Indexer.isIndexed(new File(genome))) {
            updateMessage("Indexing genome");
            Indexer index = new Indexer(genome);
            index.setPrintStream(printStream);
            index.call();
        }
        // So easy, only one command.
        updateTitle("Calling " + new File(output).getName());
        String gatk = "software" + File.separator + "gatk" + File.separator
                + "GenomeAnalysisTK.jar";
        updateProgress(50, 100);
        updateMessage("Calling SNPs and indels...");
        int ret = execute("software/jre1.7.0_71/bin/java", "-jar", gatk,
                "-T", "HaplotypeCaller", "-R", genome,
                "-I", input, "-o", output,
                "--dbsnp", dbsnp);
        updateMessage("Done.");
        updateProgress(1, 1);
        return ret;
    }
}
