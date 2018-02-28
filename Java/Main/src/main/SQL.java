/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import jdk.nashorn.internal.runtime.Version;

/**
 *
 * @author Rilind Hasanaj <rilind.hasanaj0018@stud.hkr.se>
 */
public class SQL {

    Statement stmt = null;
    ResultSet rs = null;
    Connection con = null;
    PreparedStatement ps = null;
    String session;

    /**
     * @param sensor_value
     * @throws java.lang.InterruptedException
     * @throws java.sql.SQLException
     */
    public void add(short[] sensor_value) throws InterruptedException, SQLException {
        Thread thread1 = new Thread() {
            public void run() {
                try {
                    for (int i = 0; i < sensor_value.length; i++) {
                        short value = sensor_value[i];
                        String name = i + 1 + "";
                        ps.setString(1, name);
                        ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        ps.setInt(3, value);
                        ps.executeUpdate();
                    }
                } catch (Exception ex) {
                    System.out.println("Database error");
                }
            }
        };
        thread1.start();
//                
       // System.out.println(new Timestamp(System.currentTimeMillis()));

        // ResultSetMetaData columns = rs.getMetaData();
        // System.out.printf("%4s | %-34s | %3s | %-10s\n", columns.getColumnName(1), columns.getColumnName(2), columns.getColumnName(3), columns.getColumnName(4));
        //  System.out.println("------------------------------------------------------------------");
    }

    public String start() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Sensors", "root", "root");
        session = "session_";
        stmt = con.createStatement();

        rs = stmt.executeQuery("Show Tables");

        System.out.println(rs.absolute(1));
        if (rs.absolute(1)) {
            rs.last();
            String parts[] = rs.getString(1).split("_");
            int num = Integer.parseInt(parts[1]) + 1;
            int i = 4;
            int tmp = num;
            String zero = "";
            while (tmp != 0) {
                tmp = tmp / 10;
                i--;
            }
            for (; i > 0; i--) {
                zero += 0;
            }

            session += zero + (Integer.parseInt(parts[1]) + 1);
        } else {
            session += "0000";
        }
        System.out.println(session);
        stmt = con.createStatement();
        String myTableName = "CREATE TABLE " + session + "("
                + "id INT(64) NOT NULL AUTO_INCREMENT,"
                + "Sensor_nr VARCHAR(10),"
                + "Date TIMESTAMP,"
                + "Value INT(64), "
                + "PRIMARY KEY(id))";
        stmt.executeUpdate(myTableName);
        String query = "INSERT INTO " + session + " (Sensor_nr,Date,Value) VALUES (?,?,?)";
        ps = con.prepareStatement(query);
        return session;
    }

    public void close() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (con != null) {
                con.close();

            }

        } catch (SQLException ex) {
            Logger lgr = Logger.getLogger(Version.class
                    .getName());
            lgr.log(Level.WARNING, ex.getMessage(), ex);
        }
    }

}
