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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class DindelTask extends SystemTask {

    private File input, output, temp, dindel, windows, windows2;
    private String genome, name, dindel_exe;

    public DindelTask(File input, File output, File temp, File dindel, File windows, File windows2,
            String genome, String name, String dindel_exe) {
        this.input = input;
        this.output = output;
        this.temp = temp;
        this.dindel = dindel;
        this.windows = windows;
        this.windows2 = windows2;
        this.genome = genome;
        this.name = name;
        this.dindel_exe = dindel_exe;
    }

//    public boolean configure(Config mainConfig, Config projectConfig) {
//        String al = projectConfig.getProperty("align_path");
//        String di = projectConfig.getProperty("dindel_path");
//        name = projectConfig.getProperty(Config.NAME);
//        temp = new File(projectConfig.getProperty(Config.PATH_TEMP));
//        input = new File(al, name + ".bam");
//        output = new File(di, name + "_dindel.vcf");
//        genome = mainConfig.getProperty(Config.GENOME);
//        return input.exists();
//    }
    @Override
    protected Integer call() throws Exception {
        windows = new File(temp, "windows");
        windows2 = new File(temp, "windows2");
        dindel = new File("software", "dindel");
        dindel_exe = new File(dindel, "dindel").getAbsolutePath();
        windows.mkdirs();
        windows2.mkdirs();
        int ret;
        updateMessage("Extracting candidates...");
        updateProgress(5, 100);
        ret = extractCandidatesFromBAM();
        if (ret != 0) {
            return ret;
        }

        updateMessage("Creating realignment windows...");
        updateProgress(10, 100);
        ret = createRealignWindows();
        if (ret != 0) {
            return ret;
        }

        updateMessage("Realigning windows...");
        updateProgress(20, 100);
        ret = realignWindows();
        if (ret != 0) {
            return ret;
        }

        updateMessage("Generating VCF...");
        updateProgress(90, 100);
        ret = mergeIndels();
        return ret;
    }

    /**
     * First step for standard dindel workflow: extraction of potential indels from the input BAM
     * file. The output are 2 files, named after sample name (name.libraries.txt and
     * name.variants.txt)
     */
    private int extractCandidatesFromBAM() {
        // Command appearance:
        // /home/uai/dindel/dindel
        //  --analysis getCIGARindels \
        //  --bamFile input.bam \
        //  --ref reference.fasta \
        //  --outputFile temp/name
        String output_prefix = new File(temp, name).getAbsolutePath();
        return execute(dindel_exe, "--analysis", "getCIGARindels", "--quiet",
                "--bamFile", input.getAbsolutePath(),
                "--ref", genome,
                "--outputFile", output_prefix);
        // This will generate two files
        // temp/name.libraries.txt
        // temp/name.variants.txt

    }

    /**
     * Second step for standard dindel workflow: the indels obtained in stage 1 from the BAM file
     * are the candidate indels; they must be grouped into windows of ∼ 120 basepairs, into a
     * realign-window-file. The included Python script makeWindows.py will generate such a file from
     * the file with candidate indels inferred in the first stage. The output are hundreds of files
     * (temp/name/windows/window001.txt, /temp/name/windows/window002.txt,
     * /temp/name/windows/windowXXX.txt)
     */
    private int createRealignWindows() {
        // Appearance of the command
        //  python /home/uai/dindel/makeWindows.py \
        //    --inputFile temp/name/name.variants.txt \
        //    --windowFilePrefix windows/name_window \
        //    --numWindowsPerFile 1000
        String script = new File(dindel, "makeWindows.py").getAbsolutePath();
        String in = new File(temp, name + ".variants.txt").getAbsolutePath();
        String prefix = new File(windows, name + "_window").getAbsolutePath();
        return execute("python", script,
                "--inputVarFile", in,
                "--windowFilePrefix", prefix,
                "--numWindowsPerFile", "1000");
        // So:
        // temp/windows/name_window001.txt
        // temp/windows/name_window002.txt
        // temp/windows/name_window003.txt
        // temp/windows/name_window004.txt
        // ... and so on ...
    }

    /**
     * Third step for dindel standard workflow: for every window, DindelTool will generate candidate
     * haplotypes from the candidate indels and SNPs it detects in the BAM file, and realign the
     * reads to these candidate haplotypes. The realignment step is the computationally most
     * intensive step.
     */
    private int realignWindows() {
        // Command appearance (we must do this for every window):
        // /home/uai/dindel/dindel
        //   --analysis indels \
        //   --doDiploid \
        //   --bamFile input.bam \
        //   --ref reference.fasta \
        //   --varFile temp/name/windows/name_window001.txt \
        //   --libFile temp/name/name.libraries.txt \
        //   --outputFile temp/name/windows2/name_windows001.txt \
        //   &>/dev/null
        File[] files = windows.listFiles();
        final int total = files.length;
//        int count = 1;
        String libraries = new File(temp, name + ".libraries.txt").getAbsolutePath();
        AtomicInteger progress = new AtomicInteger();
        AtomicInteger returnValue = new AtomicInteger();
        Arrays.asList(files).parallelStream().forEach((File f) -> {
            if (returnValue.get() == 0) {
                String out = new File(windows2, f.getName()).getAbsolutePath();
                int p = progress.get() * 70 / total;
                updateMessage(String.format("Realigning window %d/%d", progress.incrementAndGet(),
                        total));
                updateProgress(20 + p, 100);
                int r = execute(dindel_exe, "--analysis", "indels", "--doDiploid", "--quiet",
                        "--bamFile", input.getAbsolutePath(),
                        "--ref", genome,
                        "--varFile", f.getAbsolutePath(),
                        "--libFile", libraries,
                        "--outputFile", out);
                if (r != 0) {
                    returnValue.set(r);
                }
            }
        });
        return 0;
    }

    /**
     * Last step for dindel standard workflow: interpreting the output from DindelTool and produce
     * indel calls and qualities in the VCF4 format.
     */
    private int mergeIndels() {
        // Command appearance:
        //  python /home/uai/dindel/mergeOutputDiploid.py \
        //   --ref reference.fasta \
        //   --inputFiles fileList.txt \
        //   --outputFile output.vcf
        // where fileList contains a list of all windows2 files.
        File fileList = new File(temp, "fileList.txt");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileList))) {
            for (File f : windows2.listFiles()) {
                bw.write(f.getPath());
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(DindelTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        String script = new File(dindel, "mergeOutputDiploid.py").getAbsolutePath();
        return execute("python", script,
                "--ref", genome,
                "--inputFiles", fileList.getAbsolutePath(),
                "--outputFile", output.getAbsolutePath());
    }
}
