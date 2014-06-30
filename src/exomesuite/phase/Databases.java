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
package exomesuite.phase;

import exomesuite.MainViewController;
import exomesuite.tool.ToolPane;
import exomesuite.utils.Config;
import exomesuite.utils.OS;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Databases {

    private final Config config;
    private final ToolPane toolPane;
    private VBox params;

    public Databases() {
        this.config = MainViewController.getConfig();
        toolPane = new ToolPane("Databases", properStatus(), new ImageView(
                "exomesuite/img/database.png"));
        Button set = new Button(null, new ImageView("exomesuite/img/r_arrow.png"));
        Button ok = new Button(null, new ImageView("exomesuite/img/accept.png"));
        toolPane.addButton(ToolPane.Status.RED, set);
        toolPane.addButton(ToolPane.Status.GREEN, set);
        toolPane.addButton(ToolPane.Status.OPEN, ok);
        set.setOnAction((ActionEvent event) -> {
            openSettings();
        });
        ok.setOnAction((ActionEvent event) -> {
            closeSettings();
        });
    }

    /**
     * Will show the pane with the paramsView,
     */
    private void openSettings() {
        if (params == null) {
            params = getParamsView();
        }
        toolPane.showPane(params);
        toolPane.setStatus(ToolPane.Status.OPEN);
    }

    /**
     * Will close the paramsView.
     */
    private void closeSettings() {
        toolPane.hidePane();
        toolPane.setStatus(properStatus());
    }

    /**
     *
     * @return a VBox with a TextField for each database.
     */
    private VBox getParamsView() {
        TextField mills = getVcfParam(Config.MILLS, "Mills VCF");
        TextField phase1 = getVcfParam(Config.PHASE1, "1000 Genome Phase 1");
        TextField dbsnp = getVcfParam(Config.DBSNP, "dbSNP");
        TextField omni = getVcfParam(Config.OMNI, "OMNI");
        TextField hapmap = getVcfParam(Config.HAPMAP, "Hapmap");
        TextField ensembl = getTsvTf(Config.ENSEMBL_EXONS, "Ensembl exons database (TSV)");
        return new VBox(3, mills, phase1, dbsnp, omni, hapmap, ensembl);
    }

    public Node getView() {
        return toolPane.getView();
    }

    private ToolPane.Status properStatus() {
        if (config.containsKey(Config.MILLS) && config.containsKey(Config.PHASE1)
                && config.containsKey(Config.DBSNP) && config.containsKey(Config.OMNI)
                && config.containsKey(Config.ENSEMBL_EXONS)) {
            return ToolPane.Status.GREEN;
        } else {
            return ToolPane.Status.RED;
        }
    }

    /**
     * Opens a dialog to select a VCF and, if success, sets the textFields text with the absolute
     * path of the selected file. It will also create a property in config file with the key and the
     * file.
     *
     * @param key A key for the config file.
     * @param textField A textField.
     */
    private void setVCF(String key, TextField textField) {
        File f = OS.openVCF(textField);
        if (f != null) {
            config.setProperty(key, f.getAbsolutePath());
        }
    }

    /**
     * Creates a TextField with desc as prompt text. The textField will respond to actionEvent and
     * mouseClicked event. This events will fire setParam, with name as key for the config file.
     *
     * @param name A key for config.
     * @param desc A prompt text.
     * @return
     */
    private TextField getVcfParam(String name, String desc) {
        TextField textField = new TextField();
        if (config.containsKey(name)) {
            textField.setText(config.getProperty(name));
        }
        textField.setPromptText(desc);
        textField.setEditable(false);
        textField.setOnAction((ActionEvent event) -> {
            setVCF(name, textField);
        });
        textField.setOnMouseClicked((MouseEvent event) -> {
            setVCF(name, textField);
        });
        return textField;
    }

    private TextField getTsvTf(String name, String prompt) {
        TextField textField = new TextField();
        if (config.containsKey(name)) {
            textField.setText(config.getProperty(name));
        }
        textField.setPromptText(prompt);
        textField.setEditable(false);
        textField.setOnAction((ActionEvent event) -> {
            setTSV(name, textField);
        });
        textField.setOnMouseClicked((MouseEvent event) -> {
            setTSV(name, textField);
        });
        return textField;
    }

    private void setTSV(String name, TextField textField) {
        File f = OS.openTSV(textField);
        if (f != null) {
            config.setProperty(name, f.getAbsolutePath());
        }
    }

    public boolean isRunning() {
        return false;
    }

}
