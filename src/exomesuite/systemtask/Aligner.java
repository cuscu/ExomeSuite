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

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Aligner extends SystemTask {

    String temp, forward, reverse, genome, dbsnp, mills, phase1, output, name;
    boolean illumina;
    final int cores;
    final String java7 = OS.scanJava7();
    private final File gatk = new File("software"
            + File.separator + "gatk"
            + File.separator + "GenomeAnalysisTK.jar");

//    public Aligner() {
//        cores = Runtime.getRuntime().availableProcessors();
//    }
    public Aligner(String temp, String forward, String reverse, String genome, String dbsnp,
            String mills, String phase1, String output, String name, boolean illumina) {
        this.temp = temp;
        this.forward = forward;
        this.reverse = reverse;
        this.genome = genome;
        this.dbsnp = dbsnp;
        this.mills = mills;
        this.phase1 = phase1;
        this.output = output;
        this.name = name;
        this.illumina = illumina;
        this.cores = Runtime.getRuntime().availableProcessors();

    }

//    @Override
//    public boolean configure(Config mainConfig, Config projectConfig) {
    @Override
    protected Integer call() throws Exception {
        System.out.println("Alingment parameters");
        System.out.println("temp=" + temp);
        System.out.println("forward=" + forward);
        System.out.println("reverse=" + reverse);
        System.out.println("genome=" + genome);
        System.out.println("dbsnp=" + dbsnp);
        System.out.println("mills=" + mills);
        System.out.println("phase1=" + phase1);
        System.out.println("output=" + output);
        System.out.println("illumina=" + illumina);
        updateTitle("Aligning " + new File(output).getName());
//        for (int i = 0; i < 10; i++) {
//            updateProgress(i, 10);
//            updateMessage(i + "/" + 10);
//            Thread.sleep(1000);
//        }
//        updateProgress(1, 1);
//        Thread.sleep(1000);
        String msg = "";
        if (!tripleCheck(dbsnp)) {
            msg += "dbSNP\n";
        }
        if (mills == null || mills.isEmpty()) {
            msg += "Mills database\n";
        }
        if (phase1 == null || phase1.isEmpty()) {
            msg += "1000 genomes pahse 1 indels database\n";
        }
        if (genome == null || genome.isEmpty()) {
            msg += "Reference genome\n";
        }
        if (forward == null || forward.isEmpty()) {
            msg += "Forward sequences";
        }
        if (reverse == null || reverse.isEmpty()) {
            msg += "Reverse sequences";
        }
        if (temp == null || temp.isEmpty()) {
            msg += "Temporary folder";
        }
        if (!msg.isEmpty()) {
            System.err.println("There is one or more arguments not specifierd:\n" + msg);
            return 1;
        }
        int ret;
        if ((ret = firstAlignment()) != 0) {
            return ret;
        }
        if ((ret = refineBAM()) != 0) {
            return ret;
        }
        if ((ret = realignBAM()) != 0) {
            return ret;
        }
        if ((ret = recalibrateBAM()) != 0) {
            return ret;
        }
        updateMessage("Done");
        updateProgress(1, 1);
        return ret;

    }

    /**
     * Phase A: Align/Map sequences. Burrows-Wheeler Aligner. As this project works with paired end
     * sequences, we use simple, basic-parameters workflow.
     * <p>
     * 1 and 2: Align both sequences.</p>
     * <p>
     * bwa aln -t 4 genome.fasta sequence1.fq.gz -I -f seq1.sai</p>
     * <p>
     * bwa aln -t 4 genome.fasta sequence2.fq.gz -I -f seq2.sai</p>
     * <p>
     * -I : if the sequence is Illumina 1.3+ encoding</p>
     * -t 4 : number of threads The reference genome must be indexed 3: Generate alignments. bwa
     * sampe genome.fasta seq1.sai seq2.sai sequence1.fq.gz sequence2.fq.gz -f bwa.sam
     */
    private int firstAlignment() {
        String seq1 = new File(temp, name + "_seq1.sai").getAbsolutePath();
        String seq2 = new File(temp, name + "_seq2.sai").getAbsolutePath();
        String bwa = new File(temp, name + "_bwa.sam").getAbsolutePath();
        int ret;
        updateMessage(new File(forward).getName() + "...");
        updateProgress(2, 100);
        if ((ret = execute("bwa", "aln", "-t", String.valueOf(cores), (illumina ? "-I" : ""),
                genome, forward, "-f", seq1)) != 0) {
            return ret;
        }
        updateMessage(new File(reverse).getName() + "...");
        updateProgress(12, 100);
        if ((ret = execute("bwa", "aln", "-t", String.valueOf(cores), (illumina ? "-I" : ""),
                genome, reverse, "-f", seq2)) != 0) {
            return ret;
        }
        updateMessage("Matching pairs...");
        updateProgress(20, 100);
        if ((ret = execute("bwa", "sampe", "-P", "-f", bwa, genome, seq1, seq2, forward, reverse))
                != 0) {
            return ret;
        }
        new File(seq1).delete();
        new File(seq2).delete();
        return 0;
    }

    /*
     * Phase B: Prepare BAM for GATK
     *   SAM file from BWA must pass several filters before entering GATK.
     *
     * 4: Clean SAM
     * Perform two fix-ups
     *   Soft-clip an alignment that hangs off the end of its reference sequence
     *   Set MAPQ to 0 if a read is unmapped
     *
     * 5: Sort Sam
     *   SortOrder: coordinate
     *
     * 6: Remove Duplicated Reads
     *
     * 7: Fix RG Header
     *  RGPL = "Illumina"
     *  RGSM = "niv"
     *  RGPU = "flowcell-barcode.lane"
     *  RGLB = "BAITS"
     *
     * 8: BAM Index
     *   Generates an Index of the BAM file (.bai)
     */
    private int refineBAM() {
        String bwa = new File(temp, name + "_bwa.sam").getAbsolutePath();
        String picard = "software" + File.separator + "picard" + File.separator;
        String picard1 = new File(temp, name + "_picard1.bam").getAbsolutePath();
        String picard2 = new File(temp, name + "_picard2.bam").getAbsolutePath();
        String picard3 = new File(temp, name + "_picard3.bam").getAbsolutePath();
        String picard4 = new File(temp, name + "_picard4.bam").getAbsolutePath();
        String metrics = new File(temp, name + "_dedup.metrics").getAbsolutePath();
        int ret;
        updateMessage("Cleaning...");
        updateProgress(30, 100);
        if ((ret = execute("java", "-jar", picard + "CleanSam.jar",
                "INPUT=" + bwa, "OUTPUT=" + picard1)) != 0) {
            return ret;
        }
        new File(bwa).delete();
        updateMessage("Sorting...");
        updateProgress(35, 100);
        if ((ret = execute("java", "-jar", picard + "SortSam.jar",
                "INPUT=" + picard1,
                "OUTPUT=" + picard2,
                "SORT_ORDER=coordinate")) != 0) {
            return ret;
        }
        new File(picard1).delete();
        updateMessage("Deleting duplicates...");
        updateProgress(40, 100);
        if ((ret = execute("java", "-jar", picard + "MarkDuplicates.jar",
                "INPUT=" + picard2,
                "OUTPUT=" + picard3,
                "REMOVE_DUPLICATES=true",
                "METRICS_FILE=" + metrics)) != 0) {
            return ret;
        }
        new File(picard2).delete();
        new File(metrics).delete();
        updateMessage("Repairing headers...");
        updateProgress(45, 100);
        if ((ret = execute("java", "-jar", picard + "AddOrReplaceReadGroups.jar",
                "INPUT=" + picard3,
                "OUTPUT=" + picard4,
                "RGPL=ILLUMINA",
                "RGSM=" + name,
                "RGPU=flowcell-barcode.lane",
                "RGLB=BAITS")) != 0) {
            return ret;
        }
        new File(picard3).delete();
        updateMessage("Creating index...");
        updateProgress(50, 100);
        if ((ret = execute("java", "-jar", picard + "BuildBamIndex.jar", "INPUT=" + picard4)) != 0) {
            return ret;
        }
        return 0;
    }

    /*
     * Phase C: Realign around Indels
     *   GATK has an algorithm to avoid false positives which consists in looking at high
     *   probably Indel areas and realigning them, so no false SNPs appear.
     *
     * 9: RealignerTargetCreator
     *   Generates the intervals to reealign at. Known indels are taken from two databases:
     *    Mills and 1000 Genome Gold standard Indels
     *    1000 Genomes Phase 1 Indels
     *
     * 10: IndelRealigner
     *    Makes the realigment
     */
    private int realignBAM() {
        String picard4 = new File(temp, name + "_picard4.bam").getAbsolutePath();
        String intervals = new File(temp, name + "_gatk.intervals").getAbsolutePath();
        String gatk1 = new File(temp, name + "_gatk1.bam").getAbsolutePath();
        updateMessage("Prealigning...");
        updateProgress(60, 100);
        int ret;
        if ((ret = execute(java7, "-jar", gatk.getAbsolutePath(),
                "-T", "RealignerTargetCreator",
                "-R", genome, "-I", picard4,
                "-known", mills, "-known", phase1,
                "-o", intervals)) != 0) {
            return ret;
        }
        updateMessage("Aligning...");
        updateProgress(70, 100);
        if ((ret = execute(java7, "-jar", gatk.getAbsolutePath(),
                "-T", "IndelRealigner",
                "-R", genome, "-I", picard4,
                "-known", mills, "-known", phase1,
                "-targetIntervals", intervals,
                "-o", gatk1)) != 0) {
            return ret;
        }
        new File(picard4).delete();
        new File(picard4.replace(".bam", ".bai")).delete();
        new File(intervals).delete();

        return 0;
    }

    /*
     * Phase D: Base Quality Score Recalibration
     *   GATK uses Quality Scores to generate a calibrated error model and apply it to alignments
     *
     * 11: BaseRecalibrator
     *   Builds the error model. As reference, these databases:
     *    Mills and 100 Genome Gold standard Indels
     *    1000 Genomes Phase 1 Indels
     *    dbSNP
     *
     * 12: PrintReads
     *   Applies the recalibration
     */
    private int recalibrateBAM() {
        String gatk1 = new File(temp, name + "_gatk1.bam").getAbsolutePath();
        String recal = new File(temp, name + "_recal.grp").getAbsolutePath();
        updateProgress(80, 100);
        updateMessage("Pre-recalibrating...");
        int ret;
        if ((ret = execute(java7, "-jar", gatk.getAbsolutePath(),
                "-T", "BaseRecalibrator",
                "-I", gatk1,
                "-R", genome,
                "--knownSites", dbsnp,
                "--knownSites", mills,
                "--knownSites", phase1,
                "-o", recal)) != 0) {
            return ret;
        }

        updateProgress(90, 100);
        updateMessage("Recalibrating...");
        if ((ret = execute(java7, "-jar", gatk.getAbsolutePath(),
                "-T", "PrintReads",
                "-R", genome,
                "-I", gatk1,
                "-BQSR", recal,
                "-o", output)) != 0) {
            return ret;
        }
        new File(gatk1).delete();
        new File(gatk1.replace(".bam", ".bai")).delete();
        new File(recal).delete();

        return 0;
    }

}
