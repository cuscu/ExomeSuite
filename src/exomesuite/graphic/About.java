/*
 * Copyright (C) 2014 unidad03
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

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * FXML Controller class
 *
 * @author Pascual Lorente Arencibia
 */
public class About {

    @FXML
    private WebView webView;

    private WebEngine engine;

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        engine = webView.getEngine();
        engine.loadContent("<div>Icons made by Icons8 from <a href=\"http://www.flaticon.com\""
                + " title=\"Flaticon\">www.flaticon.com</a>"
                + " is licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\""
                + " title=\"Creative Commons BY 3.0\">CC BY 3.0</a></div>"
                + "<div>Icon made by <a href=\"http://www.freepik.com\" title=\"Freepik\">Freepik</a>"
                + " from <a href=\"http://www.flaticon.com\" title=\"Flaticon\">www.flaticon.com</a>"
                + " is licensed under <a href=\"http://creativecommons.org/licenses/by/3.0/\""
                + " title=\"Creative Commons BY 3.0\">CC BY 3.0</a></div>");
    }

}
