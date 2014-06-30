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

import exomesuite.utils.Config;
import exomesuite.utils.OS;
import java.io.File;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Caller extends SystemTask {

    private String genome, output, input, dbsnp;

    @Override
    public boolean configure(Config mainConfig, Config projectConfig) {
        try {
            genome = mainConfig.getProperty(Config.GENOME);
            dbsnp = mainConfig.getProperty(Config.DBSNP);
            String name = projectConfig.getProperty(Config.NAME);
            // Input = path/alignments/name.bam
            input = new File(projectConfig.getProperty("align_path"),
                    name + ".bam").getAbsolutePath();
            // Output = path/call/name.vcf
            output = new File(projectConfig.getProperty("call_path"),
                    name + ".vcf").getAbsolutePath();
            System.out.println("genome: " + genome);
            System.out.println("dbsnp: " + dbsnp);
            System.out.println("input: " + input);
            System.out.println("output: " + output);

        } catch (NullPointerException ex) {
            return false;
        }
        return true;
    }

    @Override
    protected Integer call() throws Exception {
        // So easy, only one command.
        updateTitle("Calling " + new File(output).getName());
        String gatk = "software" + File.separator + "gatk" + File.separator
                + "GenomeAnalysisTK.jar";
        updateProgress(50, 100);
        updateMessage("Calling SNPs and indels...");
        int ret = execute(OS.scanJava7(), "-jar", gatk,
                "-T", "HaplotypeCaller", "-R", genome,
                "-I", input, "-o", output,
                "--dbsnp", dbsnp);
        updateMessage("Done.");
        updateProgress(1, 1);
        return ret;
    }
}
