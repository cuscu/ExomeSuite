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

import exomesuite.tool.Console;
import exomesuite.utils.OS;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 * Class that should override any long-CPU process in the Application. To execute a system command,
 * use {@code execute(String... args)}. To print messages to the application use
 * {@code println(message)} instead of {@code System.out.println(message)}. Task are usually managed
 * by a {@link Step}.
 * <p>
 * As they extends from {@link Task}, they can be launched only once. In order to reuse them, it is
 * necessary to create a new SystemTask. The {@code configure} method was created to substitute the
 * constructor. Use it as shown below.
 * <p>
 * {@code SystemTask mytask = new MyTask();}
 * <p>
 * {@code mytask.configure(projectConfig, mainConfig);}
 * <p>
 * {@code new Thread(mytask).start);}
 *
 * @author Pascual Lorente Arencibia
 */
public abstract class SystemTask extends Task<Integer> {

    /**
     * A PrintStream to replace System.out.println. See that every Task has its own PrintStream,
     * this allows ExomeSuite to manage a different {@link Console} for each SystemTask.
     */
    protected PrintStream printStream;
    /**
     * The Process used by {@code execute{String...&nbsp;args)}. If there is no System command
     * running, it will be null.
     */
    private Process process;

    /**
     * Default constructor redirects output PrintStream to System.out.
     */
    public SystemTask() {
        this.printStream = System.out;
    }

    /**
     * Executes a system command.
     *
     * @param args The command args. Note that it is not necessary to put spaces between two args.
     * See {@link ProcessBuilder} for more information.
     *
     * @return command exit value
     *
     * @see ProcessBuilder
     */
    protected int execute(String... args) {
        System.out.println(OS.asString(" ", args));
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
            // If another thread interrupted the execution.
            return process.exitValue();
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(SystemTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        process = null;
        return 0;
    }

    /**
     * Ensures that the process is destroyed when the Task is canceled.
     */
    @Override
    protected void cancelled() {
        if (process != null) {
            process.destroy();
            println("Canceled...");
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (process != null) {
            process.destroy();
            println("Canceled...");
        }
        return super.cancel(mayInterruptIfRunning);
    }

//    /**
//     * Must return true if the task could be configured successfully, otherwise return false. If
//     * this method returns true, it means the task can be launch.
//     *
//     * @param mainConfig The configuration of the application.
//     * @param projectConfig The configuration of the project.
//     * @return true is SystemTask has got all the parameters ready to launch.
//     */
//    public abstract boolean configure(Config mainConfig, Config projectConfig);
    /**
     * Sets the value of the printStream. By default, its value is System.out.
     *
     * @param printStream the new printStream of the task.
     */
    public void setPrintStream(PrintStream printStream) {
        this.printStream = printStream;
    }

    /**
     * Calls {@code super.updateMessage(message)} and prints the message on printStream and
     * System.out.
     *
     * @param message The message to be printed.
     */
    @Override
    protected void updateMessage(String message) {
        super.updateMessage(message);
//        printStream.println(message);
        System.out.println(message);
    }

    /**
     * Prints a line in the PrintStream of this task. Use this method instead of System.out.println
     * if you want to have the message printed in the application.
     *
     * @param message some nice message to make happy some users.
     */
    protected void println(String message) {
        printStream.println(message);
        System.out.println(message);
    }

}
