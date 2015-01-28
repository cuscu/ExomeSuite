/*
 * Copyright (C) 2015 UICHUIMI
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
package exomesuite.bam;

import exomesuite.MainViewController;
import exomesuite.graphic.SizableImage;
import exomesuite.utils.OS;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class BamReader {

    @FXML
    private BamCanvas bamCanvas;
    @FXML
    private Label info;
    @FXML
    private ComboBox<String> chromosome;
    @FXML
    private TextField position;
    @FXML
    private Slider zoom;
    @FXML
    private CheckMenuItem bckgColor;
    @FXML
    private CheckMenuItem barsColor;
    @FXML
    private CheckMenuItem xAxis;
    @FXML
    private CheckMenuItem yAxis;
    @FXML
    private CheckMenuItem xLabels;
    @FXML
    private CheckMenuItem yLabels;
    @FXML
    private CheckMenuItem dpPercentage;
    @FXML
    private CheckMenuItem directions;
    @FXML
    private Button left;
    @FXML
    private Button right;
    /**
     * Reference genome file.
     */
    private File genome;
    /**
     * Alignments file.
     */
    private File alignments;

    private List<PileUp> windowAlignments;
    int windowStart = 0, windowEnd = 0;
    String windowChromosome = "";
    private static final int WINDOW_SIZE = 50;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        // Canvas options (force max and min size)
        initializeBamCanvas();
        initializeOptionsPanel();
        // Fill chromosomes with standard (can be replace with a samtools view -H)
        chromosome.setItems(OS.getStandardChromosomes());
        chromosome.setValue("1");
        position.setText("1");
        chromosome.setOnAction(event -> updatePosition());
        // Jump to new position
        position.setOnAction(event -> updatePosition());
        left.setOnAction(event -> {
            int value = Integer.valueOf(position.getText());
            if (value > 1) {
                position.setText((value - 1) + "");
                bamCanvas.setSelectedIndex(value - 1);
                updatePosition();
            }
        });
        right.setOnAction(event -> {
            int value = Integer.valueOf(position.getText());
            position.setText((value + 1) + "");
            bamCanvas.setSelectedIndex(value + 1);
            updatePosition();
        });
        // Avoid non digit character
        position.setOnKeyTyped(event -> {
            if (!Character.isDigit(event.getCharacter().charAt(0))) {
                event.consume();
            }
        });
        left.setGraphic(new SizableImage("exomesuite/img/left-arrow.png", SizableImage.SMALL_SIZE));
        right.setGraphic(new SizableImage("exomesuite/img/right-arrow.png", SizableImage.SMALL_SIZE));
//        zoom.setOnValueChanged(e -> bamCanvas.setBaseWidth(zoom.getValue()));
        zoom.valueProperty().bindBidirectional(bamCanvas.getBaseWidth());
        zoom.valueProperty().addListener((obs, old, current) -> updatePosition());
    }

    private void initializeBamCanvas() {
        bamCanvas.setPrefSize(9999, 9999);
        bamCanvas.setMinSize(1, 1);
        bamCanvas.widthProperty().addListener(object -> updatePosition());
        bamCanvas.getSelectedIndex().addListener((obs, old, current) -> {
            final int clickedPos = current.intValue();
            final int genomicStartPosition = bamCanvas.getGenomicPosition().get();
            if (clickedPos >= 0 && bamCanvas.getAlignments().size() > clickedPos) {
                // Count reference ·
                int emptySpaces = 0;
                for (int j = 0; j < clickedPos; j++) {
                    if (bamCanvas.getAlignments().get(j).getReference() == '*') {
                        emptySpaces++;
                    }
                }
                final int pos = current.intValue() + genomicStartPosition - emptySpaces;
                PileUp pu = bamCanvas.getAlignments().get(clickedPos);
                if (pu != null) {
                    info.setText(pos + ":" + pu);
                }
            }
        });
    }

    private void initializeOptionsPanel() {
        bckgColor.selectedProperty().bindBidirectional(bamCanvas.getShowBacgroundColor());
        barsColor.selectedProperty().bindBidirectional(bamCanvas.getBaseColors());
        directions.selectedProperty().bindBidirectional(bamCanvas.getShowAlleles());
        dpPercentage.selectedProperty().bindBidirectional(bamCanvas.getPercentageUnits());
        xAxis.selectedProperty().bindBidirectional(bamCanvas.getShowXAxis());
        yAxis.selectedProperty().bindBidirectional(bamCanvas.getShowYAxis());
        xLabels.selectedProperty().bindBidirectional(bamCanvas.getShowXLabels());
        yLabels.selectedProperty().bindBidirectional(bamCanvas.getShowYLabels());
    }

    public void setBamFile(File file) {
        this.alignments = file;
    }

    public void setReferenceFile(File file) {
        this.genome = file;
    }

    /**
     * Computes chr and position and calls changePosition if it is a valid position.
     */
    private void updatePosition() {
        final String chr = chromosome.getValue();
        if (chr != null && !chr.isEmpty()) {
            try {
                final int index = Integer.valueOf(position.getText());
                changePosition(chr, index);
            } catch (NumberFormatException ex) {
                MainViewController.printMessage("Bad position: " + position.getText(), "error");
            }
        } else {
            MainViewController.printMessage("No chromosome selected", "error");
        }
    }

    /**
     * Changes the position of the canvas to center the position chr:start.
     *
     * @param chr the chromosome to go
     * @param index the position to center
     */
    private void changePosition(String chr, int index) {
        // Lets determine the length of the graph.
        // The variant is centered on screen.
        final int elements = (int) Math.floor((bamCanvas.getWidth()
                - 2 * bamCanvas.getAxisMargin().get())
                / bamCanvas.getBaseWidth().get()) + 1;
        int start = index - elements / 2;
        int end = index + elements / 2;
        if (start < 1) {
            end += (1 - start);
            start = 1;
        }
        List<PileUp> pileups = new ArrayList();
        // So, I want chr:start-end
        // What do I have in alignmentWindow
        if (!windowChromosome.equals(chr) || start < windowStart || end > windowEnd) {
            // Determine the size of the new window
            windowStart = start - WINDOW_SIZE;
            if (windowStart < 1) {
                windowStart = 1;
            }
            windowEnd = end + WINDOW_SIZE;
            windowChromosome = chr;
            windowAlignments = readBamFile(windowChromosome, windowStart, windowEnd);
        }
        for (int i = 0; i <= end - start && i + start - windowStart < windowAlignments.size(); i++) {
            pileups.add(windowAlignments.get(i + start - windowStart));
        }
        bamCanvas.setAlignments(pileups);
        bamCanvas.setGenomicPosition(start);
//        bamCanvas.setSelectedIndex(index);
    }

    /**
     * Gets the alignment PileUps of the chromosome:start-end from the bamFile.
     *
     * @param chromosome the chromosome
     * @param start the start position
     * @param end the end position
     * @return
     */
    private List<PileUp> readBamFile(String chromosome, int start, int end) {
        final List<PileUp> pileups = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder("samtools", "mpileup", "-f", genome.getAbsolutePath(),
                "-r", chromosome + ":" + start + "-" + end, alignments.getAbsolutePath());
        System.out.println(pb.command());
        String errorLine = "";
        try {
            final Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                // [mpileup] 1 samples in 1 input files
                // <mpileup> Set max per-file depth to 8000
                reader.lines().forEachOrdered(line -> pileups.addAll(decodePileUp(line)));
                String eLine;
                while ((eLine = error.readLine()) != null) {
                    errorLine += "\n" + eLine;
                }
            } catch (IOException ex) {
                MainViewController.printException(ex);
            }
            int ret = p.waitFor();
            if (ret != 0) {
                MainViewController.printMessage("Problems loading alignments: " + errorLine, "error");
            }
        } catch (IOException | InterruptedException ex) {
            MainViewController.printException(ex);
        }
        return pileups;
    }

    /**
     * Returns one or more pileups, if there is an insertion, this list will grown up.
     *
     * @param line
     * @return
     */
    private List<PileUp> decodePileUp(String line) {
        /*
         * In the pileup format (without -u or -g), each line represents a genomic position,
         * consisting of chromosome name, 1-based coordinate, reference base, the number of reads
         * covering the site, read bases, base qualities and alignment mapping qualities.
         * Information on match, mismatch, indel, strand, mapping quality and start and end of a
         * read are all encoded at the read base column.
         * At this column:
         * (.) a dot stands for a match to the reference base on the forward strand.
         * (,) a comma for a match on the reverse strand.
         * (<>) a '>' or '<' for a reference skip.
         * (ACGTN) for a mismatch on the forward strand.
         * (acgtn) for a mismatch on the reverse strand.
         * ("\\+[0-9]+[ACGTNacgtn]+") indicates there is an insertion between this reference
         * position and the next reference position. The length of the insertion is given by the
         * integer in the pattern, followed by the inserted sequence.
         * ("-[0-9]+[ACGTNacgtn]+") represents a deletion from the reference. The deleted bases
         * will be presented as `*' in the following lines.
         * (^) marks the start of a read. The ASCII of the character following (^) minus 33 gives
         * the mapping quality.
         * ($) marks the end of a read segment.
         */
        List<PileUp> pileUps = new ArrayList<>();
        final String[] row = line.split("\t");
        final char ref = row[2].charAt(0);
        pileUps.add(new PileUp(ref));
        char[] reads = row[4].replace('.', ref).replace(',', Character.toLowerCase(ref)).toCharArray();
        for (int i = 0; i < reads.length; i++) {
            char base = reads[i];
            switch (base) {
                case 'c':
                case 'C':
                case 'a':
                case 'A':
                case 't':
                case 'T':
                case 'g':
                case 'G':
                    pileUps.get(0).incrementDepth(base);
                    break;
                case '+':
                    int j = i + 1;
                    while (Character.isDigit(reads[j])) {
                        j++;
                    }
                    // +1A
                    // i j (length=1)
                    // +10ACTACGTACG
                    // i  j (length=10)
                    int length = 0;
                    try {
                        length = Integer.valueOf(row[4].substring(i + 1, j));
                    } catch (Exception ex) {
                        MainViewController.printException(ex);
                    }
                    for (int k = 0; k < length; k++) {
                        PileUp pu;
                        if (pileUps.size() < k + 2) {
                            pu = new PileUp(PileUp.EMPTY);
                            pileUps.add(pu);
                        } else {
                            pu = pileUps.get(k + 1);
                        }
                        pu.incrementDepth(reads[j + k]);
                        i = j + length - 1;
                        break;
                    }
                    break;
                case '-':
                    // Deletions just need to be supressed
                    j = i + 1;
                    while (Character.isDigit(reads[j])) {
                        j++;
                    }
                    // -1A
                    // i j (length=1)
                    // -10ACTACGTACG
                    // i  j (length=10)
                    length = Integer.valueOf(row[4].substring(i + 1, j));
                    i = j + length - 1;
                    break;
                case '$':
                    // nothing to do
                    break;
                case '^':
                    // skip quality value
                    i++;
                    break;
                case '*':
                    // nothing to do
                    break;
                case '>':
                case '<':
                // What to do?
            }

        }
        return pileUps;
    }
}
