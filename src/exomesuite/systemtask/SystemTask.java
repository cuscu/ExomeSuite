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
package exomesuite.systemtask;

import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 * Class that should override any long-CPU process in the Application. To execute a system command,
 * use execute(String... args).
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class SystemTask extends Task<Integer> {

    private final PrintStream printStream;
    private Process process;

    public SystemTask(PrintStream printStream) {
        this.printStream = printStream;
    }

    /**
     * Executes a system command.
     *
     * @param args
     * @return
     */
    protected int execute(String... args) {
        ProcessBuilder pb;
        pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        try {
            process = pb.start();

            if (printStream != null) {
                int c;
                while ((c = process.getInputStream().read()) != -1) {
                    printStream.write(c);
                }
            } else {
                // Consume pipe, otherwise, it will be blocked ad infinitum.
                while (process.getInputStream().read() != -1) {
                }
            }
            return process.waitFor();
        } catch (InterruptedException | IOException ex) {
            process.destroy();
            Logger.getLogger(SystemTask.class.getName()).log(Level.SEVERE, null, ex);
            return -1;

        } catch (SecurityException ex) {
            System.out.println("Cannot extract pid");
            Logger.getLogger(SystemTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(SystemTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    protected void cancelled() {
        if (process != null) {
            System.out.println("Destroying...");
            process.destroy();
        }
    }

}
