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
import exomesuite.utils.OS;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Calculates the poor regions of a bam file.
 *
 * @author Pascual Lorente Arencibia
 */
public class Mist1 extends SystemTask {

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

    private final int[] chromosomeLength = {249250621, 243199373, 198022430, 191154276, 180915260,
        171115067, 159138663, 146364022, 141213431, 135534747, 135006516, 133851895, 115169878,
        107349540, 102531392, 90354753, 81195210, 78077248, 59128983, 63025520, 48129895, 51304566,
        155270560, 59373566, 16569};
    private final long genomeLength = 3095693981L;

    private long startTime;

    /**
     * Parameters are not checked inside MIST, please, be sure all of them are legal.
     *
     * @param input the input BAM
     * @param output the output MIST
     * @param ensembl the ensembl database
     * @param threshold the DP threshold
     * @param length the minimum length
     */
    public Mist1(String input, String output, String ensembl, int threshold, int length) {
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
     */
    @Override
    protected Integer call() throws Exception {
        println("MIST called with params:");
        println("Input BAM = " + input.getAbsolutePath());
        println("Threshold = " + threshold);
        println("Length    = " + length);
        println("Output    = " + output.getAbsolutePath());
        /*
         * 1: Write headers.
         * 2: Read Ensembl exon list.
         * 3: For each exon execute samtools mpileup.
         * 4: Locate mist regions.
         * 5: Save mist regions.
         */
//        method1();
        /*
         * 1: write headers
         * 2: samtools mpileup the whole bam (cross finger no memory explosion)
         * 3: for each exon locate conftbly the region in the map
         * 4: Locate mist regions.
         * 5: save mist regions
         */
        method2();
        updateProgress(1, 1);
        updateMessage("Succesful");
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
            Logger.getLogger(Mist1.class.getName()).log(Level.SEVERE, null, ex);
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
     * Processes the exon. Executes a samtools mpileup for the region (+-10) of the exon, and
     * locates mist regions.
     *
     * @param exon
     */
    private int findMistRegions(String[] exon) {
        final String chromosome = exon[EXON_CHR];
        final int start = Integer.valueOf(exon[EXON_START]);
        final int end = Integer.valueOf(exon[EXON_END]);
        AtomicInteger matches = new AtomicInteger();
        // 3: For each exon execute samtools mpileup and fill a Map
        ProcessBuilder pb = new ProcessBuilder("samtools", "mpileup", "-r",
                chromosome + ":" + (start - WINDOW_SIZE) + "-" + (end + WINDOW_SIZE),
                input.getAbsolutePath());
        TreeMap<Integer, Integer> depths = new TreeMap();
        try {
            process = pb.start();
            try (BufferedReader command
                    = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader error
                    = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                command.lines().parallel().forEachOrdered(pileup -> {
                    final String[] pileupFields = pileup.split("\t");
                    Integer pos = Integer.valueOf(pileupFields[1]);
                    Integer depth = Integer.valueOf(pileupFields[3]);
                    depths.put(pos, depth);
                });
                error.lines().forEach(err -> {
                });
                // Once we have computed the dp of the regions, let's process mist regions
                matches.addAndGet(computeMistAreas(exon, depths));
            } catch (IOException ex) {
                Logger.getLogger(Mist1.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Mist1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return matches.get();
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
        depths.forEach((Integer position, Integer depth) -> {
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
        int index = OS.getStandardChromosomes().indexOf(chr);
        int gpos = pos;
        for (int j = 0; j < index - 1; j++) {
            gpos += chromosomeLength[j];
        }
        double percentage = gpos * 100.0 / genomeLength;
        long time = System.currentTimeMillis() - startTime;
        long remaining = genomeLength * time / gpos - time;
        String elapsed = humanReadableTime(time);
        String rem = humanReadableTime(remaining);
        updateMessage(String.format("%s (%s:%,d) %d matches (%.2f%%, %s)", elapsed, chr, pos,
                matches, percentage, rem));
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

    private void method1() {
        // 1: Write headers
        writeHeader(output);
        updateTitle("Finding MIST " + input.getName());
        updateProgress(1, 3);
        startTime = System.currentTimeMillis();
        // 2: Read Ensembl exon list
        AtomicInteger iterations = new AtomicInteger();
        AtomicInteger matches = new AtomicInteger();
        try (BufferedReader reader = new BufferedReader(new FileReader(ensembl))) {
            // Skip header line
            reader.readLine();
            reader.lines().parallel().forEachOrdered(line -> {
                //iterations.incrementAndGet();
                /* The I do everything method */
                String[] exon = line.split("\t");
                matches.addAndGet(findMistRegions(exon));
                if (iterations.incrementAndGet() % 100 == 0) {
                    String chr = exon[0];
                    int pos = Integer.valueOf(exon[1]);
                    calculateProgress(chr, pos, matches.get());

                }
            });
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
     * 1: write headers
     * 2: samtools mpileup the whole bam (cross finger no memory explosion)
     * 3: for each exon locate conftbly the region in the map
     * 4: Locate mist regions.
     * 5: save mist regions
     */
    private void method2() {
        // 1: write headers
        writeHeader(output);
        updateTitle("Finding MIST " + input.getName());
        updateProgress(1, 3);
        startTime = System.currentTimeMillis();
        AtomicReference<String> currentChromosome = new AtomicReference<>("0");
        AtomicReference<int[]> depths = new AtomicReference<>();
        AtomicInteger matches = new AtomicInteger();
        try (BufferedReader reader = new BufferedReader(new FileReader(ensembl))) {
            reader.readLine();
            reader.lines().forEach(line -> {
                String[] exon = line.split("\t");
                String chr = exon[0];
                // Call next chromosome
                if (!currentChromosome.get().equals(chr)) {
//                    calculateProgress(chr, Integer.valueOf(exon[1]), matches.get());
                    depths.set(readBamFile(chr, matches.get()));
                    currentChromosome.set(chr);
                }
                // If chromosome is not standard we can not do anything by now
                if (depths.get() == null) {
                    return;
                }
                // Set the window size [start - WS, end + WS]
                int start = Integer.valueOf(exon[1]);
                int end = Integer.valueOf(exon[2]);
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
                TreeMap<Integer, Integer> dp = new TreeMap<>();
                for (int i = windowStart; i <= windowEnd && i < depths.get().length; i++) {
                    dp.put(i, depths.get()[i]);
                }
                // Call next step
                matches.addAndGet(computeMistAreas(exon, dp));

            });
        } catch (Exception e) {
            MainViewController.printException(e);
        }
//        for (int i = 0; i < OS.getStandardChromosomes().size(); i++) {
//
//            String chr = OS.getStandardChromosomes().get(i);
//            int[] depths = readBamFile(chr);
//
//            calculateProgress(chr, 1, 1);
//        }

    }

    /**
     * Creates a int[] with the length of chr + 1, using 1-based coordinates, son ret[0] is empty.
     *
     * @param chr
     * @return
     */
    private int[] readBamFile(String chr, int matches) {
        int le = -1;
        for (int i = 0; i < OS.getStandardChromosomes().size(); i++) {
            if (chr.equals(OS.getStandardChromosomes().get(i))) {
                le = chromosomeLength[i];
            }
        }
        if (le == -1) {
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
                Logger.getLogger(Mist1.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Mist1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return depths;
    }

}
