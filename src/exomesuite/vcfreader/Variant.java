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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stores a single variant.
 *
 * @author Pascual Lorente Arencibia.
 */
public class Variant {

    private final VariantCallFormat vcf;
    private final String chrom, format;
    private String id, ref, alt, filter, info;
    private final int pos;
    private final double qual;
    private final List<String> sampleGenotypes;
    private Map<String, List<String>> infoValues;
    private List<String> ids;
//    private final List<String> format;

    Variant(VariantCallFormat vcf, String line) {
        this.vcf = vcf;
        String[] row = line.split("\t");
        chrom = row[0];
        pos = Integer.valueOf(row[1]);
        id = row[2];
        ref = row[3];
        alt = row[4];
//                String qual = row[5]; r u gonna use the double value?
        qual = Double.valueOf(row[5]);
        filter = row[6];
        info = row[7];
        if (vcf.getSampleNames().size() > 0) {
            sampleGenotypes = new ArrayList<>();
            format = row[8];
            for (int i = 0; i < vcf.getSampleNames().size(); i++) {
                sampleGenotypes.add(row[9 + i]);
            }
        } else {
            format = null;
            sampleGenotypes = null;
        }
    }

    public String getChrom() {
        return chrom;
    }

    public int getPos() {
        return pos;
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

    public double getQual() {
        return qual;
    }

    public String getFilter() {
        return filter;
    }

    public void addFilter(String s) {
        if (!s.equals(".")) {
            if (filter.equals(".")) {
                filter = s;
            } else {
                if (!filter.contains(s)) {
                    filter += "," + s;
                }
            }
        }
    }

    public void addID(String id) {
        if (!id.equals(".")) {
            if (this.id.equals(".")) {
                this.id = id;
            } else {
                if (!this.id.contains(id)) {
                    this.id += "," + id;
                }
            }
        }
    }

    /**
     * Creates a Map with keys and list of values, representing the info field.
     *
     * @param info
     * @return
     */
    private Map<String, List<String>> parseinfoValues(String info) {
        TreeMap<String, List<String>> infos = new TreeMap<>();
        String[] is = info.split(";");
        for (String data : is) {
            List<String> values = new ArrayList<>();
            String[] pair = data.split("=");
            if (pair.length > 1) {
                String[] vs = pair[1].split(",");
                values.addAll(Arrays.asList(vs));
            }
            infos.put(pair[0], values);
        }
        return infos;
    }

    /**
     * Gets the info field parsed. Both keys and values are represented as Strings, so it is
     * necessary to convert them to their proper types.
     * <p>
     * Map map = getInfoValues();
     * <p>
     * int dp = Integer.valueOf(map.get("DP"));
     * <p>
     * boolean hapmap2 = map.containsKey("H2");
     *
     *
     * @return a map with the info values.
     */
    public Map<String, List<String>> getInfoValues() {
        if (infoValues == null) {
            infoValues = parseinfoValues(info);
        }
        return infoValues;
    }

    public String getFormat() {
        return format;
    }

    public List<String> getSampleGenotypes() {
        return sampleGenotypes;
    }

    @Override
    public String toString() {
//        // INFO field
//        getInfoValues();
//        List<String> infosValues = new ArrayList<>();
//        infoValues.entrySet().forEach((Map.Entry<String, List<String>> t) -> {
//            String key = t.getKey();
//            List<String> list = t.getValue();
//            if (!list.isEmpty()) {
//                key += "=" + asString(",", list);
//            }
//            infosValues.add(key);
//        });
//        String i = asString(";", infosValues);
//        String ret = String.
//                format("CHROM=%s;POS=%d;ID=%s;REF=%s;ALT=%s;QUAL=%.2f;FILTER=%s;INFO=%s",
//                        chrom, pos, id, ref, alt, qual, filter, i);
//        // FORMAT
//        if (format != null) {
//            ret += "FORMAT=" + asString(";", format) + ";";
//            ret += asString(";", sampleGenotypes);
//        }
        String ret = chrom + "\t" + pos + "\t" + id + "\t" + ref + "\t" + alt + "\t" + qual + "\t"
                + filter + "\t" + info;
        if (!sampleGenotypes.isEmpty()) {
            ret += "\t" + format + "\t" + asString("\t", sampleGenotypes);
        }
        return ret;
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] -> value1,value2,value3
     *
     * @param separator
     * @param values
     * @return
     */
    private String asString(String separator, String[] values) {
        if (values.length == 0) {
            return "";
        }
        String s = values[0];
        int i = 1;
        while (i < values.length) {
            s += separator + values[i++];
        }
        return s;
    }

    /**
     * Converts an Array to String using the separator. Omits the last separator. [value1 value2
     * value3] -> value1,value2,value3
     *
     * @param separator
     * @param values
     * @return
     */
    private String asString(String separator, List<String> values) {
        String s = "";
        int i = 0;
        while (i < values.size() - 1) {
            s += values.get(i++) + separator;
        }
        return s + values.get(i);
    }

//    /**
//     * Will mix this variant with v if, and only if, both variants have the same coordinate.
//     *
//     * @param v
//     * @return a variant that is the combination of this variant and v
//     */
//    private Variant combine(Variant v) {
//        if (compare(v) == 0) {
//            String nId = computeIds(this, v);
//        }
//        return null;
//    }
    /**
     * Compares both variants coordinates. To compare contigs, the method will use
     * <code>this.getVcf().getContigs()</code> and search for both variants.
     *
     * @param v the variant to compare with this
     * @return 0 if at same coordinate. -1 if v &lt; this, 1 if v &gt; this.
     */
    public int compare(Variant v) {
        if (v.getChrom().equals(chrom)) {
            if (v.getPos() == pos) {
                return 0;
            } else if (v.getPos() < pos) {
                return -1;
            } else if (v.getPos() > pos) {
                return 1;
            }
        } else {
            int a = vcf.getContigs().indexOf(chrom);
            int b = vcf.getContigs().indexOf(v.getChrom());
            if (a < b) {
                return 1;
            } else {
                return -1;
            }
        }
        return -1;
    }

    public VariantCallFormat getVcf() {
        return vcf;
    }

}
