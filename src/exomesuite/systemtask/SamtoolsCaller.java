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
        updateTitle("samtools call " + new File(input).getName());
        // Command 1
        // samtools mpileup -u -f DNA_Sequencing/genome/human_g1k_v37.fasta DNA_Sequencing/niv_19_2013.bam | bcftools view -bvcg - > var.raw.bcf
//        ProcessBuilder pb1 = new ProcessBuilder("samtools", "mpileup", "-u", "-f", genome, input);
//        ProcessBuilder pb2 = new ProcessBuilder("bcftools", "view", "-bvcg", "-");
        File pipe2 = new File("pipe2.bcf");
//        pipe2.delete();
//        pb2.redirectOutput(pipe2);
//        updateMessage("Piling up");
//        updateProgress(0.2, 1);
//        p1 = pb1.start();
//        p2 = pb2.start();
//        // Pipe
//        new PipeUtil(p1.getInputStream(), p2.getOutputStream()).start();
//        // Outputs
//        new PipeUtil(p1.getErrorStream(), printStream).start();
//        new PipeUtil(p2.getErrorStream(), printStream).start();
//        p1.waitFor();
//        updateMessage("view 1");
//        updateProgress(0.4, 1);
//        p2.waitFor();
        updateMessage("view 2");
        updateProgress(0.6, 1);

        // Part 2
        // bcftools view var.raw.bcf | /usr/bin/vcfutils.pl varFilter -D100 > var.flt.vcf
        ProcessBuilder pb3 = new ProcessBuilder("bcftools", "view", pipe2.getAbsolutePath());
        ProcessBuilder pb4 = new ProcessBuilder("/usr/bin/vcfutils.pl", "varFilter", "-D100");
//        pb3.redirectInput(pipe2);
        pb4.redirectOutput(new File(output));
        p3 = pb3.start();
        p4 = pb4.start();
        // Pipe
        new PipeUtil(p3.getInputStream(), p4.getOutputStream()).start();
        // Outputs
        new PipeUtil(p3.getErrorStream(), printStream).start();
        new PipeUtil(p4.getErrorStream(), printStream).start();
        p3.waitFor();
        p3.getErrorStream().close();
        updateMessage("Search");
        updateProgress(0.60, 1);
        p4.waitFor();
        updateMessage("Done");
        updateProgress(1, 1);

        pipe2.delete();
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
            int c;
            try {
                byte[] b = new byte[512];
                while ((c = input.read(b)) != -1) {
                    output.write(b);
                }
            } catch (IOException ex) {
                Logger.getLogger(SamtoolsCaller.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
