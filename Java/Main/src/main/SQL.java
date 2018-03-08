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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    int session;

    /**
     * @param sensor_value
     * @throws java.lang.InterruptedException
     * @throws java.sql.SQLException
     */
    public void add_value(short[] sensor_value) throws InterruptedException, SQLException {
        Thread thread1 = new Thread() {
            public void run() {
                try {
                    for (int i = 0; i < sensor_value.length; i++) {
                        short value = sensor_value[i];
                        String name = i + 1 + "";
                        ps.setString(1, name);
                        ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        ps.setInt(3, value);
                        ps.setInt(4, session);
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

    public ObservableList<String> list(int i) throws SQLException {
        System.out.println(i);
        if (i >= 1 && i <= 6) {
            rs = stmt.executeQuery("select Sensor_id,Name,Sensing_type from sensors where Interface= 'Analog_digital'");
        } else if (i == 7 || i == 8) {
            rs = stmt.executeQuery("select Sensor_id,Name,Sensing_type from sensors where Interface= 'I2C'");
        } else {
            rs = stmt.executeQuery("select Sensor_id,Name,Sensing_type from sensors where Interface= 'SPI'");
        }

        rs.beforeFirst();
        ObservableList<String> list = FXCollections.observableArrayList();

        while (rs.next()) {
            String format = String.format("%4s¤ %-16s ¤ %-10s", rs.getString(1), rs.getString(2), rs.getString(3));
            list.add(format);
            System.out.println(rs.getString(1) + "  : " + rs.getString(2));
        }
        return list;

    }

    public void add_sensor(String name, String sensing_type, String reading_method) throws SQLException {
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type,Reading_method) VALUES (?,?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "Analog_digital");
        ps.setString(3, sensing_type);
        ps.setString(4, reading_method);
        ps.executeUpdate();
    }

    public void add_sensor(String name, String sensing_type, String I2C_addr, String write_addr, String write_data, String read_addr1, String read_addr2, String read_addr3) throws SQLException {
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type,I2c_Address,"
                + "Write_addr,Write_data,Read_addr1,Read_addr2,Read_addr3) VALUES (?,?,?,?,?,?,?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "I2C");
        ps.setString(3, sensing_type);
        ps.setString(4, I2C_addr);
        ps.setString(5, write_addr);
        ps.setString(6, write_data);
        ps.setString(7, read_addr1);
        ps.setString(8, read_addr2);
        ps.setString(9, read_addr3);
        ps.executeUpdate();
    }

    public void add_sensor(String name, String sensing_type, String write_data, String read_addr1, String read_addr2, String read_addr3) throws SQLException {
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type,"
                + "Write_data,Read_addr1,Read_addr2,Read_addr3) VALUES (?,?,?,?,?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "SPI");
        ps.setString(3, sensing_type);
        ps.setString(4, write_data);
        ps.setString(5, read_addr1);
        ps.setString(6, read_addr2);
        ps.setString(7, read_addr3);
        ps.executeUpdate();
    }

    public int start() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Sensors", "root", "root");
        } catch (SQLException ex) {
            return -1;
        }

        stmt = con.createStatement();

        rs = stmt.executeQuery("Show Tables");

        rs.last();
        if (rs.getRow() == 0) {
            stmt = con.createStatement();
            String myTableName = "CREATE TABLE Sensor_Sessions ("
                    + "id INT(64) NOT NULL AUTO_INCREMENT,"
                    + "Sensor_nr VARCHAR(10),"
                    + "Date TIMESTAMP,"
                    + "Value INT(64), "
                    + "Session INT(64),"
                    + "PRIMARY KEY(id))";
            stmt.executeUpdate(myTableName);
            stmt = con.createStatement();
            myTableName
                    = "CREATE TABLE Sensors ("
                    + "Sensor_id INT(64) NOT NULL AUTO_INCREMENT,"
                    + "Sensing_type VARCHAR(25),"
                    + "Name VARCHAR(25),"
                    + "Interface VARCHAR(25),"
                    + "Reading_method VARCHAR(25),"
                    + "I2c_Address VARCHAR(4),"
                    + "Write_addr VARCHAR(4),"
                    + "Write_data VARCHAR(4),"
                    + "Read_addr1 VARCHAR(4),"
                    + "Read_addr2 VARCHAR(4),"
                    + "Read_addr3 VARCHAR(4),"
                    + "PRIMARY KEY(Sensor_id))";
            stmt.executeUpdate(myTableName);
        } else {
            rs.beforeFirst();
            boolean db1 = true;
            boolean db2 = true;
            while (rs.next()) {
                System.out.println(rs.getString(1));
                if (rs.getString(1).equalsIgnoreCase("Sensor_Sessions")) {
                    db2 = false;
                }
                if (rs.getString(1).equalsIgnoreCase("Sensors")) {
                    db1 = false;
                }
            }
            if (db1) {
                stmt = con.createStatement();
                String myTableName
                        = "CREATE TABLE Sensors ("
                        + "Sensor_id INT(64) NOT NULL AUTO_INCREMENT,"
                        + "Sensing_type VARCHAR(25),"
                        + "Name VARCHAR(25),"
                        + "Interface VARCHAR(25),"
                        + "Reading_method VARCHAR(25),"
                        + "I2c_Address VARCHAR(4),"
                        + "Write_addr VARCHAR(4),"
                        + "Write_data VARCHAR(4),"
                        + "Read_addr1 VARCHAR(4),"
                        + "Read_addr2 VARCHAR(4),"
                        + "Read_addr3 VARCHAR(4),"
                        + "PRIMARY KEY(Sensor_id))";
                stmt.executeUpdate(myTableName);
            }
            if (db2) {
                stmt = con.createStatement();
                String myTableName
                        = "CREATE TABLE Sensor_Sessions ("
                        + "id INT(64) NOT NULL AUTO_INCREMENT,"
                        + "Sensor_id INT,"
                        + "Date TIMESTAMP,"
                        + "Value INT, "
                        + "Session INT,"
                        + "PRIMARY KEY(id)"
                        + "FOREIGN KEY (Sensor_id) REFERENCES Sensors(Sensor_id))";
                stmt.executeUpdate(myTableName);
            }

        }

        rs = stmt.executeQuery("select session from sensor_sessions;");

        if (rs.absolute(1)) {
            rs.last();
            session = rs.getInt(1) + 1;
        } else {
            session = 0;
        }
        System.out.println("Session: " + session);
        String query = "INSERT INTO Sensor_Sessions (Sensor_nr,Date,Value,Session) VALUES (?,?,?,?)";
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
