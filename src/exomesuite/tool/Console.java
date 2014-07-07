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
package exomesuite.tool;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Console {

    private final ScrollPane view;

    private final PrintStream printStream;

    public Console() {
        TextArea area = new TextArea();
        area.setMaxHeight(Double.MAX_VALUE);
        area.setWrapText(false);
        area.setEditable(false);
        view = new ScrollPane(area);
        view.setFitToHeight(true);
        view.setFitToWidth(true);
        view.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(view, Priority.ALWAYS);
        printStream = new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                byte[] c = {(byte) b};
                final String character = new String(c, Charset.defaultCharset());
                Platform.runLater(() -> {
                    area.appendText(character);
                });
            }
        });
    }

    public Node getView() {
        return view;
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

}
