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

import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual Lorente Arencibia "pasculorente@gmail.com"
 */
public class SamtoolsCaller extends SystemTask {

    private final String genome, dbsnp, input, output;

    private Process p1, p2, p3, p4;

    public SamtoolsCaller(String genome, String dbsnp, String input, String output) {
        this.genome = genome;
        this.dbsnp = dbsnp;
        this.input = input;
        this.output = output;
    }

    @Override
    protected Integer call() throws Exception {
        final String temp = OS.getTempDir();
        updateTitle("samtools call " + new File(input).getName());

        final File samtools1 = new File(temp, "samtools_1.bcf");
        final File samtools2 = new File(temp, "samtools_2.bcf");
        final File samtools3 = new File(temp, "samtools_3.vcf");

        // samtools mpileup -u -f input.bam > samtools1.bcf
        ProcessBuilder pb1 = new ProcessBuilder("samtools", "mpileup", "-u", "-f", genome, input);
        pb1.redirectOutput(samtools1);
        updateMessage("Piling up 1/2");
        updateProgress(0.2, 1);
        p1 = pb1.start();
        new PipeUtil(p1.getErrorStream(), printStream).start();

        p1.waitFor();
        updateMessage("Piling up 2/2");
        updateProgress(0.4, 1);

        // bcftools view -vcg samtools_1.bcf > samtools_2.vcf
        ProcessBuilder pb2 = new ProcessBuilder("bcftools", "view", "-vcg", samtools1.getAbsolutePath());
        pb2.redirectOutput(samtools2);
        p2 = pb2.start();
        new PipeUtil(p2.getErrorStream(), printStream).start();

        p2.waitFor();
        updateMessage("Calling variants");
        updateProgress(0.6, 1);
        samtools1.delete();
//
//        // bcftools view samtools_2.bcf > samtools_3.vcf
//        ProcessBuilder pb3 = new ProcessBuilder("bcftools", "view", samtools2.getAbsolutePath());
//        pb3.redirectOutput(samtools3);
//        p3 = pb3.start();
//        new PipeUtil(p3.getErrorStream(), printStream).start();
//
//        p3.waitFor();
//        updateMessage("Filtering");
//        updateProgress(0.80, 1);
//        samtools2.delete();

        // /usr/bin/vcfutils.pl varFilter -D100 samtools_2.vcf > output.vcf
        ProcessBuilder pb4 = new ProcessBuilder("/usr/share/samtools/vcfutils.pl",
                "varFilter", "-D100", samtools2.getAbsolutePath());
        pb4.redirectOutput(new File(output));
        p4 = pb4.start();
        new PipeUtil(p4.getErrorStream(), printStream).start();

        p4.waitFor();
        updateMessage("Done");
        updateProgress(1, 1);

        samtools2.delete();
        return 0;

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (p1 != null && p1.isAlive()) {
            p1.destroy();
        }
        if (p2 != null && p2.isAlive()) {
            p2.destroy();
        }
        return true;
    }

    private static class PipeUtil extends Thread {

        InputStream input;
        OutputStream output;

        public PipeUtil(InputStream input, OutputStream output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public void run() {
            try {
                byte[] b = new byte[512];
                while (input.read(b) != -1) {
                    output.write(b);
                }
            } catch (IOException ex) {
                Logger.getLogger(SamtoolsCaller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
