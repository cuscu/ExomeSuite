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
package exomesuite.vcf;

import exomesuite.graphic.FileParam;
import exomesuite.graphic.SizableImage;
import exomesuite.utils.FileManager;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

/**
 * Copmbine VCFs tool. It will generate a VCF with the variants present in includes files and not
 * present in exclude files.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class CombineVariants {

    @FXML
    private FileParam output;
    @FXML
    private Button startButton;
    @FXML
    private ListView<File> includes;
    @FXML
    private Button addInclude;
    @FXML
    private ListView<File> excludes;
    @FXML
    private Button addExclude;

    @FXML
    private void initialize() {
        includes.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                includes.getItems().remove(includes.getSelectionModel().getSelectedItem());
            }
        });
        excludes.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                excludes.getItems().remove(excludes.getSelectionModel().getSelectedItem());
            }
        });
        addInclude.setOnAction(e -> addInclude());
        addExclude.setOnAction(e -> addExclude());
        startButton.setOnAction(e -> start());
        // The start Button is disable until user selects an output file.
        startButton.setDisable(true);
        output.setOnValueChanged(e -> startButton.setDisable(false));
        output.addFilter(FileManager.VCF_FILTER);
        startButton.setGraphic(new SizableImage("exomesuite/img/start.png", 32));
        addExclude.setGraphic(new SizableImage("exomesuite/img/addFile.png", 32));
        addInclude.setGraphic(new SizableImage("exomesuite/img/addFile.png", 32));

    }

    private void addInclude() {
        List<File> f = FileManager.openFiles("Select VCF", FileManager.VCF_FILTER);
        if (f != null) {
            includes.getItems().addAll(f);
        }
    }

    private void addExclude() {
        List<File> f = FileManager.openFiles("Select VCF", FileManager.VCF_FILTER);
        if (f != null) {
            excludes.getItems().addAll(f);
        }
    }

    private void start() {
        System.out.println("intersecting");
        System.out.println("Includes:" + includes.getItems().toString());
        System.out.println("Excludes:" + excludes.getItems().toString());
        System.out.println("Output:" + output.getValue());
    }

}
