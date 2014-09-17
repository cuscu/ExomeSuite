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

import exomesuite.project.Project;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

/**
 * Shows information about a project. This class may substitute ProjectProperties.
 *
 * @author Pascual Lorente Arencibia
 */
public class ProjectInfo extends VBox {

    @FXML
    private FileSelector forward;
    @FXML
    private FileSelector reverse;
    @FXML
    private TextField name;
    @FXML
    private Label code;
    @FXML
    private TextArea description;
    @FXML
    private FileSelector path;

    private Project project;

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
            project.setProperty(Project.PropertyName.FORWARD_FASTQ, forward.getFile());
        });
        reverse.setOnFileChange((EventHandler) (Event event) -> {
            project.setProperty(Project.PropertyName.REVERSE_FASTQ, reverse.getFile());
        });
        name.setOnKeyTyped((KeyEvent event) -> {
            project.setProperty(Project.PropertyName.NAME, name.getText());
        });
        description.setOnKeyTyped((KeyEvent event) -> {
            project.setProperty(Project.PropertyName.DESCRIPTION, description.getText());
        });
        path.setOnFileChange((EventHandler) (Event event) -> changePath());
        path.setDisable(true);
    }

    public void setProject(Project project) {
        setVisible(true);
        this.project = project;
        forward.setFile(project.getProperty(Project.PropertyName.FORWARD_FASTQ, ""));
        reverse.setFile(project.getProperty(Project.PropertyName.REVERSE_FASTQ, ""));
        name.setText(project.getProperty(Project.PropertyName.NAME, ""));
        code.setText(project.getProperty(Project.PropertyName.CODE, ""));
        description.setText(project.getProperty(Project.PropertyName.DESCRIPTION, ""));
        path.setFile(project.getProperty(Project.PropertyName.PATH, ""));
    }

    private void changePath() {
        final Path newPath = new File(path.getFile()).toPath();
        final Path prevPath = new File(project.getProperty(Project.PropertyName.PATH)).toPath();

        try {
            Files.walkFileTree(prevPath, new FileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes bfa) throws IOException {
                    Files.copy(dir, newPath.resolve(prevPath.relativize(dir)));
                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes bfa) throws IOException {
                    Files.copy(file, newPath.resolve(prevPath.relativize(file)), COPY_ATTRIBUTES);
                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
                    System.err.println("Copy failed: " + t);
                    return CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
                    return CONTINUE;
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(ProjectInfo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}