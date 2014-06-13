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
package exomesuite.tool.align;

import javafx.concurrent.Task;

/**
 *
 * @author Pascual Lorente Arencibia
 */
class Aligner extends Task {

    final String temp, forward, reverse, genome, dbsnp, mills, phase1, output;
    final boolean illumina;

    public Aligner(String temp, String forward, String reverse, String genome, String dbsnp,
            String mills, String phase1, String output, boolean illumina) {
        this.temp = temp;
        this.forward = forward;
        this.reverse = reverse;
        this.genome = genome;
        this.dbsnp = dbsnp;
        this.mills = mills;
        this.phase1 = phase1;
        this.output = output;
        this.illumina = illumina;
    }

    @Override
    protected Void call() throws Exception {
        System.out.println("Alingment parameters");
        System.out.println("temp=" + temp);
        System.out.println("forward=" + forward);
        System.out.println("reverse=" + reverse);
        System.out.println("genome=" + genome);
        System.out.println("dbsnp=" + dbsnp);
        System.out.println("mills=" + mills);
        System.out.println("phase1=" + phase1);
        System.out.println("output=" + output);
        System.out.println("illumina=" + illumina);
        for (double i = 0.0; i < 1.01; i += 0.01) {
            updateProgress(i, 1);
            updateMessage("Step" + i);
            Thread.sleep(100);
        }
        return null;
    }

    void stop() {
        System.out.println("Stoooooop");
    }
}
