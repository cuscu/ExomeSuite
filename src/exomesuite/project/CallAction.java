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

import exomesuite.MainViewController;
import exomesuite.graphic.CallParams;
import exomesuite.graphic.MistParams;
import exomesuite.systemtask.Caller;
import exomesuite.systemtask.SamtoolsCaller;
import exomesuite.systemtask.SystemTask;
import exomesuite.utils.FileManager;
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

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class CallAction extends Action {

    private String output, inputBAM;
    private Algorithm selectedAlgorithm;

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
        if (!FileManager.tripleCheck(inputBAM)) {
            errors.add("Alignments are missing");
        }
        String genome = project.getProperty(Project.PropertyName.REFERENCE_GENOME);
        if (genome == null) {
            errors.add("Project has no genome selected");
            MainViewController.printMessage("Select a reference genome in project properties.", "warning");
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
            switch (selectedAlgorithm) {
                case GATK:
                    return new Caller(genomeFile, output, inputBAM, dbsnp);
                case SAMTOOLS:
                    return new SamtoolsCaller(genomeFile, dbsnp, inputBAM, output);
                default:
                    MainViewController.printMessage("Algorithm not selected", "warning");
//                    Dialogs.create().title("Algorithm not found")
//                            .message("Please select an algorithm").showError();
                    return null;
            }
        } else {
            MainViewController.printMessage("Wrong parameters in Call task:\n" + errors.toString(), "warning");
//            Dialogs.create().title("Wrong parameters in Call task")
//                    .message(errors.toString()).showError();
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
            // Pass the BAM option (which file to use)
            params.setBamOptions(bams);
            // Pass the algorithm options
            List<String> algs = new ArrayList<>();
            for (Algorithm a : Algorithm.values()) {
                algs.add(a.name());
            }
            params.setAlgorithmOptions(algs);
            // Show the scene
            Scene scene = new Scene(loader.getRoot());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            // When the user closes the window
            params.setOnAccept(e -> {
                stage.close();
                inputBAM = params.getSelectedBam();
                selectedAlgorithm = Algorithm.valueOf(params.getAlgorithm());
            });
            stage.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(CallAction.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public enum Algorithm {

        GATK, SAMTOOLS
    }
}
