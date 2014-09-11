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
package exomesuite.graphic;

import exomesuite.project.ProjectData;
import exomesuite.utils.OS;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class ProjectInfo extends VBox {

    @FXML
    private FileSelector forward;
    @FXML
    private FileSelector reverse;

    private ProjectData project;

    public ProjectInfo() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectInfo.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void initialize() {
        setVisible(false);
        forward.addExtensionFilter(OS.FASTQ_FILTER);
        reverse.addExtensionFilter(OS.FASTQ_FILTER);
        forward.setOnFileChange((EventHandler) (Event event) -> {
            project.setProperty(ProjectData.PropertyName.FORWARD_FASTQ, forward.getFile());
        });
        reverse.setOnFileChange((EventHandler) (Event event) -> {
            project.setProperty(ProjectData.PropertyName.REVERSE_FASTQ, reverse.getFile());

        });
    }

    public void setProject(ProjectData project) {
        setVisible(true);
        this.project = project;
        if (project.contains(ProjectData.PropertyName.FORWARD_FASTQ)) {
            forward.setFile(project.getProperty(ProjectData.PropertyName.FORWARD_FASTQ));
        }
        if (project.contains(ProjectData.PropertyName.REVERSE_FASTQ)) {
            reverse.setFile(project.getProperty(ProjectData.PropertyName.REVERSE_FASTQ));
        }
    }

}
