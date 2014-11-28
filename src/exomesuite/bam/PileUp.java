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
package exomesuite.bam;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores information about a position in the pileup. The reference and the depth of each base.
 * Reference can be * for insertions.
 *
 * @author Pascual Lorente Arencibia (pasculorente@gmail.com)
 */
public class PileUp {

    public final static char A = 'A';
    public final static char C = 'C';
    public final static char T = 'T';
    public final static char G = 'G';
    public final static char N = 'N';
    public final static char EMPTY = '*';

    private final Map<Character, Integer> depths = new HashMap<>();
    /**
     * The reference.
     */
    private final char reference;

    /**
     * Creates a new PileUp with the given reference
     *
     * @param reference
     */
    public PileUp(char reference) {
        this.reference = reference;
    }

    /**
     * The reference base. ACTG or '*'
     *
     * @return the reference base
     */
    public char getReference() {
        return reference;
    }

    /**
     * The DP (depth of coverage) of the base in this position.
     *
     * @param base the base
     * @return the depth of coverage
     */
    public int getDepth(char base) {
        return depths.getOrDefault(base, 0);
    }

    /**
     * Adds 1 to the dp of the given base. ACTG for the forward, actg for the reverse
     *
     * @param base the base to increment (ACTGactgN*)
     */
    public void incrementDepth(char base) {
        final int dp = depths.getOrDefault(base, 0);
        depths.put(base, dp + 1);
    }

    public Map<Character, Integer> getDepths() {
        return depths;
    }

    @Override
    public String toString() {
        return String.format("%c->%s", reference, depths);
    }

}
