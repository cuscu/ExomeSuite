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
package exomesuite.utils;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class should be used to check which software is installed in the system.
 *
 * @author Pascual Lorente Arencibia
 */
public class Software {

    /**
     * Checks if samtools is installed in System.
     *
     * @return true if a call to samtools in the system returns a code different from 126 or 127
     */
    public static boolean isSamtoolsInstalled() {
        ProcessBuilder pb = new ProcessBuilder("samtools");
        Process p;
        try {
            p = pb.start();
            final int r = p.waitFor();
            //System.out.println(p.waitFor());
            return (r != 126 && r != 127);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(Software.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Checks if bwa is installed in System.
     *
     * @return true if a call to bwa in the system returns a code different from 126 or 127
     */
    public static boolean isBwaInstalled() {
        ProcessBuilder pb = new ProcessBuilder("bwa");
        Process p;
        try {
            p = pb.start();
            final int r = p.waitFor();
            //System.out.println(p.waitFor());
            return (r != 126 && r != 127);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(Software.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Checks if gatk is installed in System.
     *
     * @return true if a call to gatk in the system returns a code different from 126 or 127
     */
    public static boolean isGatkInstalled() {
        ProcessBuilder pb = new ProcessBuilder("software/jre1.7.0_71/bin/java", "-jar",
                "software/gatk/GenomeAnalysisTK.jar");
        Process p;
        try {
            p = pb.start();
            final int r = p.waitFor();
            //System.out.println(p.waitFor());
            return (r != 126 && r != 127);
        } catch (InterruptedException | IOException ex) {
            Logger.getLogger(Software.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
