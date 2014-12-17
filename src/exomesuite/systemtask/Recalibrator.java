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
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Recalibrates a VCF using GATK recalibrator.
 *
 * @author Pascual Lorente Arencibia
 */
public class Recalibrator extends SystemTask {

    String input, output, omni, mills, hapmap, dbsnp, genome, temp;

    /**
     * Creates a new Recalibrator.
     *
     * @param input the input vcf
     * @param output the output vcf
     * @param omni the OMNI vcf
     * @param mills the MILLs vcf
     * @param hapmap the hapmap vcf
     * @param dbsnp the dbSNP vcf
     * @param genome the reference genome
     * @param temp the temp path
     */
    public Recalibrator(String input, String output, String omni, String mills, String hapmap,
            String dbsnp, String genome, String temp) {
        this.input = input;
        this.output = output;
        this.omni = omni;
        this.mills = mills;
        this.hapmap = hapmap;
        this.dbsnp = dbsnp;
        this.genome = genome;
        this.temp = temp;
    }

    @Override
    protected Integer call() throws Exception {
        String gatk = "software" + File.separator + "gatk" + File.separator + "GenomeAnalysisTK.jar";
        String java7 = "/home/unidad03/NetBeansProjects/ExomeSuite/software/jre1.7.0_71/bin/java";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss_");
        final String timestamp = "recal_" + df.format(new Date());
        File snpTemp = new File(temp, timestamp + "snp.vcf");
        File snpTranches = new File(temp, timestamp + "snp.tranches");
        File snpRecal = new File(temp, timestamp + "snp.recal");
        File indelTranches = new File(temp, timestamp + "indel.tranches");
        File indelRecal = new File(temp, timestamp + "indel.recal");
        updateMessage("Modeling SNPs...");
        updateProgress(0.5, 4);
        // 1/4 snp model
        int ret = execute(java7, "-jar", gatk,
                "-T", "VariantRecalibrator",
                "-R", genome, "-input", input,
                "-tranchesFile", snpTranches.getAbsolutePath(),
                "-recalFile", snpRecal.getAbsolutePath(),
                "-resource:hapmap,known=false,training=true,truth=true,prior=15.0", hapmap,
                "-resource:omni,known=false,training=true,truth=false,prior=12.0", omni,
                "-resource:mills,known=true,training=true,truth=true,prior=12.0", dbsnp,
                "-an", "QD", "-an", "MQRankSum", "-an", "ReadPosRankSum",
                "-an", "FS", "-an", "DP", "-mode", "SNP",
                "-tranche", "100.0", "-tranche", "99.9",
                "-tranche", "99.0", "-tranche", "90.0");
        if (ret != 0) {
            return ret;
        }
        updateMessage("Applying SNP recalibration...");
        updateProgress(1.5, 4);
        // 2/4 snp recalibration
        ret = execute(java7, "-jar", gatk,
                "-T", "ApplyRecalibration",
                "-R", genome, "-input", input,
                "-tranchesFile", snpTranches.getAbsolutePath(),
                "-recalFile", snpRecal.getAbsolutePath(),
                "-o", snpTemp.getAbsolutePath(),
                "--ts_filter_level", "90.0", "-mode", "SNP");
        if (ret != 0) {
            return ret;
        }
        updateMessage("Modeling Indels...");
        updateProgress(2.5, 4);
        // 3/4 indel model
        ret = execute(java7, "-jar", gatk,
                "-T", "VariantRecalibrator",
                "-R", genome, "-input", snpTemp.getAbsolutePath(),
                "-tranchesFile", indelTranches.getAbsolutePath(),
                "-recalFile", indelRecal.getAbsolutePath(),
                "-resource:mills,known=true,training=true,truth=true,prior=12.0", mills,
                "-an", "MQRankSum", "-an", "ReadPosRankSum",
                "-an", "FS", "-an", "DP", "-mode", "INDEL",
                "-tranche", "100.0", "-tranche", "99.9",
                "-tranche", "99.0", "-tranche", "90.0");
        if (ret != 0) {
            return ret;
        }
        updateTitle("Applying Indel recalibration...");
        updateProgress(3.5, 4);
        // 4/4 indel rcalibration
        execute(java7, "-jar", gatk,
                "-T", "ApplyRecalibration",
                "-R", genome, "-input", snpTemp.getAbsolutePath(),
                "-tranchesFile", indelTranches.getAbsolutePath(),
                "-recalFile", indelRecal.getAbsolutePath(),
                "-o", output,
                "--ts_filter_level", "90.0", "-mode", "INDEL");
        FileFilter ff = (File pathname) -> pathname.getName().startsWith(timestamp);
        updateMessage("Finishing...");
        updateProgress(3.8, 4);
        for (File subfile : new File(temp).listFiles(ff)) {
            subfile.delete();
        }
        return ret;
    }

}
