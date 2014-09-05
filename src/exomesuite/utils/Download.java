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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import javafx.concurrent.Task;

/**
 *
 * @author Pascual Lorente Arencibia
 */
public class Download extends Task<Void> {

    private final String url;
    private final File file;

    public Download(String url, File file) {
        this.url = url;
        this.file = file;
    }

    @Override
    protected Void call() throws Exception {
        System.out.println(url);
        URL url1 = new URL(url);
        URLConnection connection = url1.openConnection();
        try (InputStream in = connection.getInputStream();
                OutputStream out = new FileOutputStream(file);) {
            long length = connection.getContentLengthLong();
            final String tot = OS.humanReadableByteCount(length, true);
            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalDownloaded = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                totalDownloaded += bytesRead;
                out.write(buffer);
                updateProgress(totalDownloaded, length);
                updateMessage(file + ":" + OS.humanReadableByteCount(totalDownloaded, true) + "/"
                        + tot);
            }
        }
        System.out.println("Done " + file);
        return null;
    }

}
