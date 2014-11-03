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

/**
 * Stores a variant.
 *
 * @author Pascual Lorente Arencibia
 */
public class Variant2 {

    private final String chrom, id, ref, alt, filter, info, format;
    private final int pos;
    private final double qual;
    private final String[] samples;

    /**
     * Parses the line and creates a Variant2.
     *
     * @param line the line to parse
     */
    public Variant2(String line) {
        final String[] v = line.split("\t");
        chrom = v[0];
        pos = Integer.valueOf(v[1]);
        id = v[2];
        ref = v[3];
        alt = v[4];
        qual = Double.valueOf(v[5]);
        filter = v[6];
        info = v[7];
        if (v.length > 8) {
            format = v[8];
            final int nSamples = v.length - 9;
            samples = new String[nSamples];
            for (int i = 0; i < nSamples; i++) {
                samples[i] = v[9 + i];
            }
        } else {
            format = null;
            samples = null;
        }
    }

    public String getChrom() {
        return chrom;
    }

    public String getId() {
        return id;
    }

    public String getRef() {
        return ref;
    }

    public String getAlt() {
        return alt;
    }

    public String getFilter() {
        return filter;
    }

    public String getInfo() {
        return info;
    }

    public String getFormat() {
        return format;
    }

    public int getPos() {
        return pos;
    }

    public double getQual() {
        return qual;
    }

    public String[] getSamples() {
        return samples;
    }

}
