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
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual
 */
public class Command {

    private final String[] args;
    //private final PrintStream errOut, stdOut;
    private Process process = null;

    /**
     * Creates a new command. Standard and error outputs will be flushed to System.out.
     *
     * @param args The command and its arguments
     */
    public Command(String... args) {
        this.args = args;
    }

    /*
     **WARNING: If you use bash, it won't be possible to cancel execution, since /bin/bahs creates
     * a subprocess.
     */
    /**
     * Executes command. Command must be created with constructor. Cannot be modified afterwards.
     *
     * @param output
     * @return The return value of the system call.
     */
    public int execute(PrintStream output) {
        ProcessBuilder pb;
        pb = new ProcessBuilder(args);
        pb.redirectErrorStream(true);
        try {
            process = pb.start();

            if (output != null) {
                int c;
                while ((c = process.getInputStream().read()) != -1) {
                    output.write(c);
                }
            } else {
                // Consume pipe, otherwise, it will be blocked ad infinitum.
                while (process.getInputStream().read() != -1) {
                }
            }
            return process.waitFor();
        } catch (InterruptedException | IOException ex) {
            process.destroy();
            Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
            return -1;

        } catch (SecurityException ex) {
            System.out.println("Cannot extract pid");
            Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Command.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    /**
     * Executes this command. By default PrintStream is System.out. This is equivalent to
     * execute(System.out).
     *
     * @return The return code of the command execution.
     */
    public int execute() {
        return execute(System.out);
    }

    /**
     * Pray and dance around a fire. It'll kill the process.
     */
    public void kill() {
        if (process != null) {
            System.out.println("Destroying...");
            process.destroy();
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for (String s : args) {
            ret += s + " ";
        }
        return "Command=" + ret;
    }

}
