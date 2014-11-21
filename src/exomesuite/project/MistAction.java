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
package exomesuite.project;

import exomesuite.graphic.MistParams;
import exomesuite.systemtask.Mist;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class MistAction extends Action {

    public MistAction(String icon, String description, String disableDescription) {
        super(icon, description, disableDescription);
    }

    @Override
    public boolean isDisabled(Project project) {
        // To work, MIST only needs a bam file and the Ensembl database
        String[] files = project.getProperty(Project.PropertyName.FILES, "").split(";");
        for (String file : files) {
            if (file.endsWith(".bam")) {
                return false;
            }
        }
        String ensembl = OS.getProperty("ensembl");
        return FileManager.tripleCheck(ensembl);
    }

    @Override
    public SystemTask getTask(Project project) {
        // Set the list of BAM files
        String[] files = project.getProperty(Project.PropertyName.FILES, "").split(";");
        List<String> bamfiles = new ArrayList<>();
        for (String s : files) {
            if (s.endsWith(".bam") && FileManager.tripleCheck(s)) {
                bamfiles.add(s);
            }
        }
        if (bamfiles.isEmpty()) {
            Dialogs.create().title("BAM file missing").message(
                    "There are not BAM files in processed data of the project."
                    + " Please, add some of them or align first.").
                    showError();
            return null;
        }

        // Ask user for params
        Properties params = showParamsView(bamfiles);
        if (params.isEmpty()) {
            return null;
        }
        // Assert threshold is an integer
        final int threshold;
        try {
            threshold = Integer.valueOf(params.getProperty("threshold"));
            if (threshold < 1) {
                throw new NumberFormatException("threshold must be > 0");
            }
        } catch (NumberFormatException e) {
            Dialogs.create().title("Threshold format error").showException(e);
            return null;
        }
        // Assert length is an Integer
        final int length;
        try {
            length = Integer.valueOf(params.getProperty("length"));
            if (length < 1) {
                throw new NumberFormatException("length must be greater than 0");
            }

        } catch (NumberFormatException e) {
            Dialogs.create().title("Length format error").showException(e);
            return null;
        }
        // No need to triple check input, I did it at the begining of the method.
        final String input = params.getProperty("input");

        // Check that PATH exists
        if (!FileManager.tripleCheck(project.getProperty(Project.PropertyName.PATH))) {
            Dialogs.create().title("Incorrect path").message("PATH " + project.getProperty(
                    Project.PropertyName.PATH) + " does not exist").showError();
            return null;
        }
        // Output is path/code_dp10_l1.mist
        String output = project.getProperty(Project.PropertyName.PATH) + File.separator
                + project.getProperty(Project.PropertyName.CODE) + "_dp" + threshold
                + "_l" + length + ".mist";
        final File out = new File(output);
        if (out.exists()) {
            org.controlsfx.control.action.Action ret = Dialogs.create().title("Repeat MIST?")
                    .message("A file with name " + out.getName()
                            + " exists. Do you want to replace it?").showConfirm();
            if (ret == Dialog.ACTION_CANCEL || ret == Dialog.ACTION_NO) {
                return null;
            }
        }
        String ensembl = OS.getProperty("ensembl");
        if (!FileManager.tripleCheck(ensembl)) {
            Dialogs.create().title("Ensembl database is missing").message(
                    "you need to select the ensembl database in Databases view").
                    showError();
            return null;
        }
        /* Buf, input exists, output parent path exists, ensembl exists, threshold and length are
         positive Integers. If this does not work (x_x) */
        Mist mist = new Mist(input, output, ensembl, threshold, length);
        mist.setOnSucceeded(e -> {
            if (mist.getValue() == 0) {
                project.addExtraFile(output);
            }
        });
        return mist;
    }

    private Properties showParamsView(List<String> bams) {
        Properties properties = new Properties();
        try {
            FXMLLoader loader = new FXMLLoader(MistParams.class.getResource("MistParams.fxml"));
            loader.load();
            MistParams params = loader.getController();
            params.setBamOptions(bams);
            Scene scene = new Scene(loader.getRoot());
            Stage stage = new Stage();
            scene.getStylesheets().add("exomesuite/main.css");
            stage.setScene(scene);
            stage.centerOnScreen();
            params.setOnAccept(e -> {
                stage.close();
                properties.putAll(params.getProperties());
            });
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MistAction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return properties;
    }

}
