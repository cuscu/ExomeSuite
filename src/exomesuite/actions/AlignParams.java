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
package exomesuite.actions;

import exomesuite.MainViewController;
import exomesuite.graphic.ChoiceParam;
import exomesuite.graphic.FileParam;
import exomesuite.graphic.SizableImage;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.util.Properties;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gamil.com)
 */
public class AlignParams extends VBox {

    private final Properties properties;
    private boolean accepted = false;
    private EventHandler handler;

    @FXML
    private FileParam forward;
    @FXML
    private FileParam reverse;
    @FXML
    private ChoiceParam encoding;
    @FXML
    private ChoiceParam reference;
    @FXML
    private Button accept;
    @FXML
    private Button cancel;

    public AlignParams(Properties properties) {
        this.properties = properties;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AlignParams.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception e) {
            MainViewController.printException(e);
        }
    }

    @FXML
    private void initialize() {
        forward.addFilter(FileManager.FASTQ_FILTER);
        reverse.addFilter(FileManager.FASTQ_FILTER);
        if (properties.containsKey("forward")) {
            forward.setValue(new File(properties.getProperty("forward")));
        }
        if (properties.containsKey("reverse")) {
            reverse.setValue(new File(properties.getProperty("reverse")));
        }
        if (properties.containsKey("encoding")) {
            encoding.setValue(properties.getProperty("encoding"));
        }
        if (properties.containsKey("reference")) {
            reference.setValue(properties.getProperty("reference"));
        }
        accept.setOnAction(event -> {
            accepted = true;
            handler.handle(event);
        });
        cancel.setOnAction(event -> handler.handle(event));
        encoding.setOptions(OS.getSupportedEncodings());
        reference.setOptions(OS.getSupportedReferenceGenomes());
        accept.setGraphic(new SizableImage("exomesuite/img/align.png", 32));
        cancel.setGraphic(new SizableImage("exomesuite/img/cancel.png", 32));
    }

    public boolean accept() {
        return accepted;
    }

    public Properties getParams() {
        return properties;
    }

    public void setOnClose(EventHandler handler) {
        this.handler = handler;
    }

}
