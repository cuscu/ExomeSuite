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
package exomesuite;

import exomesuite.utils.Command;
import java.io.File;
import javafx.concurrent.Task;

/**
 * This class will create the proper indexes for bwa, samtools and picard in the same directory
 * where the genome is placed.
 *
 * @author Pascual Lorente Arencibia
 */
public class Indexer extends Task<Integer> {

    private final String genome;
    private Command command;

    public Indexer(String genome) {
        this.genome = genome;
    }

    @Override
    protected Integer call() throws Exception {
        updateMessage("Generating BWA index...");
        updateProgress(5, 100);
        command = new Command("bwa", "index", "-a", "bwtsw", genome);
        int ret = command.execute();
        if (ret != 0) {
            return ret;
        }
        updateMessage("Generating samtools index...");
        updateProgress(60, 100);
        command = new Command("samtools", "faidx", genome);
        if ((ret = command.execute()) != 0) {
            return ret;
        }
        updateMessage("Generating Picard index...");
        updateProgress(80, 100);
        new Command("java", "-jar", "software" + File.separator
                + "picard" + File.separator + "CreateSequenceDictionary.jar",
                "R=" + genome, "O=" + genome + ".dict").execute();
        updateMessage("Done");
        updateProgress(1, 1);
        return 0;
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        command.kill();
    }

}
