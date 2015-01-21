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
package exomesuite.actions.index;

import exomesuite.actions.SystemTask;
import java.io.File;

/**
 * This class will create the proper indexes for bwa, samtools and picard in the same directory
 * where the genome is placed.
 *
 * @author Pascual Lorente Arencibia
 */
public class Indexer extends SystemTask {

    private final static String[] extensions = {".pac", ".sa", ".amb", ".ann", ".bwt", ".fai"};

    /**
     * The reference genome
     */
    private final String genome;

    /**
     * Creates a new Indexer with the desired genome.
     *
     * @param genome the genome to index
     */
    public Indexer(String genome) {
        this.genome = genome;
    }

    @Override
    protected Integer call() throws Exception {
        updateMessage("Generating BWA index...");
        updateProgress(5, 100);
        int ret;
        // Only perform bwa index if needed
        boolean bwaindexed = true;
        for (String extension : extensions) {
            if (!new File(genome + extension).exists()) {
                bwaindexed = false;
                break;
            }
        }
        if (!bwaindexed) {
            ret = execute("bwa", "index", "-a", "bwtsw", genome);
            if (ret != 0) {
                return ret;
            }
        }

        updateMessage("Generating samtools index...");
        updateProgress(60, 100);
        if ((ret = execute("samtools", "faidx", genome)) != 0) {
            return ret;
        }
        updateMessage("Generating Picard index...");
        updateProgress(80, 100);
        final String createDictionary = "software" + File.separator + "picard" + File.separator
                + "CreateSequenceDictionary.jar";
        if ((ret = execute("java", "-jar", createDictionary, "R=" + genome,
                "O=" + genome.replace(".fasta", ".fa").replace(".fa", ".dict"))) != 0) {
            return ret;
        }
        updateMessage("Done");
        updateProgress(1, 1);
        return ret;
    }

    /**
     * Checks if a file is already indexed. For a file to be indexed in the same path it may
     * contaion the following files.
     * <p>
     * file.fasta, file.dict, file.pac, file.sa, file.amb, file.ann, file.bwt, file.fai
     *
     * @param file the file to check
     * @return true if there are all of these files
     */
    public static boolean isIndexed(File file) {
        // Check for GATK index
        boolean isIndexed = new File(file.getAbsolutePath().replace(".fasta", ".fa").replace(".fa", ".dict")).exists();
        if (isIndexed) {
            for (String extension : extensions) {
                if (!new File(file.getAbsolutePath() + extension).exists()) {
                    isIndexed = false;
                    break;
                }
            }
        }
        return isIndexed;
    }
}
