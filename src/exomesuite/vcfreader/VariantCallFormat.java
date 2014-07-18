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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores a VCF file.
 *
 * @author Pascual Lorente Arencibia.
 */
public class VariantCallFormat {

    private BufferedReader input;

    private final List<String> headers;
    private final List<String> sampleNames;
    private final List<String> contigs;

    public VariantCallFormat(File file) {
        headers = new ArrayList<>();
        sampleNames = new ArrayList<>();
        contigs = new ArrayList<>();
        try {
            String line;
            input = new BufferedReader(new FileReader(file));
            while ((line = input.readLine()) != null && line.startsWith("##")) {
                headers.add(line);
                // Capture contigs
                if (line.startsWith("##contig=")) {
                    String values = line.replace("##contig=", "").replace("<", "").replace(">", "");
                    String[] ts = values.split(",");
                    for (String t : ts) {
                        if (t.startsWith("ID=")) {
                            contigs.add(t.replace("ID=", ""));
                            break;
                        }
                    }
                }
            }
            // Will also add #CHROM... line
            headers.add(line);
            // 8 fixed fields and 1 FORMAT column = 9;
            // if no sampleNames present sampleNames = -1;
            String[] headerLine = line.split("\t");
            if (headerLine.length > 8) {
                for (int i = 9; i < headerLine.length; i++) {
                    sampleNames.add(headerLine[i]);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VariantCallFormat.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VariantCallFormat.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Variant nextVariant() {
        String line;
        try {
            if ((line = input.readLine()) != null) {
                return new Variant(this, line);
            } else {
                return null;
            }
        } catch (IOException ex) {
            return null;
        }
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String> getSampleNames() {
        return sampleNames;
    }

    public List<String> getContigs() {
        return contigs;
    }

}
