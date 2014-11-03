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
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class MistAction extends Action {

    String output, input;
    int threshold;

    public MistAction(String icon, String description, String disableDescription) {
        super(icon, description, disableDescription);
    }

    @Override
    public boolean isDisabled(Project project) {
        String[] files = project.getProperty(Project.PropertyName.FILES, "").split(";");
        for (String file : files) {
            if (file.endsWith(".bam")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public SystemTask getTask(Project project) {
        String[] files = project.getProperty(Project.PropertyName.FILES, "").split(";");
        //String input = project.getProperty(Project.PropertyName.BAM_FILE);
        // Look for the first bam file
        List<String> bamfiles = new ArrayList<>();
        for (String s : files) {
            if (s.endsWith(".bam")) {
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
        showParamsView(bamfiles);
        if (!SystemTask.tripleCheck(input) || threshold < 0) {
            Dialogs.create().title("Bad arguments").message(
                    "Input: " + input
                    + "\nThreshold (>=0): " + threshold).
                    showError();
            return null;
        }
        output = project.getProperty(Project.PropertyName.PATH) + File.separator
                + project.getProperty(Project.PropertyName.CODE) + "_dp" + threshold + ".mist";
        String ensembl = OS.getProperty("ensembl");
        if (ensembl == null || ensembl.isEmpty()) {
            Dialogs.create().title("Ensembl database is missing").message(
                    "you need to select the ensembl database in Databases view").
                    showError();
            return null;
        }
        return new Mist(input, output, ensembl, threshold);
    }

    @Override
    public void onSucceeded(Project p, SystemTask t) {
        if (t.getValue() == 0) {
            p.addExtraFile(output);
        }
    }

    private void showParamsView(List<String> bams) {
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
                threshold = params.getThreshold();
                input = params.getSelectedBam();
            });
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(MistAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
