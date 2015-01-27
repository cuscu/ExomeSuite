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
package exomesuite.actions.align;

import exomesuite.ExomeSuite;
import exomesuite.MainViewController;
import exomesuite.actions.LongAction;
import exomesuite.actions.SystemTask;
import exomesuite.project.ModelProject;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
public class AlignLongAction extends LongAction {

    @Override
    public String getName() {
        return ExomeSuite.getResources().getString("align");
    }

    @Override
    public boolean isDisable(ModelProject project) {
        if (project == null) {
            return true;
        }
        return project.getForwardSequences() == null || project.getReverseSequences() == null;
    }

    @Override
    public String getIconPath() {
        return "exomesuite/img/align.png";
    }

    @Override
    public SystemTask getTask(ModelProject project) {
        // Load view from AlignerParameters.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AlignerParameters.fxml"),
                ExomeSuite.getResources());
        try {
            loader.load();
        } catch (IOException ex) {
            MainViewController.printException(ex);
            return null;
        }

        // Set suggestions.
        AlignerParameters controller = loader.getController();
        File forward = project.getForwardSequences();
        File reverse = project.getReverseSequences();
        String encoding = project.getEncoding();
        String reference = project.getGenomeCode();
        File path = project.getConfigFile().getParentFile();
        String output = new File(path, project.getCode() + ".bam").getAbsolutePath();
        controller.setEncodingOptions(OS.getEncodings());
        controller.setReferenceOptions(OS.getReferenceGenomes());
        controller.setEncoding(encoding);
        controller.setReference(reference);
        controller.setForward(forward.getAbsolutePath());
        controller.setReverse(reverse.getAbsolutePath());
        controller.setOutput(output);

        // Prepare enclosure for root view.
        Stage stage = new Stage();
        Scene scene = new Scene(loader.getRoot());
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
            String selectedForward = controller.getSelectedForward();
            if (!FileManager.tripleCheck(selectedForward)) {
                errors.add(ExomeSuite.getResources().getString("forward.sequences"));
            }
            String selectedReverse = controller.getSelectedReverse();
            if (!FileManager.tripleCheck(selectedReverse)) {
                errors.add(ExomeSuite.getResources().getString("reverse.sequences"));
            }
            String selectedEncoding = controller.getEncoding();
            String selectedReference = controller.getReference();
            String selectedOutput = controller.getOutput();
            String temp = OS.getTempDir();
            String genome = OS.getProperties().getProperty(selectedReference);
            String dbsnp = OS.getProperties().getProperty("dbsnp");
            String mills = OS.getProperties().getProperty("mills");
            String phase1 = OS.getProperties().getProperty("phase1");
            String code = project.getCode();
            boolean phred64 = selectedEncoding.equals("phred+64");
            boolean refine = selectedReference.equalsIgnoreCase("grch37");
            // Check that parameters are ok.
            if (refine) {
                if (!FileManager.tripleCheck(dbsnp)) {
                    errors.add(ExomeSuite.getResources().getString("dbsnp"));
                }
                if (!FileManager.tripleCheck(mills)) {
                    errors.add(ExomeSuite.getResources().getString("mills"));
                }
                if (!FileManager.tripleCheck(phase1)) {
                    errors.add(ExomeSuite.getResources().getString("phase1"));
                }
            }
            if (!FileManager.tripleCheck(genome)) {
                errors.add(ExomeSuite.getResources().getString("reference.genome"));
            }
            if (!FileManager.tripleCheck(temp)) {
                errors.add(ExomeSuite.getResources().getString("temp.path"));
            }
            if (!errors.isEmpty()) {
                String message = ExomeSuite.getResources().getString("missing.arguments") + ":\n["
                        + OS.asString(",", errors) + "]";
                MainViewController.printMessage(message, "warning");
                return null;
            }
            SystemTask aligner = new Aligner(temp, selectedForward, selectedReverse, genome,
                    dbsnp, mills, phase1, selectedOutput, code, phred64, refine);
            aligner.stateProperty().addListener((obs, old, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    project.getFiles().add(new File(selectedOutput));
                    //project.addExtraFile(output);
                }
            });
            return aligner;
        }

        return null;
    }

}
