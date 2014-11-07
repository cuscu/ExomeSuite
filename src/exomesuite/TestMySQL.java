/*
 * Copyright (C) 2014 uichuimi03
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author (duh)
 */
public class TestMySQL {

    Connection c;

    public void start() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            c = DriverManager.getConnection(
                    "jdbc:mysql://localhost/sample_db", "root",
                    "uichuimi01");
//            Statement st = c.createStatement();
//            st.executeUpdate("DROP TABLE IF EXISTS contacto");
//            st.executeUpdate(
//                    "CREATE TABLE IF NOT EXISTS contacto"
//                    + "(id INT AUTO_INCREMENT,"
//                    + "PRIMARY KEY(id),"
//                    + "nombre VARCHAR(20),"
//                    + "apellidos VARCHAR(20),"
//                    + "telefono VARCHAR(20))");
//            String nombres[] = {"Juan", "Pedro", "Antonio"};
//            String apellidos[] = {"Gomez", "Lopez", "Alvarez"};
//            String telefonos[] = {"123", "456", "789"};
//
//            for (int i = 0; i < nombres.length; i++) {
//                st.executeUpdate(String.format(
//                        "INSERT INTO contacto (nombre, apellidos, telefono)"
//                        + " VALUES ('%s','%s','%s')",
//                        nombres[i], apellidos[i], telefonos[i]));
//            }
//            ResultSet rs = st.executeQuery("SELECT * FROM contacto");
//            while (rs.next()) {
//                System.out.println(rs.getObject("nombre") + "; "
//                        + rs.getObject("apellidos") + "; "
//                        + rs.getObject("telefono"));
//            }
            importTSV(new File(
                    "/home/uichuimi03/Desktop/mist/lengthpoor50dp1/int100x/int100xdp1l50.txt"));
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TestMySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importTSV(File file) {
        String t_name = file.getName().substring(0, file.getName().indexOf("."));
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String header = in.readLine().replace("\t", ",");
            System.out.println(t_name + ": " + header);
            String[] headers = header.split(",");
            String command = "CREATE TABLE IF NOT EXISTS " + t_name
                    + "(id INT AUTO_INCREMENT,"
                    + "PRIMARY KEY(id),";
            for (String h : headers) {
                command += h + " VARCHAR(50),";
            }
            command = command.substring(0, command.length() - 1) + ")";
            Statement st = c.createStatement();
            st.executeUpdate(command);
            in.lines().forEach((String t) -> {
                try {
                    String inserrt = "INSERT INTO " + t_name
                            + " VALUES("
                            + t.replace("\t", ",") + ")";
                    System.out.println(inserrt);
                    st.executeUpdate(inserrt);
                } catch (SQLException ex) {
                    Logger.getLogger(TestMySQL.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            ResultSet rs = st.executeQuery("SHOW TABLE STATUS");
            while (rs.next()) {
                System.out.println(rs.getString("Name") + rs.getObject("Rows"));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestMySQL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TestMySQL.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(TestMySQL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
