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
package exomesuite;

import exomesuite.tool.ToolPane;
import exomesuite.utils.OS;
import java.io.File;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;

/**
 * This class manages the reference genome selector. The view is hold by a ToolPane.
 *
 * @see ToolPane
 *
 * @author Pascual Lorente Arencibia
 */
public class GenomeManager {

    private final ToolPane tool;
    private Button index;
    private Indexer indexer;
    private String name;

    /**
     * Creates a GenomeManager, if you want to show it, use the getView.
     */
    public GenomeManager() {
        /*
         * RED:   genome not selected (select:enable, index:disable)
         *        genome not indexed (select:enable, index:enable)
         * GREEN: genome selected and indexed (remove:enabled)
         */
        tool = new ToolPane("Reference genome", ToolPane.Status.RED);
        Button delete = new Button(null, new ImageView("exomesuite/img/delete.png"));
        Button add = new Button(null, new ImageView("exomesuite/img/add.png"));
        index = new Button(null, new ImageView("exomesuite/img/r_arrow.png"));
        tool.addButton(ToolPane.Status.GREEN, delete);
        tool.addButton(ToolPane.Status.RED, add);
        tool.addButton(ToolPane.Status.RED, index);
        index.setDisable(true);
        delete.setOnAction((ActionEvent event) -> {
            MainViewController.getConfig().removeProperty("genome");
            tool.setName("Reference genome");
            tool.setStatus(ToolPane.Status.RED);
            index.setDisable(true);
        });
        add.setOnAction((ActionEvent event) -> {
            File f = OS.openFASTA();
            if (f != null) {
                setProject(f);
            }
        });
        index.setOnAction((ActionEvent event) -> {
            startIndex();
        });
        // Checks if there is a genome in config file.
        if (MainViewController.getGenome() != null) {
            File f = new File(MainViewController.getGenome());
            setProject(f);
        }
    }

    /**
     * Puts into config file the File f and checks if the genome is indexed. In that case, GREEN
     * status will be active.
     *
     * @param f A file containing the reference genome path.
     */
    private void setProject(File f) {
        name = f.getName().replace(".fasta", "").replace(".fa", "");
        tool.setName(name);
        MainViewController.setGenome(f.getAbsolutePath());
        if (isIndexed(f)) {
            System.out.println("Indexed");
            tool.setStatus(ToolPane.Status.GREEN);
        } else {
            index.setDisable(false);
        }
    }

    /**
     * Returns the root pane. Place it wherever you want.
     *
     * @return the root pane.
     */
    public Node getView() {
        return tool.getView();
    }

    /**
     * Checks if a genome is indexed by looking for predefined extensions. It does not guarantee
     * that the genome is really indexed, but that index files exist.
     *
     * @param f The genome.
     * @return true if all index files exist.
     */
    private boolean isIndexed(File f) {
        final String[] extensions = {".fai", ".pac", ".rbwt", ".amb", ".rpac", ".ann", ".rsa",
            ".bwt", ".sa", ".dict"};
        final File parent = f.getParentFile();
        for (String ext : extensions) {
            if (!new File(parent, f.getName() + ext).exists()) {
                return false;
            }
        }
        return true;
    }

    private void startIndex() {
        String genome = MainViewController.getGenome();
        if (genome != null) {
            tool.setName("");
            indexer = new Indexer(genome);
            indexer.setOnSucceeded((WorkerStateEvent event) -> {
                tool.setStatus(ToolPane.Status.GREEN);
                tool.setName(name);
            });
            indexer.setOnCancelled((WorkerStateEvent event) -> {
                tool.setStatus(ToolPane.Status.RED);
                tool.setName(name);
            });
            // Listen to progress and messages.
            indexer.progressProperty().addListener((
                    ObservableValue<? extends Number> observable, Number oldValue, Number newValue)
                    -> {
                tool.updateProgress(indexer.getMessage(), newValue.doubleValue());
                if (newValue.intValue() == 1) {
                    tool.setStatus(ToolPane.Status.GREEN);
                    tool.setName(name);
                }
            });
            indexer.messageProperty().addListener((
                    ObservableValue<? extends String> observable, String oldValue, String newValue)
                    -> {
                tool.updateProgress(newValue, indexer.getProgress());
            });
            // Go go go, fire in the hole!!
            new Thread(indexer).start();
        }
    }

}
