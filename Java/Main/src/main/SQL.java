/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
     * @param sensor_on
     * @throws java.lang.InterruptedException
     * @throws java.sql.SQLException
     */
    public void add_value(short[][] sensor_value, boolean[] sensor_on, int[] id, int[] i2c_size) throws InterruptedException, SQLException {
        Thread thread1 = new Thread() {
            public void run() {
                try {
                    for (int i = 0; i < sensor_value.length; i++) {
                        if (sensor_on[i]) {
                            if (i == 6 || i == 7) {
                                for (int j = 0; j < i2c_size[i - 6]; j++) {
                                    short value = sensor_value[i][j];
                                    ps.setString(1, id[i] + "");
                                    ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                                    ps.setInt(3, value);
                                    ps.setInt(4, j);
                                    ps.setInt(5, session);
                                    ps.executeUpdate();
                                }
                            } else {
                                short value = sensor_value[i][0];
                                ps.setString(1, id[i] + "");
                                ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                                ps.setInt(3, value);
                                ps.setInt(4, 0);
                                ps.setInt(5, session);
                                ps.executeUpdate();
                            }
                        }
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

    public String getInterface(int id) throws SQLException {
        rs = stmt.executeQuery("select Interface from sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    public String getMethod(int id) throws SQLException {
        rs = stmt.executeQuery("select Reading_method from Analog_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    public String getAddr(int id) throws SQLException {
        rs = stmt.executeQuery("select I2c_Address from i2c_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    public String getReg(int id) throws SQLException {
        rs=stmt.executeQuery("select interface from sensors where sensor_id="+id);
        rs.first();
        rs = stmt.executeQuery("select Read_addr from "+rs.getString(1)+"_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    public String getWReg(int id) throws SQLException {
        rs=stmt.executeQuery("select interface from sensors where sensor_id="+id);
        rs.first();
        rs = stmt.executeQuery("select Write_addr from "+rs.getString(1)+"_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    public String getWData(int id) throws SQLException {
        rs=stmt.executeQuery("select interface from sensors where sensor_id="+id);
        rs.first();
        rs = stmt.executeQuery("select Write_data from "+rs.getString(1)+"_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
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
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type) VALUES (?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "Analog_digital");
        ps.setString(3, sensing_type);
        ps.executeUpdate();
        rs =stmt.executeQuery("select sensor_id from sensors");
        rs.last();
        int sensorID =rs.getInt(1);
        
        query = "INSERT INTO analog_sensors (Reading_method,sensor_id) VALUES (?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, reading_method);
        ps.setInt(2, sensorID);
        ps.executeUpdate();
    }

    public void add_sensor(String name, String sensing_type, String I2C_addr, String write_addr, String write_data, String read_addr) throws SQLException {
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type) VALUES (?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "I2C");
        ps.setString(3, sensing_type);
        ps.executeUpdate();
        rs =stmt.executeQuery("select sensor_id from sensors");
        rs.last();
        int sensorID =rs.getInt(1);
        
        
        query = "INSERT INTO I2C_sensors (I2c_Address,"
                + "Write_addr,Write_data,Read_addr,sensor_id) VALUES (?,?,?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, I2C_addr);
        ps.setString(2, write_addr);
        ps.setString(3, write_data);
        ps.setString(4, read_addr);
        ps.setInt(5, sensorID);
        ps.executeUpdate();
    }

    public void add_sensor(String name, String sensing_type, String write_addr, String write_data, String read_addr) throws SQLException {
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type) VALUES (?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "SPI");
        ps.setString(3, sensing_type);
        ps.executeUpdate();
        rs =stmt.executeQuery("select sensor_id from sensors");
        rs.last();
        int sensorID =rs.getInt(1);
        
        
        
        query = "INSERT INTO SPI_Sensors (Write_addr,Write_data,Read_addr,sensor_id) VALUES (?,?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, write_addr);
        ps.setString(2, write_data);
        ps.setString(3, read_addr);
        ps.setInt(4, sensorID);
        ps.executeUpdate();
    }

    public int start(String UserName, String UserPass, int PortNr, String IP_address, String Schema) throws ClassNotFoundException, SQLException {
        if (UserName.equalsIgnoreCase("")) {
            UserName = "root";
        }
        if (UserPass.equalsIgnoreCase("")) {
            UserPass = "root";
        }
        if (PortNr == 0) {
            PortNr = 3306;
        }
        if (IP_address.equalsIgnoreCase("")) {
            IP_address = "localhost";
        }
        if (Schema.equalsIgnoreCase("")) {
            Schema = "Sensors";
        }
        Class.forName("com.mysql.jdbc.Driver");
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + IP_address + ":" + PortNr + "/" + Schema, UserName, UserPass);
        } catch (SQLException ex) {
            return -1;
        }

        stmt = con.createStatement();

        rs = stmt.executeQuery("Show Tables");

        rs.last();
        if (rs.getRow() == 0) {
            stmt = con.createStatement();
            String myTableName
                    = "CREATE TABLE Sensors ("
                    + "Sensor_id INT(64) NOT NULL AUTO_INCREMENT,"
                    + "Sensing_type VARCHAR(25),"
                    + "Name VARCHAR(25),"
                    + "Interface VARCHAR(25),"
                    + "PRIMARY KEY(Sensor_id))";
            stmt.executeUpdate(myTableName);
            myTableName
                    = "CREATE TABLE I2C_Sensors ("
                    + "id INT(64) NOT NULL AUTO_INCREMENT,"
                    + "Sensor_id INT(64),"
                    + "I2c_Address VARCHAR(4),"
                    + "Write_addr VARCHAR(12),"
                    + "Write_data VARCHAR(12),"
                    + "Read_addr VARCHAR(12),"
                    + "PRIMARY KEY(id),"
                    + "FOREIGN KEY (Sensor_id) REFERENCES sensors(Sensor_id))";
            stmt.executeUpdate(myTableName);
            myTableName
                    = "CREATE TABLE SPI_Sensors ("
                    + "id INT(64) NOT NULL AUTO_INCREMENT,"
                    + "Sensor_id INT(64),"
                    + "Write_addr VARCHAR(12),"
                    + "Write_data VARCHAR(12),"
                    + "Read_addr VARCHAR(12),"
                    + "PRIMARY KEY(id),"
                    + "FOREIGN KEY (Sensor_id) REFERENCES sensors(Sensor_id))";
            stmt.executeUpdate(myTableName);
            myTableName
                    = "CREATE TABLE Analog_Sensors ("
                    + "id INT(64) NOT NULL AUTO_INCREMENT,"
                    + "Sensor_id INT(64),"
                    + "Reading_method VARCHAR(25),"
                    + "PRIMARY KEY(id),"
                    + "FOREIGN KEY (Sensor_id) REFERENCES sensors(Sensor_id))";
            stmt.executeUpdate(myTableName);
            myTableName
                    = "CREATE TABLE Sensor_Sessions ("
                    + "id INT(64) NOT NULL AUTO_INCREMENT,"
                    + "Sensor_id INT,"
                    + "Date TIMESTAMP,"
                    + "Value INT, "
                    + "Value_nr INT,"
                    + "Session INT,"
                    + "PRIMARY KEY(id),"
                    + "FOREIGN KEY (Sensor_id) REFERENCES sensors(Sensor_id))";
            stmt.executeUpdate(myTableName);
            stmt = con.createStatement();

        } else {
            rs.beforeFirst();
            boolean db1 = true;
            boolean db2 = true;
            boolean db3 = true;
            boolean db4 = true;
            boolean db5 = true;
            while (rs.next()) {
                System.out.println(rs.getString(1));
                if (rs.getString(1).equalsIgnoreCase("Sensors")) {
                    db1 = false;
                }
                if (rs.getString(1).equalsIgnoreCase("I2C_Sensors")) {
                    db2 = false;
                }
                if (rs.getString(1).equalsIgnoreCase("SPI_Sensors")) {
                    db3 = false;
                }
                if (rs.getString(1).equalsIgnoreCase("Analog_Sensors")) {
                    db4 = false;
                }
                if (rs.getString(1).equalsIgnoreCase("Sensor_Sessions")) {
                    db5 = false;
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
                        + "PRIMARY KEY(Sensor_id))";
                stmt.executeUpdate(myTableName);
            }
            if (db2) {
                stmt = con.createStatement();
                String myTableName
                        = "CREATE TABLE I2C_Sensors ("
                        + "id INT(64) NOT NULL AUTO_INCREMENT,"
                        + "Sensor_id INT(64),"
                        + "I2c_Address VARCHAR(4),"
                        + "Write_addr VARCHAR(12),"
                        + "Write_data VARCHAR(12),"
                        + "Read_addr VARCHAR(12),"
                        + "PRIMARY KEY(id),"
                        + "FOREIGN KEY (Sensor_id) REFERENCES sensors(Sensor_id))";
                stmt.executeUpdate(myTableName);
            }
            if (db3) {
                stmt = con.createStatement();
                String myTableName
                        = "CREATE TABLE SPI_Sensors ("
                        + "id INT(64) NOT NULL AUTO_INCREMENT,"
                        + "Sensor_id INT(64),"
                        + "Write_addr VARCHAR(12),"
                        + "Write_data VARCHAR(12),"
                        + "Read_addr VARCHAR(12),"
                        + "PRIMARY KEY(id),"
                        + "FOREIGN KEY (Sensor_id) REFERENCES sensors(Sensor_id))";
                stmt.executeUpdate(myTableName);
            }
            if (db4) {
                stmt = con.createStatement();
                String myTableName
                        = "CREATE TABLE Analog_Sensors ("
                        + "id INT(64) NOT NULL AUTO_INCREMENT,"
                        + "Sensor_id INT(64),"
                        + "Reading_method VARCHAR(25),"
                        + "PRIMARY KEY(id),"
                        + "FOREIGN KEY (Sensor_id) REFERENCES sensors(Sensor_id))";
                stmt.executeUpdate(myTableName);
            }

            if (db5) {
                stmt = con.createStatement();
                String myTableName
                        = "CREATE TABLE Sensor_Sessions ("
                        + "id INT(64) NOT NULL AUTO_INCREMENT,"
                        + "Sensor_id INT,"
                        + "Date TIMESTAMP,"
                        + "Value INT, "
                        + "Value_nr INT, "
                        + "Session INT,"
                        + "PRIMARY KEY(id),"
                        + "FOREIGN KEY (Sensor_id) REFERENCES sensors(Sensor_id))";
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
        String query = "INSERT INTO Sensor_Sessions (Sensor_id,Date,Value,Value_nr,Session) VALUES (?,?,?,?,?)";
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
