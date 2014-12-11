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
package exomesuite.vcf;

import exomesuite.graphic.Param;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class VariantGenotype extends VBox implements VariantListener {

    @Override
    public void variantChanged(Variant variant, VCFHeader vcfHeader) {
        // format       genotype[]
        // GT:PL:GQ	0/1:41,0,5:10
        getChildren().clear();
        if (variant == null) {
            return;
        }
        final String[] format = variant.getFormat().split(":");
        for (String g : variant.getSamples()) {
            FlowPane box = new FlowPane();
            final String[] genotype = g.split(":");
            for (int i = 0; i < genotype.length; i++) {
                Param param = new Param();
                param.setTitle(format[i]);
                param.setValue(genotype[i]);
//                Label l = new Label(format[i] + "=" + genotype[i]);
//                l.getStyleClass().add("parameter");
                box.getChildren().add(param);
            }
            getChildren().add(box);
        }
    }

}
