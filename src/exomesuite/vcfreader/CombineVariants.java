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
package exomesuite.vcfreader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class CombineVariants {

    private VBox view;
    private CombineController controller;

    public CombineVariants() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Combine.fxml"));
            view = loader.load();
            controller = loader.getController();
        } catch (IOException ex) {
            Logger.getLogger(CombineVariants.class.getName()).log(Level.SEVERE, null, ex);
        }
        controller.getCombineButton().setOnAction((ActionEvent event) -> start());
    }

    public void show() {
        Stage stage = new Stage();
        Scene scene = new Scene(view);
        stage.setTitle("Combine variants");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public final void start() {
        File v1 = controller.getVariants1();
        if (v1 == null) {
            return;
        }
        File v2 = controller.getVariants2();
        if (v2 == null) {
            return;
        }
        File e1 = controller.getExons1();
        if (e1 == null) {
            return;
        }
        File e2 = controller.getExons2();
        if (e2 == null) {
            return;
        }
        Task<Void> task = new Task() {

            @Override
            protected Void call() throws Exception {
                intersect(v1, v2, e1, e2);
                return null;
            }
        };
        Platform.runLater(task);
    }

    private void intersect(File variants1, File variants2, File mist1, File mist2) {
        int cv1 = 1;
        int cv2 = 1;
        int normal = 0;
        int m1 = 0;
        int m2 = 0;
        VariantCallFormat vcf1 = new VariantCallFormat(variants1);
        VariantCallFormat vcf2 = new VariantCallFormat(variants2);
        MISTReader mistr1 = new MISTReader(mist1);
        MISTReader mistr2 = new MISTReader(mist2);
        Variant v1 = vcf1.nextVariant();
        Variant v2 = vcf2.nextVariant();
        while (v1 != null && v2 != null) {
            int r = v1.compare(v2);
            if (r == 0) {
                // M1 M2
                // -  - normal++; No print
                // -  x normal++; Print
                // x  - normal++; Print
                // x  x No print
                if (mistr1.contains(v1)) {
                    if (!mistr2.contains(v2)) {
                        System.out.println("Match");
                        System.out.println("[MIST] " + v1);
                        System.out.println(v2);
                        normal++;
                    }
                } else {
                    if (mistr2.contains(v2)) {
                        System.out.println("Match");
                        System.out.println(v1);
                        System.out.println("[MIST] " + v2);
                    }
                    normal++;
                }
                v1 = vcf1.nextVariant();
                v2 = vcf2.nextVariant();
                cv1++;
                cv2++;
                // v2 > v1
            } else if (r > 0) {
//                if (mistr2.crossContains(v1) && !mistr1.contains(v1)) {
//                    System.out.println("Unknown");
//                    System.out.println(v1);
//                    System.out.println("[MIST] " + mistr2.getRegion(v1));
//                    m1++;
//                }
                v1 = vcf1.nextVariant();
                cv1++;
            } else {
//                if (mistr1.crossContains(v2) && !mistr2.contains(v2)) {
//                    System.out.println("Unknown");
//                    System.out.println("[MIST] " + mistr1.getRegion(v2));
//                    System.out.println(v2);
//                    m2++;
//                }
                v2 = vcf2.nextVariant();
                cv2++;
            }
        }
        System.out.println("Variants in first:" + cv1);
        System.out.println("Variants in second:" + cv2);
        System.out.println("Normal intersections:" + normal);
        System.out.println("First but not second:" + m1);
        System.out.println("Second but not first:" + m2);
    }
}
