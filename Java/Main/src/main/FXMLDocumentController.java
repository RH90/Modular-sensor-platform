/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.microedition.io.StreamConnection;

/**
 *
 * @author Rilind
 * This is the main GUI were the user can observe the sensor data from the microcontoroller.
 */
public class FXMLDocumentController implements Initializable {
//<?import java.util.ArrayList?>

    @FXML
    private ArrayList<Label> Label_list_a;
    @FXML
    private ArrayList<Label> Label_list_b;
    @FXML
    private Label lia;
    @FXML
    private Label lib;
    @FXML
    private Label wireless_module_l;
    @FXML
    private Label ldba;
    @FXML
    private TextField delay;
    @FXML
    private Label ldbb;
    @FXML
    private Button Start;
    @FXML
    private Button add_sensor_b;
    @FXML
    private Button config_db_b;
    @FXML
    private Label WIFI_L;
    @FXML
    private TextField WIFI_TF;
    @FXML
    private Rectangle wireless_module_r;
    static String[] list_string_a = new String[10];
    private final int size = 10;
    private boolean simulink = false;
    private short[][] sensor_value = new short[10][6];
    private boolean test = false;
    private boolean on_off = true;
    private Thread thread1 = null;
    private Thread thread = null;
    private Socket socket = null;
    private Socket socket1 = null;
    private boolean socket_OnOff = true;
    private String L9a_s = "";
    private String L9b_s = "";
    static boolean[] sensor_on = new boolean[10];
    static String[] labels = new String[10];
    static int[] id = new int[10];
    private SQL sql = new SQL();
    private Semaphore mutex = new Semaphore(1);
    private StreamConnection sc = null;
    private ServerSocket serverSocket;
    private int SampleRate = 1000;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
    static String UserName = "";
    static String UserPass = "";
    static int PortNr = 0;
    static String IP_address = "";
    static String Schema = "";
    private int[] i2c_size = new int[4];
    private BufferedReader Reader_Wifi = null;
    private BufferedReader Reader_Blue = null;
    private BufferedWriter Writer_Wifi = null;
    private BufferedWriter Writer_Blue = null;
    private boolean retry = true;

    // This method updates the Text on the UI
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        WIFI_TF.setText("FireFly-6786");
        //WIFI_TF.setDisable(true);
        //WIFI_L.setDisable(true);
        WIFI_L.setText("ID:");
        for (int i = 0; i < list_string_a.length; i++) {
            list_string_a[i] = "No Sensor!";
        }
        for (int i = 0; i < sensor_value.length; i++) {
            sensor_value[i][0] = 0;
            sensor_on[i] = false;
        }
        for (int i = 0; i < i2c_size.length; i++) {
            i2c_size[i] = 1;
        }
        // this task will countinious update the text on the screen with a intervall of 10 ms
        // the array sensor_value[][] contains the sensor values and sensor_on[] tells what sensor nodes are on or off.
        Task task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                int i = 0;
                while (true) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            for (int j = 0; j < Label_list_a.size(); j++) {
                                Label_list_a.get(j).setText(list_string_a[j]);
                                if (sensor_on[j]) {
                                    if (j == 6 || j == 7 || j == 8 || j == 9) {
                                        String ss = "";
                                        for (int k = 0; k < i2c_size[j - 6]; k++) {
                                            if (k % 2 == 0 && k != 0) {
                                                ss += "\n";
                                            }
                                            ss += String.format("%d: %-6s", k, sensor_value[j][k]);

                                        }
                                        Label_list_b.get(j).setText(ss);
                                    } else {
                                        Label_list_b.get(j).setText(sensor_value[j][0] + "");
                                    }
                                } else {
                                    Label_list_b.get(j).setText("0");
                                }
                            }
                            lib.setText(L9b_s);
                            lia.setText(L9a_s);
                        }
                    });
                    Thread.sleep(10);
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        // L1.setText("hej");
    }

    // This method controlls what wireless communication is currently being used (Bluetooth or Wifi) 
    @FXML
    private void chooseModule(MouseEvent event) {
        if (on_off) {
            if (wireless_module_l.getText().equalsIgnoreCase("BlueTooth")) {
                //WIFI_TF.setDisable(false);
                //WIFI_L.setDisable(false);
                WIFI_L.setText("IP:");
                wireless_module_l.setText("Wifi");
                WIFI_TF.setText("192.168.137.100");
                wireless_module_r.setFill(Paint.valueOf("0xffff00"));
            } else {
                //WIFI_TF.setDisable(true);
                //WIFI_L.setDisable(true);
                WIFI_L.setText("ID:");
                wireless_module_l.setText("BlueTooth"); 
                WIFI_TF.setText("FireFly-6786");
                wireless_module_r.setFill(Paint.valueOf("0x1e90ff"));
            }
        }
    }

    // Method for the add sensor button
    @FXML
    private void add_sensor(ActionEvent event) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("new_Sensor.fxml"));
            Parent root1 = (Parent) fxmlLoader.load();
            Stage stage_add = new Stage();
            stage_add.initModality(Modality.APPLICATION_MODAL);
            //stage.initStyle(StageStyle.UNDECORATED);

            stage_add.setTitle("Add Sensor");
            stage_add.setScene(new Scene(root1));

            stage_add.show();
        } catch (IOException ex) {

        }
    }

    @FXML
    private void configure_db(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ConfigDb.fxml"));

        Parent root1 = (Parent) fxmlLoader.load();

        Stage stage_add = new Stage();
        stage_add.initModality(Modality.APPLICATION_MODAL);
        //stage.initStyle(StageStyle.UNDECORATED);
        stage_add.setTitle("Configure DB");
        stage_add.setScene(new Scene(root1));

        stage_add.show();

    }
    // This method retrives specifications for a sensor and connects it to specific Node.
    @FXML
    private void search_sensor(MouseEvent event) {
        // You can only set Sensor nodes if the program is stopped.
        if (on_off) {
            Rectangle r = (Rectangle) event.getSource();
            System.out.println(r.getId());
            try {
                Get_sensorController.s = Integer.parseInt(r.getId().substring(1));
                // Open the Get_sensor class
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Get_sensor.fxml"));

                Parent root1 = (Parent) fxmlLoader.load();

                Stage stage_add = new Stage();
                stage_add.initModality(Modality.APPLICATION_MODAL);
                //stage.initStyle(StageStyle.UNDECORATED);
                stage_add.setTitle(r.getId().substring(1) + " | Get Sensor");
                stage_add.setScene(new Scene(root1));

                stage_add.show();

            } catch (IOException ex) {

            }
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (on_off) {
            add_sensor_b.setDisable(true);
            config_db_b.setDisable(true);
            try {
                int sq = sql.start(UserName, UserPass, PortNr, IP_address, Schema);
                if (sq == -1) {
                    ldba.setText("DB Error");
                } else {
                    ldbb.setText("Session: " + sq);
                    ldba.setText("DB Connected");
                }
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
            Light.Distant light = new Light.Distant();
//        light.setAzimuth(45.0);
//        light.setElevation(30.0);
            light.setColor(Color.valueOf("#ff4f4f"));

            Lighting lighting = new Lighting();
            lighting.setLight(light);
            lighting.setDiffuseConstant(2.0);
            Start.setEffect(lighting);
            Start.setText("Stop");
            on_off = false;
            test = false;
            socket = null;

            socket_OnOff = true;
            if (thread == null || thread1 == null) {
                thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            WirelessModule();
                        } catch (Exception ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                thread.start();
                thread1 = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Simulink();
                        } catch (IOException | InterruptedException ex) {
                            // Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                thread1.start();
            }
        } else {

            simulink = true;

            thread1.interrupt();
            socket_OnOff = false;
            test = true;
            while (thread1.isAlive()) {
                //System.out.println("Alive");
            }
            System.out.println("Dead");
            while (thread.isAlive()) {
                //System.out.println("Blue Alive");
            }
            thread = null;
            thread1 = null;
            System.out.println("Blue Dead");
            Light.Distant light = new Light.Distant();
//        light.setAzimuth(45.0);
//        light.setElevation(30.0);
            light.setColor(Color.valueOf("#32ff3c"));

            Lighting lighting = new Lighting();
            lighting.setLight(light);
            lighting.setDiffuseConstant(2.0);
            Start.setEffect(lighting);
            Start.setText("Start");
            sql.close();
            ldba.setText("DB Disconnected");
            ldbb.setText("");
            on_off = true;
            System.out.println("");
            System.out.println("");
//            try {
//                socket1.close();
//            } catch (IOException ex) {
//                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            socket1=null;
            add_sensor_b.setDisable(false);
            config_db_b.setDisable(false);
            for (int i = 0; i < sensor_value.length; i++) {
                for (int j = 0; j < 6; j++) {
                    sensor_value[i][j] = 0;

                }

            }
            for (int i = 0; i < i2c_size.length; i++) {
                i2c_size[i] = 1;
            }
        }
    }

    public void Simulink() throws IOException, InterruptedException {
        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(8080);
                //new ServerSocket(9090, 0, InetAddress.getByName("localhost"))
            }
            serverSocket.setSoTimeout(10000);
            while (socket == null && socket_OnOff) {
                try {
                    socket = serverSocket.accept();
                } catch (Exception ex) {

                }
                //System.out.println("Socket: " +socket);
            }
            BufferedOutputStream bo = (new BufferedOutputStream(socket.getOutputStream()));
            System.out.println("Connected");
            L9b_s = "Simulink Connected";
            //PrintWriter pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
            //BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {

//                if (i % 50 == 0) {
//                    s1 = (short) (Math.random() * 1000);
//                    s2 = (short) (Math.random() * 1000);
//                }
                //System.out.println(br.readLine());
                for (int i = 0; i < size; i++) {
                    byte[] bytes = ByteBuffer.allocate(2).putShort(sensor_value[i][0]).array();
                    bo.write(bytes);

                    bo.flush();
                }
                //  System.out.println(new Timestamp(System.currentTimeMillis()));
//                sql.add("1", s1);
//                sql.add("2", s2);
                if (Thread.currentThread().isInterrupted()) {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                    L9b_s = "Simulink Disconnected";

                    break;
                }
                Thread.sleep(100);
            }
        } catch (Exception ex) {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }

            //    ex.printStackTrace();
            System.out.println("Simulink Disconnect");
            L9b_s = "Simulink Disconnected";
        }
    }

    public void WirelessModule() throws Exception {
        System.out.println("Ready");
        try {
            if (wireless_module_l.getText().equalsIgnoreCase("BlueTooth")) {
                Bluetooth Blue = new Bluetooth();

                if (sc == null) {
                    sc = Blue.go(WIFI_TF.getText());

                    Reader_Blue = new BufferedReader(new InputStreamReader(sc.openInputStream()));
                    Writer_Blue = new BufferedWriter(new OutputStreamWriter(sc.openOutputStream()));
                    // PrintWriter pw = new PrintWriter(new BufferedOutputStream(socket1.getOutputStream()));
                    reader = Reader_Blue;
                    writer = Writer_Blue;
                    System.out.println("hej");
                } else {
                    //sc.openInputStream();
                    reader = Reader_Blue;
                    writer = Writer_Blue;
                }

            } else {
                if (socket1 == null) {
                    System.out.println(WIFI_TF.getText());
                    socket1 = new Socket(WIFI_TF.getText(), 8800);
                    System.out.println("Socket");
                    Writer_Wifi = new BufferedWriter(new OutputStreamWriter(socket1.getOutputStream()));
                    Reader_Wifi = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
                    reader = Reader_Wifi;
                    writer = Writer_Wifi;
                    System.out.println("hej");
                } else {
                    reader = Reader_Wifi;
                    writer = Writer_Wifi;
                }
            }
            int delay_data = 10;
            try {
                // s: 0.8  (1/0.8)*10= 100

                // s: 10   (1/10)*10= 1
                delay_data = (int) ((1 / Double.parseDouble(delay.getText())) * 10);
            } catch (Exception ex) {
                delay_data = 10;
                delay.setText("1");
            }
            System.out.println("Sample rate: " + delay_data);
            writer.write(0x01);
            writer.write(delay_data);

            for (int i = 0; i < id.length; i++) {
                if (sensor_on[i]) {
                    switch (sql.getInterface(id[i])) {
                        case "Analog_digital":
                            System.out.println("Analog!");
                            String sss = (sql.getMethod(id[i]));
                            if (sss.equalsIgnoreCase("Simple Read")) {
                                writer.write(2);
                                writer.write(0);
                            } else {
                                writer.write(1);
                                writer.write(0);
                            }
                            break;
                        case "I2C":
                            System.out.println("I2c");
                            String a = sql.getAddr(id[i]);
                            String b = sql.getWReg(id[i]);
                            String c = sql.getWData(id[i]);
                            String d = sql.getReg(id[i]);
                            i2c_size[i - 6] = d.length() / 2;
                            int W_size = b.length() / 2;
                            System.out.println("i2c_size: " + i2c_size[i - 6]);
                            if (b.equalsIgnoreCase("")) {
                                b = "ff";
                            }
                            if (c.equalsIgnoreCase("")) {
                                c = "ff";
                            }
                            System.out.println("a: " + a);
                            System.out.println("b: " + b);
                            System.out.println("c: " + c);
                            System.out.println("d: " + d);

                            writer.write(new BigInteger(a.charAt(1) + "", 16).toByteArray()[0]);
                            writer.write(new BigInteger(a.charAt(0) + "", 16).toByteArray()[0]);
                            System.out.println("w_size: " + W_size);
                            writer.write(i2c_size[i - 6]);
                            writer.write(W_size);
                            if (W_size > 0) {
                                for (int j = 0; j < W_size * 2; j += 2) {
                                    writer.write(new BigInteger(b.charAt(j + 1) + "", 16).toByteArray()[0]);
                                    writer.write(new BigInteger(b.charAt(j) + "", 16).toByteArray()[0]);

                                    writer.write(new BigInteger(c.charAt(j + 1) + "", 16).toByteArray()[0]);
                                    writer.write(new BigInteger(c.charAt(j) + "", 16).toByteArray()[0]);
                                }

                            }

                            for (int j = 0; j < i2c_size[i - 6] * 2; j += 2) {
                                System.out.println("h: " + d.charAt(j) + d.charAt(j + 1));
                                writer.write(new BigInteger(d.charAt(j + 1) + "", 16).toByteArray()[0]);
                                writer.write(new BigInteger(d.charAt(j) + "", 16).toByteArray()[0]);
                            }

                            break;
                        case "SPI":
                            System.out.println("SPI");
                            String spi_Wreg = sql.getWReg(id[i]);
                            String spi_Wdata = sql.getWData(id[i]);
                            String spi_Rreg = sql.getReg(id[i]);
                            System.out.println(spi_Rreg);
                            i2c_size[i - 6] = spi_Rreg.length() / 2;
                            int spi_Wsize = spi_Wdata.length() / 2;
                            writer.write(new BigInteger(spi_Rreg.charAt(1) + "", 16).toByteArray()[0]);
                            writer.write(new BigInteger(spi_Rreg.charAt(0) + "", 16).toByteArray()[0]);

                            writer.write(i2c_size[i - 6]);
                            writer.write(spi_Wsize);

                            for (int j = 2; j < i2c_size[i - 6] * 2; j += 2) {
                                writer.write(new BigInteger(spi_Rreg.charAt(j + 1) + "", 16).toByteArray()[0]);
                                writer.write(new BigInteger(spi_Rreg.charAt(j) + "", 16).toByteArray()[0]);
                            }

                            if (spi_Wsize > 0) {
                                for (int j = 0; j < spi_Wsize * 2; j += 2) {
                                    writer.write(new BigInteger(spi_Wreg.charAt(j + 1) + "", 16).toByteArray()[0]);
                                    writer.write(new BigInteger(spi_Wreg.charAt(j) + "", 16).toByteArray()[0]);

                                    writer.write(new BigInteger(spi_Wdata.charAt(j + 1) + "", 16).toByteArray()[0]);
                                    writer.write(new BigInteger(spi_Wdata.charAt(j) + "", 16).toByteArray()[0]);
                                }
                            }

                            break;
                        default:
                            break;

                    }

                } else {
                    writer.write(new BigInteger("f", 16).toByteArray()[0]);
                    writer.write(new BigInteger("f", 16).toByteArray()[0]);
                }
            }
//            writer.write(2);
//            writer.write(1);
//            writer.write(0x30);
//            writer.write(0x29);
            writer.flush();
            System.out.println("Go");
            L9a_s = "Wireless Connected";
            String line = "";
            int g = 0;
            int h = 0;
            int i = 0;
            int j = 0;
            retry = true;
            while (true) {
                //  System.out.println("ff");
                char c = (char) reader.read();
                if (((int) c) == 65535) {
                    sc = null;
                    WirelessModule();
                }
                switch (c) {
                    case 'a':
                        sensor_value[0][0] = Short.parseShort(new StringBuffer(line).reverse().toString());
                        line = "";
                        break;
                    case 'b':
                        sensor_value[1][0] = Short.parseShort(new StringBuffer(line).reverse().toString());
                        line = "";
                        break;
                    case 'c':
                        sensor_value[2][0] = Short.parseShort(new StringBuffer(line).reverse().toString());
                        line = "";
                        break;
                    case 'd':
                        sensor_value[3][0] = Short.parseShort(new StringBuffer(line).reverse().toString());
                        line = "";
                        break;
                    case 'e':
                        sensor_value[4][0] = Short.parseShort(new StringBuffer(line).reverse().toString());
                        line = "";
                        break;
                    case 'f':
                        sensor_value[5][0] = Short.parseShort(new StringBuffer(line).reverse().toString());
                        line = "";
                        break;
                    case 'g':
                        int tmp = Integer.parseInt(new StringBuffer(line).reverse().toString()) & 0xFF;
                        tmp = (tmp & 0x80) == 0 ? tmp : tmp - 256;
                        System.out.println(tmp);
                        //System.out.println("Acc: " + tmp);

                        sensor_value[6][g] = (short) tmp;
                        g++;
                        line = "";
                        break;
                    case 'h':
                        int tmp1 = Integer.parseInt(new StringBuffer(line).reverse().toString()) & 0xFF;
                        tmp1 = (tmp1 & 0x80) == 0 ? tmp1 : tmp1 - 256;
                        //System.out.println("Acc: " + tmp);
                        sensor_value[7][h] = (short) tmp1;
                        h++;
                        line = "";
                        break;
                    case 'i':
                        sensor_value[8][i] = Short.parseShort(new StringBuffer(line).reverse().toString());
                        i++;
                        line = "";
                        break;
                    case 'j':
                        sensor_value[9][j] = Short.parseShort(new StringBuffer(line).reverse().toString());
                        j++;
                        line = "";
                        break;
                    case 'y':
                        simulink = true;
                        mutex.acquire();
                        g = 0;
                        h = 0;
                        i = 0;
                        j = 0;
                        sql.add_value(sensor_value, sensor_on, id, i2c_size);
                        mutex.release();
                        line = "";
                        break;
                    case 'x':
                        writer.write(0);
                        writer.flush();
                        line = "";
                        break;
                    default:
                        line += c;
                        continue;
                }
                if (test) {
                    L9a_s = "Wireless Disconnected";
                    System.out.println("heyy");
                    writer.write(1);
                    writer.flush();
                    break;
                }
            }
        } catch (Exception ex) {
//            writer.write(1);
//            writer.flush();
//            ex.printStackTrace();
            L9a_s = "Wireless Disconnected";
            System.out.println("Wireless connection error");
            if (wireless_module_l.getText().equalsIgnoreCase("BlueTooth")) {
                sc = null;
            } else {
                socket1 = null;
            }
            if (retry) {
                retry = false;
                WirelessModule();
            }
        }
    }
}
