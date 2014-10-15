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

import exomesuite.graphic.CallParams;
import exomesuite.graphic.MistParams;
import exomesuite.systemtask.Caller;
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
public class CallAction extends Action {

    private String output, inputBAM;

    public CallAction(String icon, String description, String disableDescription) {
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
        List<String> errors = new ArrayList<>();
        showParamsView(project);
        if (!SystemTask.tripleCheck(inputBAM)) {
            errors.add("Alignments are missing");
        }
        String genome = project.getProperty(Project.PropertyName.REFERENCE_GENOME);
        if (genome == null) {
            errors.add("Project has no genome selected");
            Dialogs.create().title("Project has no genome selected").message(
                    "Select a reference genome in project properties.").showError();
        }
        String genomeFile = OS.getProperty(genome);
        if (genomeFile == null) {
            errors.add("Genome " + genome + " is not selected in Databases");
        }
        output = project.getProperty(Project.PropertyName.PATH) + File.separator
                + project.getProperty(Project.PropertyName.CODE) + ".vcf";
        final String dbsnp = OS.getProperty("dbsnp");
        if (output == null) {
            errors.add("dbSNP is missing. Select it in Databases");
        }
        if (errors.isEmpty()) {
            return new Caller(genomeFile, output, inputBAM, dbsnp);
        } else {
            Dialogs.create().title("Wrong parameters in Call task").message(errors.toString()).
                    showError();
            return null;
        }
    }

    @Override
    public void onSucceeded(Project p, SystemTask t) {
        if (t.getValue() == 0) {
            p.addExtraFile(output);
        }
    }

    private void showParamsView(Project project) {
        String[] files = project.getProperty(Project.PropertyName.FILES, "").split(";");
        //String input = project.getProperty(Project.PropertyName.BAM_FILE);
        // Look for the first bam file
        List<String> bams = new ArrayList<>();
        for (String s : files) {
            if (s.endsWith(".bam")) {
                bams.add(s);
            }
        }
        try {
            FXMLLoader loader = new FXMLLoader(MistParams.class.getResource("CallParams.fxml"));
            loader.load();
            CallParams params = loader.getController();
            params.setBamOptions(bams);
            Scene scene = new Scene(loader.getRoot());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            params.setOnAccept(e -> {
                stage.close();
                inputBAM = params.getSelectedBam();
            });
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(CallAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
