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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Mist extends SystemTask {

    private File input, output, ensembl;
    private int threshold;
    private final static int WINDOW_SIZE = 10;
    private final static String INSIDE = "inside";
    private final static String OVERLAP = "overlap";
    private final static String LEFT = "left";
    private final static String RIGHT = "right";

    /*
     DISCUSSION:
     This could be seen a little bit weird. But, when running, there are some problems when
     accessing tsv columns.
     First solution is to use int values (chr = row[0]; pos = row[1]). But what if the files columns
     order changes? We have to programatically change them. But, what if two different files have
     different columns order?. We want to determine the position of the columns on the run.
     Second try was to create a List<String> with the column headers. When we want to access to
     a column, simply call list.indexOf("chrom") or list.indexOf("exon_start"). Good, it works.
     But indexOf is a loop, so imagine a file with thousans of lines, it implies thousands of
     miniLoops, thousands of times making the same questions with the same answers.
     Third solution, never implemented, but thought, was to create as many int constants as headers
     and calculate them programatically at the begining, but this only work if all files have the
     same header.
     Current solution is a Map taht pairs an Enum value of Header with an int. There is a specific
     Map for it, the EnumMap (This representation is extremely compact and efficient, prays the
     documentation). To programatically calculate the order, we use another Map, taht pairs a Header
     with its String representation.
     To sum up. I hope EnumMap.indexOf(Header) to be faster than StringMap.indexOf(String). This is
     the aspect of getting a value: row[columns.get(Header.EXON_START)], where row is the line and
     columns the EnumSet.
     */
    /*
     LAST UPDATE: forget everything, int static are the best non-headache solution.
     */
    private static int EXON_CHR;
    private static int EXON_START;
    private static int EXON_END;
    private static int GENE_ID;
    private static int GENE_NAME;
    private static int EXON_N;
    private static int EXON_ID;
    private static int TRANS_NAME;
    private static int TRANS_INFO;
    private static int GENE_BIO;

    private static final int OUT_CHR = 0;
    private static final int OUT_EXON_START = 1;
    private static final int OUT_EXON_END = 2;
    private static final int OUT_POOR_START = 3;
    private static final int OUT_POOR_END = 4;
    private static final int OUT_GENE_ID = 5;
    private static final int OUT_GENE_NAME = 6;
    private static final int OUT_EXON_NUMBER = 7;
    private static final int OUT_EXON_ID = 8;
    private static final int OUT_TR_NAME = 9;
    private static final int OUT_TR_INFO = 10;
    private static final int OUT_GENE_BIO = 11;
    private static final int OUT_MATCH = 12;

    final String[] headers = {"chrom", "exon_start", "exon_end", "poor_start", "poor_end",
        "gene_id", "gene_name", "exon_number", "exon_id", "transcript_name", "transcript_info",
        "gene_biotype", "match"};

    private static String chromosome;
    private static int[] depths;
    static boolean go = false;

    /*
     * IMPORTANT NOTE FOR DEVELOPERS. Genomic positions start at 1, Java array positions start at 0.
     * To avoid confusions, all Java arrays will have length incremented in 1, and I won't use
     * position 0 in them. So any time there is an array access (depths[i]) it is accessing
     * to genomic position.
     */
    @Override
    protected Integer call() throws Exception {
        AtomicInteger iterations = new AtomicInteger(0);
        // Get contigs and their lengths from SAM header.
        Map<String, Integer> lengths = getChroms(input);
        writeHeader(output);
        // This should be the first iteration, but, to avoid unnecesary comparations, it's outside.
        // It is possible, improbable but possible, that chromosome 1 is not in the SAM.
        chromosome = "1";
        depths = new int[lengths.get(chromosome) + 1];
        String message = String.format("Chromosome %s (%d/%d)", chromosome,
                iterations.incrementAndGet(), lengths.size());
        updateMessage(message);
        updateProgress(iterations.get(), lengths.size());
        // qname | flag | chr | pos | mapq | cigar | rnext | pnext | tlen | seq | qual
        // chr: 2
        // pos: 3
        // len: 9.length
        try (BufferedReader in = executeAndRead("samtools", "view", input.getAbsolutePath())) {
            in.lines().forEach((String line) -> {
                // When reached *, flush all the lines. These rows are not aligned.
                if (chromosome.equals("*")) {
                    return;
                }
                final String[] row = line.split("\t");
                // Look for changes in the chromosome.
                if (!row[2].equals(chromosome)) {
                    // Chromosome change
                    storeChrom(chromosome, depths, output);
                    chromosome = row[2];
                    if (chromosome.equals("*")) {
                        final String msg = "Finishing...";
                        updateMessage(msg);
                        return;
                    }
                    depths = new int[lengths.get(row[2]) + 1];
                    final String msg = String.format("Chromosome %s (%d/%d)", chromosome,
                            iterations.incrementAndGet(), lengths.size());
                    updateMessage(msg);
                    updateProgress(iterations.get(), lengths.size() + 1);
                }
                final int start = Integer.valueOf(row[3]);
                final int length = row[9].length();
                try {
                    // Using genomic positions. Read NOTE at beginning.
                    for (int i = start; i <= start + length; i++) {
                        depths[i]++;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    println("Some sequences fall out of chromosome."
                            + chromosome + ":" + start + "(chr length:" + lengths.get(row[2]) + ")");
                } catch (Exception ex) {
                    Logger.getLogger(Mist.class.getName()).log(Level.SEVERE, null, ex);
                }

            });
            println("Done");
            // If there are mot misaligned lines, the algorithm reaches the end of the file with a
            // pending chromosome.
            if (chromosome != null && !chromosome.equals("*")) {
                storeChrom(chromosome, depths, output);
            }
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(Mist.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Executes a command and returns the output as a BufferedReader.
     *
     * @param input A sam/bam file.
     * @return a BufferedReader that can be read Line by line.
     */
    private BufferedReader executeAndRead(String... args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        return new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    /**
     * Reads input bam headers a returns a Map with pairs contig-length.
     *
     * @param input the sam or bam file.
     * @return a Map with an entry for each chromosome. Chromosome name as key; chromosome length as
     * value.
     */
    private Map<String, Integer> getChroms(File input) {
        // samtools view -H input.bam
        // @SQ	SN:1	LN:249250621
        // @SQ	SN:GL000249.1	LN:38502
        Map<String, Integer> chroms = new TreeMap<>();
        try (BufferedReader in = executeAndRead("samtools", "view", "-H", input.getAbsolutePath())) {
            in.lines().forEach((String line) -> {
                String[] row = line.split("\t");
                if (row[0].startsWith("@SQ")) {
                    final String chr = row[1].substring(3);
                    final int length = Integer.valueOf(row[2].substring(3));
                    chroms.put(chr, length);
                }
            });
        } catch (IOException ex) {
            println("Error reading " + input + " lengths.");
            Logger.getLogger(Mist.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return chroms;
    }

    /**
     * Calculates poor regions and insert any exon matching into the output file.
     *
     * @param chr
     * @param depths
     * @param output
     */
    private void storeChrom(String chr, int[] depths, File output) {
        try (BufferedReader in = new BufferedReader(new FileReader(ensembl))) {
            //Header out.
            fillEnsemblIndexes(in.readLine());
            AtomicInteger c = new AtomicInteger(0);
            AtomicInteger e = new AtomicInteger(0);
            AtomicInteger et = new AtomicInteger(0);
            boolean good;
            int poor_start, poor_end;
            String line;
            while ((line = in.readLine()) != null) {
                String[] exon = line.split("\t");
                if (exon[EXON_CHR].equals(chr)) {
                    et.incrementAndGet();
                    final int start = Integer.valueOf(exon[EXON_START]) - WINDOW_SIZE;
                    int end = Integer.valueOf(exon[EXON_END]) + WINDOW_SIZE;
                    if (end >= depths.length) {
                        end = depths.length - 1;
                    }
                    int i = start;
                    good = true;
                    while (i <= end) {
                        if (depths[i] < threshold) {
                            good = false;
                            // Put start and end.
                            poor_start = i++;
                            try {
                                while (depths[i] < threshold && i <= end) {
                                    i++;
                                }
                            } catch (ArrayIndexOutOfBoundsException ex) {
                                println(String.format(
                                        "Some exons fall out of the chromosome %s (length=%d) %s",
                                        chr, depths.length, line));
                            }
                            poor_end = i - 1;
                            String[] outLine = new String[headers.length];
                            outLine[OUT_CHR] = exon[EXON_CHR];
                            outLine[OUT_EXON_START] = exon[EXON_START];
                            outLine[OUT_EXON_END] = exon[EXON_END];
                            outLine[OUT_POOR_START] = poor_start + "";
                            outLine[OUT_POOR_END] = poor_end + "";
                            outLine[OUT_EXON_ID] = exon[EXON_ID];
                            outLine[OUT_EXON_NUMBER] = exon[EXON_N];
                            outLine[OUT_GENE_ID] = exon[GENE_ID];
                            outLine[OUT_GENE_NAME] = exon[GENE_NAME];
                            outLine[OUT_GENE_BIO] = exon[GENE_BIO];
                            outLine[OUT_TR_INFO] = exon[TRANS_INFO];
                            outLine[OUT_TR_NAME] = exon[TRANS_NAME];
                            boolean isLow = poor_start <= start + WINDOW_SIZE;
                            boolean isHigh = poor_end >= end - WINDOW_SIZE;
                            if (isHigh) {
                                outLine[OUT_MATCH] = isLow ? OVERLAP : RIGHT;
                            } else {
                                outLine[OUT_MATCH] = isLow ? LEFT : INSIDE;
                            }
                            writeLine(output, outLine);
                            c.incrementAndGet();
                        } else {
                            i++;
                        }
                    }
                    if (!good) {
                        e.incrementAndGet();
                    }
                }
            }
            println(String.format("%d matches in %d/%d exons.", c.get(), e.get(), et.get()));
//            println(c.get() + " matches in " + e.get() + "/" + et.get() + " exons.");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mist.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Mist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Writes all the args in the file using a tab as separator and insert a newLine mark at the
     * end.
     *
     * @param output
     * @param values
     */
    private void writeLine(File output, String... values) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(output, true))) {
            int i = 0;
            while (i < values.length - 1) {
                out.write(values[i++] + "\t");
            }
            out.write(values[i]);
            out.newLine();

        } catch (IOException ex) {
            Logger.getLogger(Mist.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    // chrom | start | end | gene_id | gene_name | exon_number | transcript_id | transcript_name |
    // transcript_info | gene_biotype
    private void fillEnsemblIndexes(String line) {
        final String[] row = line.split("\t");
        for (int i = 0; i < row.length; i++) {
            switch (row[i].toLowerCase()) {
                case "chrom": EXON_CHR = i;
                    break;
                case "start": EXON_START = i;
                    break;
                case "end": EXON_END = i;
                    break;
                case "gene_id": GENE_ID = i;
                    break;
                case "gene_name": GENE_NAME = i;
                    break;
                case "exon_number": EXON_N = i;
                    break;
                case "transcript_id": EXON_ID = i;
                    break;
                case "transcript_name": TRANS_NAME = i;
                    break;
                case "transcript_info": TRANS_INFO = i;
                    break;
                case "gene_biotype": GENE_BIO = i;
                    break;
            }
        }
    }

    /**
     * Writes the first line of the output line, which contains the headers for the columns.
     *
     * @param output
     */
    private void writeHeader(File output) {
        if (output.exists()) {
            output.delete();
        }
        writeLine(output, headers);
    }

    @Override
    public boolean configure(Config mainConfig, Config projectConfig) {
//    private int threshold;
        // INPUT=path/alignments/name.bam
        input = new File(projectConfig.getProperty("align_path"),
                projectConfig.getProperty(Config.NAME) + ".bam");
        // OUTPUT=path/mist/name.mist
        output = new File(projectConfig.getProperty("mist_path"),
                projectConfig.getProperty(Config.NAME) + ".mist");
        ensembl = new File(mainConfig.getProperty(Config.ENSEMBL_EXONS));
        threshold = Integer.valueOf(projectConfig.getProperty("threshold"));
        return true;
    }

}
