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

import exomesuite.systemtask.Indexer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Checks if genome is indexed. Index genome using different softwares: samtools, bwa.
 *
 * @author Pascual Lorente Arencibia
 */
public class GenomeIndexer {

    public static boolean isIndexed(File file) {
        final String[] extensions = {".pac", ".sa", ".amb", ".ann", ".bwt", ".fai"};
        boolean isIndexed = true;
        for (String extension : extensions) {
            if (!new File(file.getAbsolutePath() + extension).exists()) {
                isIndexed = false;
                break;
            }
        }
        return isIndexed;
    }

    public static void index(File file) {
        Indexer indexer = new Indexer(file.getAbsolutePath());
        ProgressBar pb = new ProgressBar();
        Label title = new Label();
        Label label = new Label();
        TextArea area = new TextArea();
        Button stop = new Button("Stop");
        area.setEditable(false);
        indexer.setPrintStream(new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> area.appendText((char) b + ""));
            }
        }));
        pb.progressProperty().bind(indexer.progressProperty());
        label.textProperty().bind(indexer.messageProperty());
        title.textProperty().bind(indexer.titleProperty());
        Scene scene = new Scene(new VBox(pb, label, area, stop));
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.centerOnScreen();
        indexer.setOnSucceeded(e -> stage.close());
        stop.setOnAction(e -> indexer.cancel());
        new Thread(indexer).start();
        stage.showAndWait();

    }
}
