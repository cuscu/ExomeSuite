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
package exomesuite.actions.mist;

import exomesuite.ExomeSuite;
import exomesuite.MainViewController;
import exomesuite.actions.LongAction;
import exomesuite.actions.SystemTask;
import exomesuite.project.ModelProject;
import exomesuite.utils.FileManager;
import exomesuite.utils.OS;
import exomesuite.utils.Software;
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
public class MistLongAction extends LongAction {

    @Override
    public String getName() {
        return ExomeSuite.getResources().getString("mist");
    }

    @Override
    public String getIconPath() {
        return "exomesuite/img/mist.png";
    }

    @Override
    public boolean isDisable(ModelProject project) {
        if (project == null) {
            return true;
        }
        if (!Software.isSamtoolsInstalled()) {
            // Too much calls to this method, use only at start
            MainViewController.printMessage(
                    ExomeSuite.getResources().getString("samtools.not.installed"), "warning");
            return true;
        }
        return project.getFiles().stream().noneMatch(file -> (file.getName().endsWith(".bam")));
    }

    @Override
    public SystemTask getTask(ModelProject project) {
        // Prepare parameters to the parameters window.
        List<String> bams = new ArrayList();
        project.getFiles().forEach(file -> {
            if (file.getName().endsWith(".bam")) {
                bams.add(file.getAbsolutePath());
            }
        });
        // Load view from MistParameters.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MistParameters.fxml"),
                ExomeSuite.getResources());
        try {
            loader.load();
        } catch (IOException ex) {
            MainViewController.printException(ex);
            return null;
        }
        MistParameters controller = loader.getController();
        // Prepare enclosure for root view.
        Stage stage = new Stage();
        Scene scene = new Scene(loader.getRoot());
        // path/code.vcf
        File path = project.getConfigFile().getParentFile();
        String output = new File(path, project.getCode() + ".mist").getAbsolutePath();

        controller.setAlignmentsOptions(bams);
        controller.setAlignments(bams.get(0));
        controller.setThreshold(10);
        controller.setLength(1);
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
            String selectedOutput = controller.getOutput();
            String selectedAlignments = controller.getSelectedAlignments();
            int selectedLength = controller.getSelectedLength();
            int selectedThreshold = controller.getSelectedThreshold();
            String ensembl = OS.getProperties().getProperty("ensembl");

            List<String> errors = new ArrayList();
            if (!FileManager.tripleCheck(selectedAlignments)) {
                errors.add(ExomeSuite.getResources().getString("alignments"));
            }
            if (!FileManager.tripleCheck(ensembl)) {
                errors.add(ExomeSuite.getResources().getString("ensembl"));
            }
            if (!errors.isEmpty()) {
                String message = ExomeSuite.getResources().getString("missing.arguments") + "\n"
                        + ": [" + OS.asString(",", errors) + "]";
                MainViewController.printMessage(message, "warning");
                return null;
            }
            Mist task = new Mist(selectedAlignments, selectedOutput, ensembl, selectedThreshold, selectedLength);
            task.stateProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == Worker.State.SUCCEEDED) {
                    project.getFiles().add(new File(selectedOutput));
//                    project.addExtraFile(selectedOutput);
                }
            });
            return task;
        }
        return null;
    }
}
