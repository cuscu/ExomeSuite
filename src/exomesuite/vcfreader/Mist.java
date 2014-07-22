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
 *
 * @author Pascual Lorente Arencibia
 */
public class Mist {

    private final int exon_start;
    private final int exon_end;
    private final int poorStart;
    private final int poorEnd;
    private final int exon_number;
    private final String chrom;
    private final String gene_id;
    private final String gene_name;
    private final String exon_id;
    private final String transcript_name;
    private final String transcript_info;
    private final String gene_biotype;
    private final String match;
    private final String line;

    public Mist(String line) {
        String[] row = line.split("\t");
        this.chrom = row[0];
        this.exon_start = Integer.valueOf(row[1]);
        this.exon_end = Integer.valueOf(row[2]);
        this.poorStart = Integer.valueOf(row[3]);
        this.poorEnd = Integer.valueOf(row[4]);
        this.gene_id = row[5];
        this.gene_name = row[6];
        this.exon_number = Integer.valueOf(row[7]);
        this.exon_id = row[8];
        this.transcript_name = row[9];
        this.transcript_info = row[10];
        this.gene_biotype = row[11];
        this.match = row[12];
        this.line = line;
    }

    /**
     * @return the exon_start
     */
    public int getExonStart() {
        return exon_start;
    }

    /**
     * @return the exon_end
     */
    public int getExonEnd() {
        return exon_end;
    }

    /**
     * @return the poor_start
     */
    public int getPoorStart() {
        return poorStart;
    }

    /**
     * @return the poor_end
     */
    public int getPoorEnd() {
        return poorEnd;
    }

    /**
     * @return the exon_number
     */
    public int getExon_number() {
        return exon_number;
    }

    /**
     * @return the chrom
     */
    public String getChrom() {
        return chrom;
    }

    /**
     * @return the gene_id
     */
    public String getGene_id() {
        return gene_id;
    }

    /**
     * @return the gene_name
     */
    public String getGene_name() {
        return gene_name;
    }

    /**
     * @return the exon_id
     */
    public String getExon_id() {
        return exon_id;
    }

    /**
     * @return the transcript_name
     */
    public String getTranscript_name() {
        return transcript_name;
    }

    /**
     * @return the transcript_info
     */
    public String getTranscript_info() {
        return transcript_info;
    }

    /**
     * @return the gene_biotype
     */
    public String getGene_biotype() {
        return gene_biotype;
    }

    /**
     * @return the match
     */
    public String getMatch() {
        return match;
    }

    @Override
    public String toString() {
        return line;
    }

    public boolean contains(Variant v) {
        return chrom.equals(v.getChrom()) && poorStart <= v.getPos() && v.getPos() <= poorEnd;
    }

    /**
     *
     * @param v
     * @return -1 if variant is before this, +1 if after, 0 if inside (the same than contains(v))
     */
    public int compare(Variant v) {
        if (chrom.equals(v.getChrom())) {
            if (poorStart > v.getPos()) {
                return -1;
            } else if (poorEnd < v.getPos()) {
                return 1;
            } else {
                return 0;
            }
        } else {
            int iv = v.getVcf().getContigs().indexOf(v.getChrom());
            int im = v.getVcf().getContigs().indexOf(chrom);
            if (iv > im) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
