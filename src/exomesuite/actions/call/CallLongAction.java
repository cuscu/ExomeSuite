/*
 * Copyright (C) 2015 UICHUIMI
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
package exomesuite.actions.call;

import exomesuite.ExomeSuite;
import exomesuite.MainViewController;
import exomesuite.actions.LongAction;
import exomesuite.actions.SystemTask;
import exomesuite.project.Project;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 */
public class CallLongAction extends LongAction {

    @Override
    public String getName() {
        return ExomeSuite.getResources().getString("call");
    }

    @Override
    public String getIconPath() {
        return "exomesuite/img/call.png";
    }

    @Override
    public boolean isDisable(Project project) {
        if (project == null) {
            return true;
        }
        String files = project.getProperties().getProperty(Project.FILES, "");
        List<String> fs = Arrays.asList(files.split(";"));
        return fs.stream().noneMatch((file) -> (file.endsWith(".bam")));
    }

    @Override
    public SystemTask getTask(Project project) {
        // Prepare parameters to the parameters window.
        String files = project.getProperties().getProperty(Project.FILES, "");
        List<String> fs = Arrays.asList(files.split(";"));
        List<String> bams = new ArrayList();
        fs.forEach(file -> {
            if (file.endsWith(".bam")) {
                bams.add(file);
            }
        });
        String reference = project.getProperties().getProperty(Project.REFERENCE_GENOME);
        List<String> referenceGenomes = OS.getReferenceGenomes();
        String output = project.getProperties().getProperty(Project.PATH) + File.separator
                + project.getProperties().getProperty(Project.CODE) + ".vcf";
        // Load view from AlignerParameters.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("CallerParameters.fxml"),
                ExomeSuite.getResources());
        try {
            loader.load();
        } catch (IOException ex) {
            MainViewController.printException(ex);
            return null;
        }
        CallerParameters controller = loader.getController();
        // Prepare enclosure for root view.
        Stage stage = new Stage();
        Scene scene = new Scene(loader.getRoot());
        controller.setAlignmentsOptions(bams);
        controller.setAlignments(bams.get(0));
        controller.setReferenceOptions(referenceGenomes);
        controller.setReference(reference);
        controller.setAlgorithmOptions("GATK");
        controller.setAlgorithm("GATK");
        controller.setOutput(output);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.UTILITY);
        // Show window and wait till closure.
        controller.setOnClose(event -> stage.close());
        stage.showAndWait();

        // Control after user closed parameters window.
        if (controller.accepted()) {
            List<String> errors = new ArrayList();
            String selectedAlgorithm = controller.getSelectedAlgorithm();
            String selectedAlignments = controller.getSelectedAlignments();
            String selectedReference = controller.getSelectedReference();
            String selectedOutput = controller.getOutput();
            if (!FileManager.tripleCheck(selectedAlignments)) {
                errors.add(ExomeSuite.getResources().getString("alignments"));
            }
            String temp = OS.getTempDir();
            String genome = OS.getProperties().getProperty(selectedReference);
            String dbsnp = OS.getProperties().getProperty("dbsnp");
            String name = project.getProperties().getProperty(Project.NAME);
            // Check that parameters are ok.
            if (!FileManager.tripleCheck(dbsnp)) {
                errors.add(ExomeSuite.getResources().getString("dbsnp"));
            }
            if (!FileManager.tripleCheck(genome)) {
                errors.add(ExomeSuite.getResources().getString("reference.genome"));
            }
            if (!FileManager.tripleCheck(temp)) {
                errors.add(ExomeSuite.getResources().getString("temp.path"));
            }
            if (!errors.isEmpty()) {
                String message = ExomeSuite.getResources().getString("missing.arguments") + "\n"
                        + ": [" + OS.asString(",", errors) + "]";
                MainViewController.printMessage(message, "warning");
                return null;
            }
            SystemTask caller = new Caller(genome, selectedOutput, selectedAlignments, dbsnp);
            caller.stateProperty().addListener((obs, old, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    project.addExtraFile(selectedOutput);
                }
            });
            return caller;
        }

        return null;
    }

}
