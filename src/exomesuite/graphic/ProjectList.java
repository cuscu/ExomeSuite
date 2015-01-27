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

import exomesuite.project.ModelProject;
import javafx.scene.control.ListView;

/**
 * The list of projects.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ProjectList extends ListView<ModelProject> {

    /**
     * Creates the list of projects.
     */
    public ProjectList() {
//        initialize();
    }

//    @FXML
//    private void initialize() {
//        // Cell factory
//        setEditable(false);
//        // The placeholder, when there are no projects opened.
//        String message = ExomeSuite.getResources().getString("no.projects");
//        // In order to show a resizable message, I use a FlowPane with individual words.
//        FlowPane placeholder = new FlowPane();
//        String[] words = message.split(" ");
//        for (String word : words) {
//            placeholder.getChildren().add(new Label(word + " "));
//        }
//        setPlaceholder(placeholder);
//        // Context menu
//        MenuItem close = new MenuItem(ExomeSuite.getResources().getString("close"),
//                new SizableImage("exomesuite/img/cancel.png", SizableImage.SMALL_SIZE));
//        MenuItem delete = new MenuItem(ExomeSuite.getResources().getString("delete"),
//                new SizableImage("exomesuite/img/delete.png", SizableImage.SMALL_SIZE));
//        close.setOnAction(event -> close(getSelectionModel().getSelectedItem()));
//        delete.setOnAction(event -> delete(getSelectionModel().getSelectedItem()));
//        final ContextMenu contextMenu = new ContextMenu(close, delete);
//        // Context menu is only shown when there are opened projects
//        //setContextMenu(contextMenu);
//        getItems().addListener((ListChangeListener.Change<? extends Project> c) -> {
//            setContextMenu(getItems().isEmpty() ? null : contextMenu);
//        });
//        // This cell listens for project changes to update the name.
//        setCellFactory(row -> new ProjectCell());
//    }
//
//    /**
//     * Removes from list the given project
//     *
//     * @param project the project to remove from list
//     */
//    private void close(Project project) {
//        if (project != null) {
//            getItems().remove(project);
//            OS.removeProject(project);
//        }
//    }
//
//    /**
//     * Deletes the selected project and asks user for removing the whole folder.
//     *
//     * @param project
//     */
//    private void delete(Project project) {
//        if (project != null) {
//            // Ask user to remove folder content
//            File path = new File(project.getProperties().getProperty(Project.PATH));
//            //File config = project.getConfigFile();
//            String title = ExomeSuite.getResources().getString("delete.project.title");
//            String message = ExomeSuite.getStringFormatted("delete.project.message", path.toString());
//            String yes = ExomeSuite.getResources().getString("delete.project.yes");
//            String no = ExomeSuite.getResources().getString("delete.project.no");
//            String cancel = ExomeSuite.getResources().getString("cancel");
//            Dialog.Response response = new Dialog().showYesNoCancel(title, message, yes, no, cancel);
//            switch (response) {
//                case YES:
//                    FileManager.delete(path, true);
//                case NO:
////                    config.delete();
//                    getItems().remove(project);
//                    OS.removeProject(project);
//            }
//        }
//    }
//
//    public void openProject(Project project) {
//        // Check if project is already opened
//        boolean opened = false;
//        for (Project p : getItems()) {
//            if (p.getProperties().getProperty(Project.CODE).equals(project.getProperties().getProperty(Project.CODE))) {
//                MainViewController.printMessage(ExomeSuite.getResources().getString("already.opened"), "warning");
//                opened = true;
//                break;
//            }
//        }
//        // If not, add to the list, the listener will be automatically added
//        if (!opened) {
//            getItems().add(project);
//            String message = ExomeSuite.getStringFormatted("project.opened",
//                    project.getProperties().getProperty(Project.NAME));
//            printMessage(message, "info");
//            OS.addProject(project);
//            getSelectionModel().select(project);
//        }
//    }
//
//    /**
//     * This cells listen for changes in the projects, so the name is updated whenever the name
//     * property is changed.
//     */
//    private static class ProjectCell extends ListCell<Project> implements Configuration.ConfigurationListener {
//
//        /**
//         * Associated project.
//         */
//        Project project;
//
//        @Override
//        protected void updateItem(Project item, boolean empty) {
//            super.updateItem(item, empty);
//            if (!empty) {
//                setText(item.toString());
//                // Update associated project, but before, remove from listener list of the previous
//                // project
//                if (project != null) {
//                    project.getProperties().removeListener(this);
//                }
//                item.getProperties().addListener(this);
//                project = item;
//            } else {
//                setText(null);
//            }
//        }
//
//        @Override
//        public void configurationChanged(Configuration configuration, String keyChanged) {
//            // Easy one
//            if (keyChanged.equals(Project.NAME)) {
//                setText(configuration.getProperty(keyChanged));
//            }
//        }
//
//    }
}
