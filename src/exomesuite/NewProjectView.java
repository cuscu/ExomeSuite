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
package exomesuite;

import exomesuite.graphic.SizableImage;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.text.Normalizer;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class NewProjectView {

    @FXML
    private Label finalPath;
    @FXML
    private TextField name;
    @FXML
    private TextField code;
    @FXML
    private TextField path;
    @FXML
    private TextField forward;
    @FXML
    private TextField reverse;
    @FXML
    private Button selectPath;
    @FXML
    private Button selectForward;
    @FXML
    private Button selectReverse;
    @FXML
    private Button acceptButton;
    @FXML
    private ComboBox<String> genome;
    @FXML
    private ComboBox<String> encoding;
    @FXML
    private Label suggestedEncoding;

    /**
     * If user clicked on accpet.
     */
    private boolean accepted = false;
    /**
     * What happens whe user click on accept or cancel.
     */
    private EventHandler handler;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        selectForward.setOnAction(event -> {
            String message = ExomeSuite.getStringFormatted("select.file", "FASTQ");
            FileManager.openFile(forward, message, FileManager.FASTQ_FILTER);
            if (forward.getText() != null && !forward.getText().isEmpty()) {
                checkEncoding(forward.getText());
            }
        });
        selectReverse.setOnAction(event -> {
            String message = ExomeSuite.getStringFormatted("select.file", "FASTQ");
            FileManager.openFile(reverse, message, FileManager.FASTQ_FILTER);
            if (reverse.getText() != null && !reverse.getText().isEmpty()) {
                checkEncoding(reverse.getText());
            }
        });
        selectPath.setOnAction(event -> {
            File dir = FileManager.openDirectory(ExomeSuite.getResources().getString("select.path"));
            if (dir != null) {
                path.setText(dir.getAbsolutePath());
                updatePath();
                checkButton();
            }
        });
        name.setOnKeyReleased(event -> {
            String normalize = Normalizer.normalize(name.getText(), Normalizer.Form.NFD);
            code.setText(normalize.replace(" ", "_").replaceAll("\\p{InCombiningDiacriticalMarks}+", ""));
            checkButton();
        });
        code.setOnKeyReleased(event -> {
            updatePath();
            checkButton();
        });
        acceptButton.setOnAction(event -> {
            accepted = true;
            handler.handle(event);
        });
        acceptButton.setDisable(true);
        acceptButton.setGraphic(new SizableImage("exomesuite/img/new.png", SizableImage.SMALL_SIZE));
        encoding.getItems().setAll(OS.getEncodings());
        genome.getItems().setAll(OS.getReferenceGenomes());
    }

    String getName() {
        return name.getText();
    }

    String getCode() {
        return code.getText();
    }

    String getPath() {
        return path.getText();
    }

    String getForward() {
        return forward.getText();
    }

    String getReverse() {
        return reverse.getText();
    }

    String getGenome() {
        return genome.getValue();
    }

    String getEncoding() {
        return encoding.getValue();
    }

    public void setHandler(EventHandler handler) {
        this.handler = handler;
    }

    private void updatePath() {
        if (path.getText() != null && code.getText() != null) {
            finalPath.setText(path.getText() + File.separator + code.getText());
        }
    }

    public boolean isAccepted() {
        return accepted;
    }

    private void checkButton() {
        acceptButton.setDisable(name.getText().isEmpty() || code.getText().isEmpty() || path.getText().isEmpty());
    }

    private void checkEncoding(String file) {
        String enc = FileManager.guessEncoding(new File(file));
        suggestedEncoding.setText(enc == null ? null : ExomeSuite.getStringFormatted("suggested.encoding", enc));
    }

}
