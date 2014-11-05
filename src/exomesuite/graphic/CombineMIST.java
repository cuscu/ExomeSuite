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

import exomesuite.utils.FileManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.controlsfx.dialog.Dialogs;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class CombineMIST {

    @FXML
    private ListView<File> fileList;
    @FXML
    private FlatButton addButton;
    @FXML
    private Parameter outputParam;
    @FXML
    private FlatButton startButton;
    final int compareColumn = 8;
    String[] header;
    List<String[]> lines;
    Set<String>[] inputKeys;

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        addButton.setOnAction(e -> {
            List<File> f = FileManager.openFiles("Select MIST", FileManager.MIST_FILTER,
                    FileManager.ALL_FILTER);
            fileList.getItems().addAll(f);
        });
        startButton.setDisable(true);
        outputParam.setOnValueChanged(e -> startButton.setDisable(false));
        startButton.setOnAction(e -> intersect());
    }

    private void intersect() {
        List<File> inputs = fileList.getItems();
        File output = new File(outputParam.getValue());
        AtomicInteger c = new AtomicInteger();
        AtomicInteger m = new AtomicInteger();

        // Voy a ordenar los ficheros por exon_id y guardar ya la info minima
        loadMaps(inputs);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            printLine(bw, header);
            for (String[] row : lines) {
                final String id = row[compareColumn];
                boolean match = true;
                for (Set s : inputKeys) {
                    if (!s.contains(id)) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    row[3] = ".";
                    row[4] = ".";
                    row[12] = ".";
                    printLine(bw, row);
                    m.incrementAndGet();

                }
//                if (c.incrementAndGet() % 1000 == 0) {
//                    System.out.println(c + " lines (" + m + " matches)");
//                }
            }
        } catch (Exception ex) {
            Logger.getLogger(CombineMIST.class.getName()).log(Level.SEVERE, null, ex);
        }
        Dialogs.create().title("Completed").message("File " + output + " generated").
                showInformation();
    }

    /**
     * Guarda en inputMaps los ficheros ordenados por compareColumn, sin repetidos (se guarda el
     * último)
     *
     * @param input
     */
    private void loadMaps(List<File> input) {
        inputKeys = new Set[input.size()];
        lines = new ArrayList<>();
        // Se lee cada ficehro de entrada
        for (int i = 0; i < input.size(); i++) {
            inputKeys[i] = new TreeSet<>();
            final Set<String> set = inputKeys[i];
            try (BufferedReader reader = new BufferedReader(new FileReader(input.get(i)))) {
                if (i == 0) {
                    header = reader.readLine().split("\t");
                    reader.lines().forEach(line -> {
                        String[] row = line.split("\t");
                        String id = row[compareColumn];
                        if (!set.contains(id)) {
                            lines.add(row);
                            set.add(id);
                        }
                    });
                } else {
                    reader.lines().forEach(line -> {
                        String[] row = line.split("\t");
                        String id = row[compareColumn];
                        if (!set.contains(id)) {
                            set.add(id);
                        }
                    });

                }
            } catch (Exception e) {
                Logger.getLogger(CombineMIST.class.getName()).log(Level.SEVERE, e, null);
            }
        }
    }

    private void printLine(BufferedWriter bw, String[] row) throws IOException {
        bw.write(row[0]);
        for (int i = 1; i < row.length; i++) {
            bw.write("\t" + row[i]);
        }
        bw.newLine();
    }

}
