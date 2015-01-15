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

import exomesuite.utils.Software;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Main launcher of the app. Creates the main window and launches it.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class ExomeSuite extends Application {

    private static Stage mainStage;
    private static MainViewController controller;
    private static ResourceBundle resources;

    @Override
    public void start(Stage stage) throws Exception {
        resources = ResourceBundle.getBundle("exomesuite.language.ExomeSuite");
        testVars();
        testSoftware();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"), resources);
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Exome Suite");
        stage.setOnCloseRequest(event -> exit(event));
        mainStage = stage;
        stage.getIcons().add(new Image(ExomeSuite.class.getResourceAsStream("img/exomesuite.png")));
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Gets the main Stage. Use it to block events when another window is shown or to exit from
     * application.
     *
     * @return the main stage
     */
    public static Stage getMainStage() {
        return mainStage;
    }

    /**
     * Prints System params
     */
    private void testVars() {
        final String[] ps = new String[]{
            "java.class.path",
            "java.home",
            "java.vendor",
            "java.vendor.url",
            "java.version",
            "os.arch",
            "os.name",
            "os.version",
            // "line.separator",
            // "file.separator",
            // "path.separator",
            "user.dir",
            "user.home",
            "user.name"
        };
        for (String p : ps) {
            System.out.println(p + " = " + System.getProperty(p));
        }
    }

    private void testSoftware() {
        System.out.print(resources.getString("check.samtools"));
        System.out.println(Software.isSamtoolsInstalled());
        System.out.print(resources.getString("check.bwa"));
        System.out.println(Software.isSamtoolsInstalled());
        System.out.print(resources.getString("check.gatk"));
        System.out.println(Software.isGatkInstalled());
//        GenomeIndexer.index(new File("/home/unidad03/DNA_Sequencing/HomoSapiensGRCh38/genome.fasta"));
    }

    private void exit(WindowEvent event) {
        controller.exitApplication();
        event.consume();
    }

    /**
     * Gets the controller of the main view, which performs the most commom operations.
     *
     * @return the controller of the main view
     */
    public static MainViewController getController() {
        return controller;
    }

    /**
     * Gets the current ResourceBundle.
     *
     * @return the current resource bundle.
     */
    public static ResourceBundle getResources() {
        return resources;
    }

    /**
     * Gets the current locale.
     *
     * @return the current locale.
     */
    public static Locale getLocale() {
        return resources.getLocale();
    }

    /**
     * Changes the current system locale.
     *
     * @param locale
     */
    public static void changeLocale(Locale locale) {
        resources = ResourceBundle.getBundle("exomesuite.language.ExomeSuite", locale);
        FXMLLoader loader = new FXMLLoader(MainViewController.class.getResource("MainView.fxml"), resources);
        try {
            loader.load();
            mainStage.setScene(new Scene(loader.getRoot()));
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Formats the String key from resources (the current ResourceBundle) using a MessageFormat
     * which will have args.
     *
     * @param key the key of the string
     * @param args the arguments of the string
     * @return the resulting string using the current locale
     */
    public static String getStringFormatted(String key, Object... args) {
        MessageFormat formatter = new MessageFormat(resources.getString(key), resources.getLocale());
        return formatter.format(args);
    }

}
