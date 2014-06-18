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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Console {

    private Node view;

    private PrintStream printStream;

    private ConsoleController controller;

    public Console() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Console.fxml"));
            loader.load();
            view = loader.getRoot();
            controller = loader.getController();
            printStream = new PrintStream(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    byte[] c = {(byte) b};
                    final String character = new String(c, Charset.defaultCharset());
                    controller.addText(character);
                }
            });
        } catch (IOException ex) {
            Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Node getView() {
        return view;
    }

    public void addText(String text) {
        controller.addText(text);
    }

    public void clear() {
        controller.clear();
    }

    public PrintStream getPrintStream() {
        return printStream;
    }

}
