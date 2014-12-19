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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The task that call variants with GATK.
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Caller extends SystemTask {

    private final String genome, output, input, dbsnp;

    /**
     * Creates a new Caller.
     *
     * @param genome the reference genome
     * @param output the output vcf
     * @param input the input bam
     * @param dbsnp the dbsnp vcf file
     */
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
        updateMessage("Calling SNPs and indels...");
        int ret = haplotypeCaller(genome, dbsnp, input, output);
        updateMessage("Done.");
        updateProgress(1, 1);
        return ret;
    }

    private int haplotypeCaller(String genome, String dbsnp, String input, String output) {
        final String gatk = "software" + File.separator + "gatk" + File.separator
                + "GenomeAnalysisTK.jar";
        /* java -jar GenomeAnalysisTK.jar -T HaplotypeCaller \
         * -R genome -I input.bam -o output.vcf \
         * --dbsnp dbsnp.vcf
         */
        ProcessBuilder pb = new ProcessBuilder(
                "software/jre1.7.0_71/bin/java", "-jar", gatk,
                "-T", "HaplotypeCaller", "-R", genome,
                "-I", input, "-o", output,
                "--dbsnp", dbsnp);
        pb.redirectErrorStream(true);
        try {
            process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.lines().forEach(line -> {
                printStream.println(line);
//                updateMessage(line);
                calculateProgress(line);
            });
            return process.waitFor();
        } catch (IOException | InterruptedException ex) {
            MainViewController.printException(ex);
        }
        return -1;
    }

    private void calculateProgress(String line) {
        int posOfP = line.indexOf("%");
        if (posOfP == -1) {
            return;
        }
        int j = posOfP;
        while (j > 0) {
            if (line.charAt(j) == ' ') {
                break;
            }
            j--;
        }
        double progress = Double.valueOf(line.substring(j + 1, posOfP));
        System.out.println("progress: " + progress);
        updateProgress(progress, 100.);
    }
}
