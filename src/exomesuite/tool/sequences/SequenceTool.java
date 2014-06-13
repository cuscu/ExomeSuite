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
package exomesuite.tool.sequences;

import exomesuite.Project;
import exomesuite.tool.ToolPane;
import exomesuite.utils.Config;
import exomesuite.utils.OS;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * This tool let the user select the FASTQ sequences. Place it with getView.
 *
 * @author Pascual Lorente Arencibia
 */
public class SequenceTool {

    private final ToolPane tool = new ToolPane("Sequences", ToolPane.Status.RED);
    private final Project project;
    private final Config config;
    private File forward, reverse;
    private final Button acceptButton;
    private final TextField forwardTF, reverseTF;

    public SequenceTool(Project project) {
        this.project = project;
        this.config = project.getConfig();
        // Create buttons.
        Button openButton = new Button(null, new ImageView("exomesuite/img/rd_arrow.png"));
        openButton.setOnAction((ActionEvent event) -> {
            showSelectPanel();
        });
        acceptButton = new Button(null, new ImageView("exomesuite/img/accept.png"));
        acceptButton.setOnAction((ActionEvent event) -> {
            accept();
        });
        acceptButton.setDisable(true);
        tool.addButton(ToolPane.Status.RED, openButton);
        tool.addButton(ToolPane.Status.GREEN, openButton);
        tool.addButton(ToolPane.Status.OPEN, acceptButton);
        // Create panel.
        forwardTF = new TextField();
        forwardTF.setPromptText("Forward sequences (FASTQ file)");
        forwardTF.setOnMouseClicked((MouseEvent event) -> {
            openForward();
        });
        forwardTF.setOnAction((ActionEvent event) -> {
            openForward();
        });
        reverseTF = new TextField();
        reverseTF.setPromptText("Reverse sequences (FASTQ file)");
        reverseTF.setOnAction((ActionEvent event) -> {
            openReverse();
        });
        reverseTF.setOnMouseClicked((MouseEvent event) -> {
            openReverse();
        });
        // Check if the files have already been selected.
        String fw = config.getProperty(Project.FORWARD);
        if (fw != null) {
            forwardTF.setText(fw);
            forward = new File(fw);
        }
        String rv = config.getProperty(Project.REVERSE);
        if (rv != null) {
            reverseTF.setText(rv);
            reverse = new File(rv);
        }
        if (fw != null && rv != null) {
            tool.setStatus(ToolPane.Status.GREEN);
            acceptButton.setDisable(false);
        }
    }

    private void showSelectPanel() {
        VBox box = new VBox(forwardTF, reverseTF);
        tool.showPane(box);
        tool.setStatus(ToolPane.Status.OPEN);
    }

    private void sequencesSelected() {
        acceptButton.setDisable(false);
    }

    private void accept() {
        config.setProperty(Project.FORWARD, forward.getAbsolutePath());
        config.setProperty(Project.REVERSE, reverse.getAbsolutePath());
        tool.hidePane();
        tool.setStatus(ToolPane.Status.GREEN);
    }

    public ToolPane getTool() {
        return tool;
    }

    private void openForward() {
        forward = OS.openFASTQ(forwardTF);
        if (forward != null && reverse != null) {
            sequencesSelected();
        }
    }

    private void openReverse() {
        reverse = OS.openFASTQ(reverseTF);
        if (forward != null && reverse != null) {
            sequencesSelected();
        }
    }

}
