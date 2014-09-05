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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;

/**
 * An hbox which contains buttons that perform actions.
 *
 * @author Pascual Lorente Arencibia
 */
public class ProjectActions extends HBox {

    private ProjectData project;

    private final List<Action> actions = new ArrayList<>();

    public ProjectActions() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ProjectActions.fxml"));
        loader.setController(this);
        loader.setRoot(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(ProjectActions.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    public void initialize() {
        Action align = new Action("align.png", "Align genome", "Select FASTQ files first") {

            @Override
            public boolean isDisabled(ProjectData project) {
                return !project.contains(ProjectData.PropertyName.FORWARD_FASTQ)
                        || !project.contains(ProjectData.PropertyName.REVERSE_FASTQ);
            }
        };

        Action call = new Action("call.png", "Call variants", "Align genome first") {

            @Override
            public boolean isDisabled(ProjectData project) {
                return !project.contains(ProjectData.PropertyName.BAM_FILE);
            }
        };
        boolean add = actions.add(call);
    }

    public void setProject(ProjectData project) {
        this.project = project;
        getChildren().clear();
        actions.forEach((Action a) -> {
            FlatButton fb = new FlatButton(a.icon, a.isDisabled(project) ? a.disableDescription
                    : a.description);
            fb.setDisable(a.isDisabled(project));
            getChildren().add(fb);
        });
    }

    private abstract class Action {

        String icon, description, disableDescription;
        boolean running;

        public Action(String icon, String description, String disableDescription) {
            this.icon = icon;
            this.description = description;
            this.disableDescription = disableDescription;
            this.running = false;
        }

        public abstract boolean isDisabled(ProjectData project);

    }
}
