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

    /**
     * Character used for the reference whe nhas no value. It is an asterisk. '*'
     */
    public final static char EMPTY = '*';

    /**
     * Keys are ACTGN and values the DPs
     */
    private final Map<Character, Integer> depths = new HashMap();
    /**
     * The reference.
     */
    private final char reference;

    /**
     * Creates a new PileUp with the given reference
     *
     * @param reference the reference base
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

    /**
     * Gets the depths as map. the depth of a is {@code getDepths().get('A)}
     *
     * @return the depths map
     */
    public Map<Character, Integer> getDepths() {
        return depths;
    }

    /**
     * The string representation of the pilepup: A-{T=1,C=2,t=14,c=12}.
     *
     * @return the String representation
     */
    @Override
    public String toString() {
        return String.format("%c->%s", reference, depths);
    }

}
