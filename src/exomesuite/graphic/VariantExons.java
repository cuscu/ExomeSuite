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
package exomesuite.graphic;

import exomesuite.utils.OS;
import exomesuite.vcf.Variant2;
import exomesuite.vcf.VariantListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.controlsfx.dialog.Dialogs;

/**
 * FXML Controller class. This is the panel under the variants table.
 *
 * @author Pascual Lorente Arencibia
 */
public class VariantExons extends TableView<String[]> implements VariantListener {

//    @FXML
//    private TableView<String[]> table;
    public VariantExons() {
        File exons = new File(OS.getProperty("ensembl"));
        Ensembl.initialize(exons);
        initialize();
    }

    private void initialize() {
        TableColumn<String[], String> start = new TableColumn<>("Coordinates");
        start.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> param)
                -> new SimpleStringProperty(String.format("%s:%s - %s",
                                param.getValue()[0], param.getValue()[1], param.getValue()[2])));
        getColumns().add(start);
        TableColumn<String[], String> geneid = new TableColumn<>("Gene ID");
        geneid.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> param)
                -> new SimpleStringProperty(param.getValue()[3]));
        getColumns().add(geneid);
        TableColumn<String[], String> genename = new TableColumn<>("Gene name");
        genename.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> param)
                -> new SimpleStringProperty(param.getValue()[4]));
        getColumns().add(genename);
        TableColumn<String[], String> exonid = new TableColumn<>("Exon ID");
        exonid.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> param)
                -> new SimpleStringProperty(param.getValue()[6]));
        getColumns().add(exonid);
        TableColumn<String[], String> exonName = new TableColumn<>("Transcript");
        exonName.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> param)
                -> new SimpleStringProperty(param.getValue()[7]));
        getColumns().add(exonName);
        TableColumn<String[], String> biotype = new TableColumn<>("Biotype");
        biotype.setCellValueFactory((TableColumn.CellDataFeatures<String[], String> param)
                -> new SimpleStringProperty(param.getValue()[8]));
        getColumns().add(biotype);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @Override
    public void variantChanged(Variant2 variant) {
        getItems().clear();
        if (variant == null) {
            return;
        }
        if (Ensembl.chromosomes.containsKey(variant.getChrom())) {
            List<Exon> list = new ArrayList<>(Ensembl.chromosomes.get(variant.getChrom()));
            for (Exon e : list) {
                if (e.start > variant.getPos()) {
                    break;
                } else if (e.start <= variant.getPos() && e.end >= variant.getPos()) {
                    getItems().add(e.line);
                }
            }
        } else {
            final String[] loading = {"loading...", "", "", "loading...", "loading...", "", "loading...",
                "loading...", "loading...", ""};
            final String[] empty = {"no data", "", "", "no data", "no data", "", "no data",
                "no data", "no data", ""};
            getItems().setAll(Ensembl.ready ? empty : loading);
        }
    }

    /**
     * Class that loads the Ensembl exons.
     */
    private static class Ensembl {

        static File file;
        static boolean ready = false;
        // Chr, list of exons
        static Map<String, List<Exon>> chromosomes;

        static void initialize(File file) {
            if (chromosomes != null) {
                return;
            }
            Task task;
            task = new Task() {

                @Override
                protected Object call() throws Exception {
                    System.out.println("Reading exons");
                    Ensembl.chromosomes = new TreeMap<>();
                    Ensembl.file = file;
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        // Skip header
                        br.readLine();
                        br.lines().forEach(line -> {
                            // Create the Exon
                            String[] row = line.split("\t");
                            String chr = row[0];
                            int start = Integer.valueOf(row[1]);
                            int end = Integer.valueOf(row[2]);
                            Exon exon = new Exon(chr, start, end, row);
                            // Take the exons list of the chromosome
                            List<Exon> exons = chromosomes.get(chr);
                            // If list does not exist, create it and add the only exon
                            if (exons == null) {
                                exons = new ArrayList<>();
                                exons.add(exon);
                                chromosomes.put(chr, exons);
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
                        Dialogs.create().showException(e);
                    }
                    return null;
                }

            };
            task.setOnSucceeded(e -> {
                ready = true;
//                print();
            });
            new Thread(task).start();
        }

        static void print() {
            File exit = new File("/home/unidad03/exons.txt");
            exit.delete();
            chromosomes.entrySet().stream().forEach(chromosome -> {
//                String chr = chromosome.getKey();
                chromosome.getValue().stream().forEach(exon -> {
                    try (BufferedWriter out = new BufferedWriter(new FileWriter(exit, true))) {
                        out.write(Arrays.toString(exon.line));
                        out.newLine();
                    } catch (Exception e) {
                        Dialogs.create().showException(e);
                    }
                });
            });
        }
    }

    /**
     * Keeps info about an exon line.
     */
    private static class Exon {

        String chr;
        int start;
        int end;
        String[] line;

        public Exon(String chr, int start, int end, String[] line) {
            this.chr = chr;
            this.start = start;
            this.end = end;
            this.line = line;
        }

    }
}
