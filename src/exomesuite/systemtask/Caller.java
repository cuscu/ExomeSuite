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

import exomesuite.ExomeSuite;
import exomesuite.MainViewController;
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
        // Check if genome is already indexed.
        if (!Indexer.isIndexed(new File(genome))) {
            updateMessage(ExomeSuite.getResources().getString("indexing.genome"));
            Indexer index = new Indexer(genome);
            index.setPrintStream(printStream);
            index.call();
        }
        // So easy, only one command.
        String message = ExomeSuite.getStringFormatted("calling.title", new File(output).getName());
        updateTitle(message);
        updateMessage(ExomeSuite.getResources().getString("calling.variants"));
        int ret = haplotypeCaller(genome, dbsnp, input, output);
        updateMessage(ExomeSuite.getResources().getString("done"));
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
        } catch (IOException ex) {
            MainViewController.printException(ex);
        } catch (InterruptedException ex) {
        }
        return -1;
    }

    /**
     * Calculates progress of algorithm. In fact, this method locates the symbol % in the current
     * line. If present, uses the number before the % to update the progress.
     *
     *
     * @param line
     */
    private void calculateProgress(String line) {
        int posOfP = line.indexOf("%");
        if (posOfP == -1) {
            return;
        }
        int j = posOfP;
        while (j > 0) {
            if (Character.isDigit(line.charAt(j - 1))) {
                j--;
            } else {
                break;
            }
        }
        try {
            double progress = Double.valueOf(line.substring(j, posOfP));
            updateProgress(progress, 100.);
        } catch (NumberFormatException ex) {
        }
    }
}
