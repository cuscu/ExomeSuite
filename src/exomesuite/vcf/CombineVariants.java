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
import exomesuite.graphic.FlatButton;
import exomesuite.utils.FileManager;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class CombineVariants {

    @FXML
    private FileParam output;
    @FXML
    private FlatButton startButton;
    @FXML
    private ListView<File> includes;
    @FXML
    private FlatButton addInclude;
    @FXML
    private ListView<File> excludes;
    @FXML
    private FlatButton addExclude;

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
        output.addFilter(FileManager.VCF_FILTER);
    }

    public void show() {

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
