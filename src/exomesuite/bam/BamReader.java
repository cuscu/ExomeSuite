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
package exomesuite.bam;

import exomesuite.graphic.ChoiceParam;
import exomesuite.graphic.NumberParam;
import exomesuite.graphic.TextParam;
import exomesuite.utils.OS;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * The super graph.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class BamReader extends VBox {

    @FXML
    private StackPane canvas;
    @FXML
    private ChoiceParam chromosome;
    @FXML
    private TextParam position;
    @FXML
    private Label info;
    @FXML
    private NumberParam zoom;

    private final File bamFile;
    private final File genome;
    private GraphParameters parameters;
    private BamBaseBackgroundLayer backgroundLayer;
    private BamAxisLayer axisLayer;
    private BamBarsLayer barsLayer;
    private BamSelectLayer selectLayer;
    private BamTickLabelLayer tickLabelLayer;
    private BamTicksLayer ticksLayer;
    private BamBaseLabelLayer baseLabelLayer;

    public BamReader(File bamFile, File genome) {
        this.bamFile = bamFile;
        this.genome = genome;
        System.out.println(bamFile.getAbsolutePath());
        System.out.println(genome.getAbsolutePath());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BamReader.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, e);
        }
    }

    @FXML
    private void initialize() {
        canvas.setPrefSize(9999, 9999);
        canvas.setMinSize(1, 1);
        canvas.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        parameters = new GraphParameters();
        backgroundLayer = new BamBaseBackgroundLayer(parameters);
        axisLayer = new BamAxisLayer(parameters);
        barsLayer = new BamBarsLayer(parameters);
        selectLayer = new BamSelectLayer(parameters);
        tickLabelLayer = new BamTickLabelLayer(parameters);
        baseLabelLayer = new BamBaseLabelLayer(parameters);
        ticksLayer = new BamTicksLayer(parameters);
        addlayer(backgroundLayer);
        addlayer(selectLayer);
        addlayer(barsLayer);
        addlayer(axisLayer);
        addlayer(ticksLayer);
        addlayer(tickLabelLayer);
        addlayer(baseLabelLayer);
        backgroundLayer.setDisable(true);
        parameters.setPercentageUnits(false);
        parameters.setBaseColors(false);
        parameters.setGenomicPosition(1);
        parameters.setReference(getReferenceSequence("1", 1, 100));
        parameters.getSelectedIndex().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if (parameters.getValues().size() > newValue.intValue()) {
                info.setText((newValue.intValue() + parameters.getGenomicPosition().intValue())
                        + ":" + parameters.getValues().get(parameters.getSelectedIndex().get()).toString());
            }
        });
        chromosome.setOptions(OS.getStandardChromosomes());
        position.setOnValueChanged(event
                -> setPosition(chromosome.getValue(), Integer.valueOf(position.getValue())));
        zoom.setOnValueChanged(e -> parameters.setBaseWidth(Double.valueOf(zoom.getValue())));
    }

    private void addlayer(BamLayer layer) {
        layer.widthProperty().bind(canvas.widthProperty());
        layer.heightProperty().bind(canvas.heightProperty());
        canvas.getChildren().add(layer);
    }

    private void setPosition(String chr, int position) {
        final int elements = (int) Math.floor((canvas.getWidth() - parameters.getAxisMargin().get())
                / parameters.getBaseWidth().get());
        System.out.println(elements);
        // Get reference sequence
        parameters.setReference(getReferenceSequence(chr, position, position + elements));
        parameters.setGenomicPosition(position);
        parameters.setValues(getDepthValues(chr, position, position + elements));
    }

    /**
     * Gets a list with the bases of the reference.
     *
     * @param chr
     * @param from
     * @param to
     * @return
     */
    private List<Character> getReferenceSequence(String chr, int from, int to) {
        List<Character> list = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder("samtools", "faidx", genome.getAbsolutePath(),
                chr + ":" + from + "-" + to);
        try {
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                // Skip echo
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    for (char c : line.toCharArray()) {
                        list.add(c);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(BamReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(BamReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    private List<Map<Character, Integer>> getDepthValues(String chr, int from, int to) {
        Map<Character, Integer>[] array = new Map[to - from + 1];
        ProcessBuilder pb = new ProcessBuilder("samtools", "view", bamFile.getAbsolutePath(),
                chr + ":" + from + "-" + to);
        try {
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                reader.lines().forEach(line -> {
                    String[] row = line.split("\t");
                    String seq = row[9];
                    int pos = Integer.valueOf(row[3]);
                    int i = pos < from ? 0 : pos - from;
                    int j = pos < from ? from - pos : 0;
                    while (i < array.length && i + from < to && j < seq.length()) {
                        if (array[i] == null) {
                            array[i] = new TreeMap<>();
                            array[i].put(seq.charAt(j), 1);
                        } else {
                            Map<Character, Integer> map = array[i];
                            int dp = map.getOrDefault(seq.charAt(j), 0);
                            map.put(seq.charAt(j), dp + 1);
                        }
                        i++;
                        j++;
                    }
                });
            } catch (IOException ex) {
                Logger.getLogger(BamReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(BamReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Arrays.asList(array);
    }
}
