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
package exomesuite.actions.mist;

import exomesuite.MainViewController;
import exomesuite.actions.SystemTask;
import exomesuite.utils.OS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculates the poor regions of a bam file.
 *
 * @author Pascual Lorente Arencibia
 */
public class Mist extends SystemTask {

    private final File input, output, ensembl;
    private final int threshold, length;
    private final static int WINDOW_SIZE = 10;
    private final static String INSIDE = "inside";
    private final static String OVERLAP = "overlap";
    private final static String LEFT = "left";
    private final static String RIGHT = "right";

    // chrom | start | end | gene_id | gene_name | exon_number | transcript_id | transcript_name |
    // transcript_info | gene_biotype
    private final static int EXON_CHR = 0;
    private final static int EXON_START = 1;
    private final static int EXON_END = 2;
    private final static int GENE_ID = 3;
    private final static int GENE_NAME = 4;
    private final static int EXON_N = 5;
    private final static int EXON_ID = 6;
    private final static int TRANS_NAME = 7;
    private final static int TRANS_INFO = 8;
    private final static int GENE_BIO = 9;

    private long genomeLength;

    private long startTime;
    private List<Chromosome> chromosomes;

    /**
     * Parameters are not checked inside MIST, please, be sure all of them are legal.
     *
     * @param input the input BAM
     * @param output the output MIST
     * @param ensembl the ensembl database
     * @param threshold the DP threshold
     * @param length the minimum length
     */
    public Mist(String input, String output, String ensembl, int threshold, int length) {
        this.input = new File(input);
        this.output = new File(output);
        this.ensembl = new File(ensembl);
        this.threshold = threshold;
        this.length = length;
    }

    final String[] headers = {"chrom", "exon_start", "exon_end", "mist_start", "mist_end",
        "gene_id", "gene_name", "exon_id", "transcript_name", "biotype", "match"};

    /*
     * IMPORTANT NOTE FOR DEVELOPERS. Genomic positions start at 1, Java array positions start at 0.
     * To avoid confusions, all Java arrays will have length incremented in 1, and I won't use
     * position 0 in them. So any time there is an array access (depths[i]) it is accessing
     * to genomic position.
     * NOTE 2: Firs implementation had a high cost: read each exon from Ensembl and call 'samtools
     * mpileup' for each. Even with parallelization, its estimated time was 2 or 3 days for a sample.
     * Current implementation piles up chromosome by chromosome by requesting.
     */
    @Override
    protected Integer call() throws Exception {
        println("MIST called with params:");
        println("Input BAM = " + input.getAbsolutePath());
        println("Threshold = " + threshold);
        println("Length    = " + length);
        println("Output    = " + output.getAbsolutePath());
        int ret = startMIST();
        updateProgress(1, 1);
        if (ret == 0) {
            updateMessage("Successful");
            return 0;
        } else {
            updateMessage("Canceled");
            return 0;
        }
    }

    /*
     * 1: write headers
     * 2: Read exons
     * 3: if exon's chr is not loaded, mpileup in memory chr
     * 4: Locate mist regions.
     * 5: save mist regions
     */
    private int startMIST() {
        updateTitle("Finding MIST " + input.getName());
        updateProgress(0, 1);
        chromosomes = readBamHeaders(input);
        // 1: write headers
        writeHeader(output);
        startTime = System.currentTimeMillis();

        // Name of chromosome loaded in memory
        AtomicReference<String> currentChromosome = new AtomicReference("0");
        // Chromosome in memory, an array of ints
        AtomicReference<int[]> depths = new AtomicReference();
        // Counter for matches
        AtomicInteger matches = new AtomicInteger();

        // Read the exons file
        try (BufferedReader reader = new BufferedReader(new FileReader(ensembl))) {
            // Skip first line
            reader.readLine();
            reader.lines().forEach(line -> {
                String[] exon = line.split("\t");
                String chr = exon[0];
                // Call next chromosome
                if (!currentChromosome.get().equals(chr)) {
                    // Load new chromosome in memory, replacing current
                    depths.set(readBamContent(chr, matches.get()));
                    // Mark chromosome as read
                    for (Chromosome c : chromosomes) {
                        if (c.name.equals(chr)) {
                            c.processed = true;
                            break;
                        }
                    }
                    currentChromosome.set(chr);
                }
                // Ensure something was loaded
                if (depths.get() == null) {
                    return;
                }
                int start = Integer.valueOf(exon[1]);
                int end = Integer.valueOf(exon[2]);
                // Set the window size [start - WS, end + WS]
                // Start can not be smaller than 1
                int windowStart = start - WINDOW_SIZE;
                if (windowStart < 1) {
                    windowStart = 1;
                }
                // End cannot be greater than chromosome
                int windowEnd = end + WINDOW_SIZE;
                if (windowEnd >= depths.get().length) {
                    windowEnd = depths.get().length - 1;
                }
                // Fill a TreeMap with <pos, dp>
                TreeMap<Integer, Integer> dp = new TreeMap();
                for (int i = windowStart; i <= windowEnd && i < depths.get().length; i++) {
                    dp.put(i, depths.get()[i]);
                }
                // Call next step
                matches.addAndGet(computeMistAreas(exon, dp));
            });
        } catch (Exception e) {
            // Dont do anything, someone canceled the stream
            //MainViewController.printException(e);
            return 1;
        }
        return 0;
    }

    /**
     * Writes all the args in the file using a tab as separator and insert a newLine mark at the
     * end.
     *
     * @param output output file
     * @param values list of values
     */
    private void writeLine(File output, String... values) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(output, true))) {
            out.write(OS.asString("\t", values));
            out.newLine();
        } catch (IOException ex) {
            Logger.getLogger(Mist.class.getName()).log(Level.SEVERE, null, ex);
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

    /**
     * Calculates the mist regions and for each one, prints a line in the output. This method
     * suposes that the depths correspond to the exon, it does not check that depths position are
     * inside the exon.
     *
     * @param exon the exon been analized
     * @param depths the depths of the exon
     */
    private int computeMistAreas(String[] exon, TreeMap<Integer, Integer> depths) {
        AtomicInteger mistStart = new AtomicInteger();
        AtomicInteger mistEnd = new AtomicInteger();
        AtomicBoolean inMist = new AtomicBoolean(false);
        AtomicInteger matches = new AtomicInteger();
        depths.forEach((position, depth) -> {
            if (depth < threshold) {
                // If the depth is under the threshold, and previously no mist region,
                // set the start of the mist region
                if (inMist.compareAndSet(false, true)) {
                    mistStart.set(position);
                }
            } else {
                // If the depth is over threshold, and a mist region was in progress
                // Set the end of the region and inform
                if (inMist.compareAndSet(true, false)) {
                    mistEnd.set(position);
                    if (printMist(exon, mistStart.get(), mistEnd.get())) {
                        matches.incrementAndGet();
                    }
                }
            }
        });
        if (inMist.get()) {
            mistEnd.set(depths.lastKey());
            if (printMist(exon, mistStart.get(), mistEnd.get())) {
                matches.incrementAndGet();
            }
        }
        return matches.get();
    }

    /**
     * Stores a MIST region only if its length is greater than the length parameter.
     *
     * @param exon the TSV exon
     * @param mistStart the start of the mist region
     * @param mistEnd the end of the mist region
     */
    private boolean printMist(String[] exon, int mistStart, int mistEnd) {
        if (mistEnd - mistStart + 1 >= length) {
            final int exonStart = Integer.valueOf(exon[EXON_START]);
            final int exonEnd = Integer.valueOf(exon[EXON_END]);
            // Determine type of match
            String match = determineMatch(exonStart, exonEnd, mistStart, mistEnd);
            // chrom, exon_start, exon_end, mist_start, mist_end, gene_id, gene_name, exon_id,
            // transcript_name, biotype, match
            writeLine(output, exon[EXON_CHR], exon[EXON_START], exon[EXON_END], mistStart + "",
                    mistEnd + "", exon[GENE_ID], exon[GENE_NAME], exon[EXON_ID], exon[TRANS_NAME],
                    exon[GENE_BIO], match);
            return true;
        }
        return false;
    }

    /**
     * Given an exon coordinates and a MIST region coordinates determines if the MIST region if
     * left, right, inside or overlapping the exon.
     *
     * @param exonStart start of the exon
     * @param exonEnd end of the exon
     * @param mistStart start of the mist region
     * @param mistEnd end of the mist region
     * @return left, rigth, inside or overlap
     */
    private String determineMatch(int exonStart, int exonEnd, int mistStart, int mistEnd) {
        if (mistStart < exonStart) {
            if (mistEnd > exonEnd) {
                return OVERLAP;
            } else {
                return LEFT;
            }
        } else {
            if (mistEnd > exonEnd) {
                return RIGHT;
            } else {
                return INSIDE;
            }
        }
    }

    private void calculateProgress(String chr, int pos, int matches) {
        AtomicLong gpos = new AtomicLong(pos);
        chromosomes.forEach(c -> {
            if (c.processed) {
                gpos.addAndGet(c.length);
            }
        });
        double percentage = gpos.get() * 100.0 / genomeLength;
        long time = System.currentTimeMillis() - startTime;
        long remaining = genomeLength * time / gpos.get() - time;
        String elapsed = humanReadableTime(time);
        String rem = humanReadableTime(remaining);
        updateMessage(String.format("%s (%s:%,d) %d matches (%s)", elapsed, chr, pos, matches, rem));
//        println(String.format("%.2f\t%s:%,d\t%s", percentage, chr, pos, matches, elapsed, rem));
        updateProgress(percentage, 100.0);
    }

    private String humanReadableTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        String ret = "";
        if (days > 0) {
            ret += days + " d ";
        }
        ret += String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return ret;
    }

    /**
     * Creates a int[] with the length of chr + 1, using 1-based coordinates, son ret[0] is empty.
     *
     * @param chr
     * @return
     */
    private int[] readBamContent(String chr, int matches) {
        int le = -1;
        for (Chromosome c : chromosomes) {
            if (c.name.equals(chr)) {
                le = c.length;
                break;
            }
        }
        if (le == -1) {
            println("Chromosome " + chr + " is not in BAM header. Impossible to process.");
            return null;
        }
        int[] depths = new int[le + 1];
        ProcessBuilder pb = new ProcessBuilder("samtools", "mpileup", "-r", chr,
                input.getAbsolutePath());
        AtomicInteger iterations = new AtomicInteger();
        try {
            process = pb.start();
            try (BufferedReader command
                    = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                command.lines().parallel().forEachOrdered(pileup -> {
                    final String[] pileupFields = pileup.split("\t");
                    Integer pos = Integer.valueOf(pileupFields[1]);
                    Integer depth = Integer.valueOf(pileupFields[3]);
                    depths[pos] = depth;
                    if (iterations.incrementAndGet() % 1000000 == 0) {
                        calculateProgress(chr, pos, matches);
                    }
                });
            } catch (IOException ex) {
                MainViewController.printException(ex);
            }
        } catch (IOException ex) {
            MainViewController.printException(ex);
        }
        return depths;
    }

    /**
     * Reads input bam headers a returns a Map with pairs contig-length.
     *
     * @param input the sam or bam file.
     * @return a Map with an entry for each chromosome. Chromosome name as key; chromosome length as
     * value.
     */
    private List<Chromosome> readBamHeaders(File input) {
        // samtools view -H input.bam
        // @SQ	SN:1	LN:249250621
        // @SQ	SN:GL000249.1	LN:38502
        List<Chromosome> chroms = new ArrayList();
        ProcessBuilder pb = new ProcessBuilder("samtools", "view", "-H", input.getAbsolutePath());
        try {
            process = pb.start();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                in.lines().forEach(line -> {
                    String[] row = line.split("\t");
                    if (row[0].startsWith("@SQ")) {
                        final String chr = row[1].substring(3);
                        final int size = Integer.valueOf(row[2].substring(3));
                        chroms.add(new Chromosome(chr, size));
                    }
                });
            } catch (IOException ex) {
                MainViewController.printException(ex);
            }
        } catch (IOException ex) {
            MainViewController.printException(ex);
        }
        chroms.stream().forEach(chr -> genomeLength += chr.length);
        return chroms;
    }

    /**
     * Tiny class to store together a chrom with its length and an already processed flag. This is
     * only used for progress purpose.
     */
    private class Chromosome {

        String name;
        int length;
        boolean processed = false;

        public Chromosome(String name, int length) {
            this.name = name;
            this.length = length;
        }

    }
}
