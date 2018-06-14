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

/*
 * SQL.java
 *
 * This component is used to connect to the Mysql database and save sensor data and sensor information
 * 
 * Created: 2018/02/27
 * @author Rilind Hasanaj <rilind.hasanaj0018@stud.hkr.se>
 */

public class SQL {
    
    Statement stmt = null;          // Is used to create SQL statements 
    ResultSet rs = null;            // saves the results from a SQL statemtnt execution
    Connection con = null;          // Is used to create a connection to database
    PreparedStatement ps = null;    // creates prepared SQL statements
    int session;                    // current session number
    
    /**
     * @param sensor_value
     * @param sensor_on
     * @param id
     * @param i2c_size
     * @throws java.lang.InterruptedException
     * @throws java.sql.SQLException
     * 
     * Adds a new sensor value to the Database
     */
    public void add_value(short[][] sensor_value, boolean[] sensor_on, int[] id, int[] i2c_size) throws InterruptedException, SQLException {
        Thread thread1 = new Thread() { 
            public void run() {
                try {
                    for (int i = 0; i < sensor_value.length; i++) {
                        if (sensor_on[i]) {
                            if (i == 6 || i == 7|| i == 8 || i == 9) {
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
    }
    
    // Get the interface for a sensor with a specific id from the database
    public String getInterface(int id) throws SQLException {
        rs = stmt.executeQuery("select Interface from sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    // Get the method for a analog/digital sensor
    public String getMethod(int id) throws SQLException {
        rs = stmt.executeQuery("select Reading_method from Analog_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }
    
    // get the Hex address for a i2c or SPI sensor from database
    public String getAddr(int id) throws SQLException {
        rs = stmt.executeQuery("select I2c_Address from i2c_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    // Get read register for a specifc sensor
    public String getReg(int id) throws SQLException {
        rs=stmt.executeQuery("select interface from sensors where sensor_id="+id);
        rs.first();
        rs = stmt.executeQuery("select Read_addr from "+rs.getString(1)+"_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }
    
    // Get write register for a specific sensor
    public String getWReg(int id) throws SQLException {
        rs=stmt.executeQuery("select interface from sensors where sensor_id="+id);
        rs.first();
        rs = stmt.executeQuery("select Write_addr from "+rs.getString(1)+"_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    // get write data for a specific sensor
    public String getWData(int id) throws SQLException {
        rs=stmt.executeQuery("select interface from sensors where sensor_id="+id);
        rs.first();
        rs = stmt.executeQuery("select Write_data from "+rs.getString(1)+"_sensors where Sensor_id=" + id);
        rs.first();
        return rs.getString(1);
    }

    // Get a list of sensor that is connected to specfic sensor node.
    // Node 0 to 5: return a list of Analog/digital sensors
    // Node 6 and 7: return a list of i2c sensors
    // Node 8 and 9: retirn a list of SPI sensors
    public ObservableList<String> list(int i) throws SQLException {
        System.out.println(i);
        if (i >= 1 && i <= 6) {
            rs = stmt.executeQuery("select Sensor_id,Name,Sensing_type,Reference from sensors where Interface= 'Analog_digital'");
        } else if (i == 7 || i == 8) {
            rs = stmt.executeQuery("select Sensor_id,Name,Sensing_type,Reference from sensors where Interface= 'I2C'");
        } else {
            rs = stmt.executeQuery("select Sensor_id,Name,Sensing_type,Reference from sensors where Interface= 'SPI'");
        }

        rs.beforeFirst();
        ObservableList<String> list = FXCollections.observableArrayList();

        while (rs.next()) {
            String format = String.format("%4s¤ %-16s ¤ %-12s ¤ %-5s", rs.getString(1), rs.getString(2), rs.getString(3),rs.getString(4));
            list.add(format);

            System.out.println(rs.getString(1) + "  : " + rs.getString(2));
        }
        return list;

    }
    // Add information on a analog/digital sensor to database
    public void add_sensor(String name, String sensing_type, String reading_method,String reference) throws SQLException {
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type,reference) VALUES (?,?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "Analog_digital");
        ps.setString(3, sensing_type);
        ps.setString(4, reference);
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

    // Add information on a i2c sensor to database
    public void add_sensor(String name, String sensing_type, String I2C_addr, String write_addr, String write_data, String read_addr,String reference) throws SQLException {
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type,reference) VALUES (?,?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "I2C");
        ps.setString(3, sensing_type);
        ps.setString(4, reference);
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

    // Add information on a SPI sensor to database
    public void add_sensor(String name, String sensing_type, String write_addr, String write_data, String read_addr,String reference) throws SQLException {
        String query = "INSERT INTO Sensors (Name,Interface,Sensing_type,reference) VALUES (?,?,?,?)";
        ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.setString(2, "SPI");
        ps.setString(3, sensing_type);
        ps.setString(4, reference);
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

    // Connects to a mysql database and creates the tables that are needed if they don't exist already 
    // If it is able to connect than this method will return the session number, else it will return: -1
    public int start(String UserName, String UserPass, int PortNr, String IP_address, String Schema) throws ClassNotFoundException, SQLException {
        
        // if all the input is blank then a default configuration will be used
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
        System.out.println("HHH: "+ Schema);
        if (Schema.equalsIgnoreCase("")) {
            Schema = "Sensors";
        }
        Class.forName("com.mysql.jdbc.Driver");
        // connect to database
        System.out.println("DBBB");
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + IP_address + ":" + PortNr, UserName, UserPass);
        } catch (SQLException ex) {
            // if fail return -1
            return -1;
        }
        
        // create the database if does not exists
        stmt = con.createStatement();
        rs= stmt.executeQuery("SHOW DATABASES");
        rs.beforeFirst();
        boolean schema=true;
        while(rs.next()){
           if( rs.getString(1).equalsIgnoreCase(Schema))
           schema=false;
        }
        if(schema){
         stmt = con.createStatement();
         stmt.execute("CREATE SCHEMA "+Schema);
            
        }
        System.out.println(Schema);
       
        con.close();
         try {
            con = DriverManager.getConnection("jdbc:mysql://" + IP_address + ":" + PortNr+"/"+Schema, UserName, UserPass);
        } catch (SQLException ex) {
            // if fail return -1
            return -1;
        }
        
        stmt = con.createStatement();
        // get all the tables in this scheme
        rs = stmt.executeQuery("Show Tables");
        rs.last();
        
        // If no tables exist then create all the tables
        if (rs.getRow() == 0) {
            stmt = con.createStatement();
            String myTableName
                    = "CREATE TABLE Sensors ("
                    + "Sensor_id INT(64) NOT NULL AUTO_INCREMENT,"
                    + "Sensing_type VARCHAR(25),"
                    + "Name VARCHAR(25),"
                    + "Interface VARCHAR(25),"
                    + "Reference VARCHAR(12),"
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
            // Else check what tables do not exist and then create them
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
                        + "Reference VARCHAR(12),"
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
        // get the latest sensor session
        rs = stmt.executeQuery("select session from sensor_sessions;");
        if (rs.absolute(1)) {
            rs.last();
            session = rs.getInt(1) + 1;
        } else {
            session = 0;
        }
        System.out.println("Session: " + session);
        // create a new session 
        String query = "INSERT INTO Sensor_Sessions (Sensor_id,Date,Value,Value_nr,Session) VALUES (?,?,?,?,?)";
        ps = con.prepareStatement(query);
        return session;
    }

    // Close the Database connection
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
