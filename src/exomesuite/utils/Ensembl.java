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
package exomesuite.utils;

import exomesuite.MainViewController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class Ensembl {

    private static File file;
    private static ObservableMap<String, ObservableList<Exon>> transcripts;

    /**
     * Per chromosome map list of transcripts.
     *
     * @return the trasncripts.
     */
    public static ObservableMap<String, ObservableList<Exon>> getTranscripts() {
        return transcripts;
    }

    private static void initialize() {
        Task task;
        task = new Task() {

            @Override
            protected Object call() throws Exception {
                System.out.println("Reading exons");
                transcripts = FXCollections.observableHashMap();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    // Skip header
                    br.readLine();
                    br.lines().forEach(line -> {
                        // Create the Exon
                        Exon exon = new Exon(line);
                        String chr = exon.getChromosome();
                        // Take the exons list of the chromosome
                        ObservableList<Exon> exons = transcripts.get(chr);
                        // If list does not exist, create it and add the only exon
                        if (exons == null) {
                            exons = FXCollections.observableArrayList();
                            exons.add(exon);
                            transcripts.put(chr, exons);
                        } else {
                            // Try to insert in a previous position
                            boolean inserted = false;
                            for (int i = 0; i < exons.size(); i++) {
                                // If there is an exons with a higher start, insert before
                                if (exons.get(i).start > exon.start) {
                                    exons.add(i, exon);
                                    inserted = true;
                                    break;
                                } else if (exons.get(i).start == exon.start) {
                                    if (exons.get(i).end > exon.end) {
                                        exons.add(i, exon);
                                        inserted = true;
                                        break;
                                    }
                                }
                            }
                            // Add to the end of the list
                            if (!inserted) {
                                exons.add(exon);
                            }
                        }
                    });
                    System.out.println("Done");
                } catch (Exception e) {
                    MainViewController.printException(e);
//                    Dialogs.create().printException(e);
                }
                return null;
            }

        };
        new Thread(task).start();
    }

    /**
     * Changes the ensmbl file.
     *
     * @param file the new Ensembl file
     */
    public static void setFile(File file) {
        if (!file.equals(Ensembl.file)) {
            Ensembl.file = file;
            initialize();
        }
    }

    /**
     * Keeps info about an exon line.
     */
    public static class Exon {

        private int start;
        private int end;
        private final String[] line;

        /**
         * Creates a new exons using the line. Line must be separated by tabs "\t".
         *
         * @param line the exon line
         */
        public Exon(String line) {
            this.line = line.split("\t");
            this.start = Integer.valueOf(this.line[1]);
            this.end = Integer.valueOf(this.line[2]);
        }

        /**
         * Gets the end position of the exon.
         *
         * @return the end position
         */
        public int getEnd() {
            return end;
        }

        /**
         * Gets the whole line of the Exon.
         *
         * @return the line
         */
        public String[] getLine() {
            return line;
        }

        /**
         * Gets the start position of the exon.
         *
         * @return the start position of the exon
         */
        public int getStart() {
            return start;
        }

        /**
         * Gets the chromosome of the exon.
         *
         * @return the chromosome
         */
        public String getChromosome() {
            return line[0];
        }
    }

}
