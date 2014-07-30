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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
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
    private final static String MIST = "Mist";
    private final static String UNKNOWN = "Unknown";

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
        File o = controller.getOutput();
        if (o == null) {
            return;
        }
        Task<Void> task = new Task() {

            @Override
            protected Void call() throws Exception {
                intersect(v1, v2, e1, e2, o);
                return null;
            }
        };
        Platform.runLater(task);
    }

    /*
     Cases    :     1        2           3           4           5
     Sample 1 : ----v-----mmmvmmm-----mmmmmmm-----mmmvmmm-----mmmmmmm-----
     Sample 2 : ----v--------v-----------v--------mmmvmmm-----mmmvmmm-----
     - : exon
     v : variant
     m : mist region
     1 : Variant with best quality.
     2 : Variant from Sample 2 added, mist tag.
     3 : Variant from Sample 2 added, unknown tag.
     4 : No variant added.
     5 : No variant added.
     */
    private void intersect(File variants1, File variants2, File mist1, File mist2, File output) {
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
        try (PrintStream out = new PrintStream(output)) {
            vcf1.getHeaders().forEach(out::println);
            while (v1 != null && v2 != null) {
                switch (v1.compare(v2)) {
                    case 0:
                        // M1: v1 in mist
                        // M2: v2 in mist.
                        //    M1 M2
                        // 0  -  - normal++; Case 1
                        // 1  -  x normal++; Case 2
                        // 2  x  - normal++; Case 2
                        // 3  x  x           Case 4
                        if (mistr1.contains(v1)) {
                            if (!mistr2.contains(v2)) {
                                // 2: v1 in MIST and v2 good.
                                v2.addFilter(MIST);
                                v2.addFilter(v1.getFilter());
                                v2.addID(v1.getId());
                                out.println(v2);
                                normal++;
                            } // 3: Both in MIST region, discarded.
                        } else {
                            if (mistr2.contains(v2)) {
                                // 1: v2 in MIST and v1 good
                                v1.addFilter(MIST);
                                v1.addFilter(v2.getFilter());
                                v1.addID(v2.getId());
                                out.println(v1);
                            } else {
                                // 0: Both variants good.
                                if (v1.getQual() > v2.getQual()) {
                                    v1.addID(v2.getId());
                                    v1.addFilter(v2.getFilter());
                                    out.println(v1);
                                } else {
                                    v2.addID(v1.getId());
                                    v2.addFilter(v1.getFilter());
                                    out.println(v2);
                                }
                            }
                            normal++;
                        }
                        v1 = vcf1.nextVariant();
                        v2 = vcf2.nextVariant();
                        cv1++;
                        cv2++;
                        break;
                    // v2 > v1
                    case 1:
                        if (mistr2.contains(v1) && !mistr1.contains(v1)) {
                            // Case 3
                            Variant v = v1;
                            v.addFilter(UNKNOWN);
                            out.println(v);
                            m1++;
                        }
                        v1 = vcf1.nextVariant();
                        cv1++;
                        break;
                    // v2 < v1
                    case -1:
                        if (mistr1.contains(v2) && !mistr2.contains(v2)) {
                            // Case 3
                            Variant v = v2;
                            v.addFilter(UNKNOWN);
                            out.println(v);
                            m2++;
                        }
                        v2 = vcf2.nextVariant();
                        cv2++;
                        break;
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CombineVariants.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(
                "Variants in first:" + cv1);
        System.out.println(
                "Variants in second:" + cv2);
        System.out.println(
                "Normal intersections:" + normal);
        System.out.println(
                "First but not second:" + m1);
        System.out.println(
                "Second but not first:" + m2);
    }

    /**
     * Returns a new Variant combining data form this two variants. Both must be at the same
     * coordinate.
     *
     * @param v1
     * @param v2
     * @return
     */
    private Variant merge(Variant v1, Variant v2) {
        String chrom = v1.getChrom();
        int pos = v1.getPos();
        List<String> refs = new ArrayList<>();
        return v1.getQual() > v2.getQual() ? v1 : v2;
    }
}
