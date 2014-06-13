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
package exomesuite;

import exomesuite.tool.align.AlignTool;
import exomesuite.tool.sequences.SequenceTool;
import exomesuite.utils.Config;
import java.io.File;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Project {

    public final static String FORWARD = "forward_sequence";
    public final static String REVERSE = "reverse_sequence";
    public final static String GENOME = "reference_genome";
    public final static String DBSNP = "dbsnp";
    public final static String MILLS = "mills";
    public final static String PHASE1 = "phase1";
    public final static String PATH_ALIGNMENT = "alignments";
    public final static String PATH_TEMP = "temp";

    private String name;
    private File path;
    private final VBox toolsPane;
    private final Config config;

    Project(String name, File parent) {
        path = new File(parent, name);
        path.mkdirs();
        config = new Config(new File(path, name + ".config"));
        toolsPane = new VBox(5);
        this.name = name;
        this.path = new File(parent, name);
        path.mkdirs();
        loadTools();
    }

    private void loadTools() {
        toolsPane.getChildren().add(new SequenceTool(this).getTool().getView());
        toolsPane.getChildren().add(new AlignTool(this).getTool().getView());
    }

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VBox getToolsPane() {
        return toolsPane;
    }

    public Config getConfig() {
        return config;
    }

}
