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
package exomesuite.phase.align;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class AlignParamsViewController {

    @FXML
    private RadioButton phred64;
    @FXML
    private RadioButton phred33;

    private ToggleGroup group;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        group = new ToggleGroup();
        phred33.setToggleGroup(group);
        phred64.setToggleGroup(group);
    }

    boolean isPhred64() {
        return phred64.isSelected();
    }

}
