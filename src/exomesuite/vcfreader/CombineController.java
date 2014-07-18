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
package exomesuite.vcfreader;

import exomesuite.utils.OS;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class CombineController {

    @FXML
    private TextField vcf1;
    @FXML
    private TextField mist1;
    @FXML
    private TextField vcf2;
    @FXML
    private TextField mist2;

    private File variants1, variants2, exons1, exons2;
    @FXML
    private Button combineButton;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        vcf1.setOnAction((ActionEvent event) -> openVCF(vcf1));
        vcf2.setOnAction((ActionEvent event) -> openVCF(vcf2));
        mist1.setOnAction((ActionEvent event) -> openMIST(mist1));
        mist2.setOnAction((ActionEvent event) -> openMIST(mist2));
        vcf1.setOnMouseClicked((MouseEvent event) -> openVCF(vcf1));
        vcf2.setOnMouseClicked((MouseEvent event) -> openVCF(vcf2));
        mist1.setOnMouseClicked((MouseEvent event) -> openMIST(mist1));
        mist2.setOnMouseClicked((MouseEvent event) -> openMIST(mist2));
        vcf1.setMaxWidth(Double.MAX_VALUE);
        vcf2.setMaxWidth(Double.MAX_VALUE);
        mist1.setMaxWidth(Double.MAX_VALUE);
        mist2.setMaxWidth(Double.MAX_VALUE);
    }

    private void openVCF(TextField vcf) {
        File f = OS.openFile(vcf, "Select Variant Call File", OS.VCF_FILTER);
        if (f != null) {
            File possibleMist = new File(f.getAbsolutePath().replace(".vcf", ".mist"));
            TextField mist;
            if (vcf.equals(vcf1)) {
                variants1 = f;
                if (possibleMist.exists()) {
                    mist1.setText(possibleMist.getAbsolutePath());
                    exons1 = possibleMist;
                }
            } else {
                variants2 = f;
                if (possibleMist.exists()) {
                    mist2.setText(possibleMist.getAbsolutePath());
                    exons2 = possibleMist;
                }
            }

        }
    }

    private void openMIST(TextField mist) {
        File f = OS.openFile(mist, "Select MIST file", OS.MIST_FILTER);
        if (mist.equals(mist1)) {
            exons1 = f;
        } else {
            exons2 = f;
        }
    }

    /**
     * @return the variants1
     */
    public File getVariants1() {
        return variants1;
    }

    /**
     * @return the variants2
     */
    public File getVariants2() {
        return variants2;
    }

    /**
     * @return the exons1
     */
    public File getExons1() {
        return exons1;
    }

    /**
     * @return the exons2
     */
    public File getExons2() {
        return exons2;
    }

    public Button getCombineButton() {
        return combineButton;
    }

}
