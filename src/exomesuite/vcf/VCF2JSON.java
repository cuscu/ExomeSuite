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

import exomesuite.MainViewController;
import exomesuite.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class VCF2JSON {

    public static void vcf2Json(File vcfFile, File output) {
        JSONObject json = new JSONObject();
        try (BufferedReader reader = new BufferedReader(new FileReader(vcfFile));
                FileWriter writer = new FileWriter(output)) {
            AtomicReference<String[]> header = new AtomicReference<>();
            reader.lines().forEach(line -> {
                if (line.startsWith("##fileformat=")) {
                    String value = line.substring(13);
                    json.accumulate("fileformat", value);
                } else if (line.startsWith("##FILTER=")) {
                    String value = line.substring(9);
                    json.accumulate("filters", value);
                } else if (line.startsWith("##FORMAT=")) {
                    String value = line.substring(9);
                    json.accumulate("formats", value);
                } else if (line.startsWith("##INFO=")) {
                    String value = line.substring(7);
                    json.accumulate("infos", value);
                } else if (line.startsWith("##")) {
                    int nextEq = line.indexOf("=");
                    String name = line.substring(2, nextEq);
                    String value = line.substring(nextEq + 1);
                    json.accumulate(name, value);
                } else if (line.startsWith("#CHROM")) {
                    header.set(line.substring(1).split("\t"));
                } else {
//                    storevariant(json, line, header.get());
                    json.accumulate("variants", parseVariant(line, header.get()));
                }
            });
//            json.write(writer);
            writer.write(json.toString(2));
//            writer.write(json.toString());
        } catch (FileNotFoundException ex) {
            MainViewController.printException(ex);
        } catch (IOException ex) {
            MainViewController.printException(ex);
        }
    }

    private static JSONObject parseVariant(String line, String[] header) {
        JSONObject variant = new JSONObject();
        String[] values = line.split("\t");
        for (int i = 0; i < values.length; i++) {
            variant.accumulate(header[i], values[i]);
        }
        return variant;
    }

    private static void storevariant(JSONObject json, String line, String[] header) {
        JSONObject variant = new JSONObject();
        String[] values = line.split("\t");
        for (int i = 1; i < values.length; i++) {
            variant.accumulate(header[i], values[i]);
        }
        String chr = values[0];
        JSONObject vars;
        if (json.has("variants")) {
            vars = json.getJSONObject("variants");
            vars.accumulate(chr, variant);
        } else {
            JSONObject c = new JSONObject();
            c.accumulate(chr, variant);
            json.put("variants", c);
        }
    }
}
