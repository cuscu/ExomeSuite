/*
 * Copyright (C) 2014 uichuimi03
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
package exomesuite.tool.align;

import exomesuite.utils.OS;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author uichuimi03
 */
public class AlignParamsViewController {

    @FXML
    private RadioButton phred64;
    @FXML
    private RadioButton phred33;

    private ToggleGroup group;
    @FXML
    private TextField mills;
    @FXML
    private TextField phase1;
    @FXML
    private TextField dbsnp;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        group = new ToggleGroup();
        phred33.setToggleGroup(group);
        phred64.setToggleGroup(group);
        dbsnp.setOnAction((ActionEvent event) -> {
            openDbsnp();
        });
        dbsnp.setOnMouseClicked((MouseEvent event) -> {
            openDbsnp();
        });
        mills.setOnAction((ActionEvent event) -> {
            openMills();
        });
        mills.setOnMouseClicked((MouseEvent event) -> {
            openMills();
        });
        phase1.setOnAction((ActionEvent event) -> {
            openPhase1();
        });
        phase1.setOnMouseClicked((MouseEvent event) -> {
            openPhase1();
        });
    }

    boolean isPhred64() {
        return phred64.isSelected();
    }

    public TextField getDbsnp() {
        return dbsnp;
    }

    public TextField getMills() {
        return mills;
    }

    public TextField getPhase1() {
        return phase1;
    }

    private void openDbsnp() {
        OS.openVCF(dbsnp);

    }

    private void openMills() {
        OS.openVCF(mills);
    }

    private void openPhase1() {
        OS.openVCF(phase1);
    }

}
