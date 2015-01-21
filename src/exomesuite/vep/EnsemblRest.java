/*
 * Copyright (C) 2015 UICHUIMI
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
package exomesuite.vep;

import exomesuite.json.JSONArray;
import exomesuite.json.JSONObject;
import exomesuite.vcf.Variant;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Only works with GRCh38.
 *
 * @author Lorente Arencibia, Pascual <pasculorente@gmail.com>
 *
 */
public class EnsemblRest {

    private final static String server = "http://rest.ensembl.org";

    /**
     * Only works for GRCh38.
     *
     * @param variants
     * @return
     */
    public static Map<String, String> getVepInformation(Variant... variants) {
        return getVepInformation(Arrays.asList(variants));
    }

    /**
     * Only works for GRCh38.
     *
     * @param variants
     * @return
     */
    public static Map<String, String> getVepInformation(List<Variant> variants) {
        try {
            final String ext = "/vep/homo_sapiens/region";
            URL url;
            try {
                url = new URL(server + ext);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
//                MainViewController.printException(ex);
                return null;
            }

            URLConnection connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            JSONArray list = new JSONArray();
            variants.forEach(v -> list.put(v.toString().replace("\t", " ")));
            JSONObject root = new JSONObject();
            root.put("variants", list);
            String postBody = root.toString(2);
            System.out.println(postBody);
//            for (Variant v : variants) {
//                postBody += "\"" + v.toString().replace("\t", " ") + "\"" + ", ";
//            }
            // Remove last comma+space
            postBody = postBody.substring(0, postBody.length() - 3);
            postBody += "]}";
//            String postBody = "{ \"variants\" : [\"21 26960070 rs116645811 G A . . .\", \"21 26965148 rs1135638 G A . . .\" ] }";
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-Type", "application/json");
            httpConnection.setRequestProperty("Accept", "application/json");
            httpConnection.setRequestProperty("Content-Length", Integer.toString(postBody.getBytes().length));
            httpConnection.setUseCaches(false);
            httpConnection.setDoInput(true);
            httpConnection.setDoOutput(true);

            // Send request
            try (DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream())) {
                wr.writeBytes(postBody);
                wr.flush();
            }
            InputStream response = connection.getInputStream();
            int responseCode = httpConnection.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);
            }

            String output;
            try (Reader reader = new BufferedReader(new InputStreamReader(response, "UTF-8"))) {
                StringBuilder builder = new StringBuilder();
                char[] buffer = new char[8192];
                int read;
                while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                    builder.append(buffer, 0, read);
                }
                output = builder.toString();
            }
            // Output is a vector
//            System.out.println(output);
            JSONArray json = new JSONArray(output);
            System.out.println(json.toString(2));
//            JSONObject json = new JSONObject(output);

//            System.out.println(output);
        } catch (IOException ex) {
            Logger.getLogger(EnsemblRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
