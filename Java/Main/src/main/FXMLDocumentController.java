/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.Color;
import javax.microedition.io.StreamConnection;

/**
 *
 * @author Rilind
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Label L1;
    @FXML
    private Label L2;
    @FXML
    private Button Start;
    private short s1 = 0;
    private short s2 = 0;
    private boolean test = false;
    private boolean on_off = true;
    private Thread thread1 = null;
    private Thread thread = null;
    private Socket socket;
    private SQL sql = new SQL();
    ;
    private StreamConnection sc = null;
    private ServerSocket serverSocket;
    private int SampleRate = 1000;
    private BufferedReader reader = null;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (on_off) {
            try {
                sql.start();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
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
            if (thread == null || thread1 == null) {
                thread = new Thread() {
                    public void run() {
                        try {
                            Blue();
                        } catch (Exception ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                thread.start();
                thread1 = new Thread() {
                    public void run() {
                        try {
                            Simulink();
                        } catch (Exception ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                thread1.start();
            }
        } else {
            
            thread1.interrupt();
            test = true;
            while (thread1.isAlive()) {
                //System.out.println("Alive");
            }
            System.out.println("Dead");
            while (thread.isAlive()) {
                //System.out.println("Blue Alive");
            }
            thread = null;
            thread = null;
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
            on_off = true;
            System.out.println("");
            System.out.println("");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO

        Task task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                int i = 0;
                while (true) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            L1.setText(s1 + "");
                            L2.setText(s2 + "");
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

    public void Simulink() throws IOException, InterruptedException {
        try {
            if (serverSocket == null) {
                serverSocket = new ServerSocket(8080);
                //new ServerSocket(9090, 0, InetAddress.getByName("localhost"))
            }
            serverSocket.setSoTimeout(10000);
            socket = serverSocket.accept();
            System.out.println("Connected");
            int i = 0;
            while (true) {

                BufferedOutputStream bo = (new BufferedOutputStream(socket.getOutputStream()));
                PrintWriter pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                if (i % 50 == 0) {
//                    s1 = (short) (Math.random() * 1000);
//                    s2 = (short) (Math.random() * 1000);
//                }
                byte[] bytes = ByteBuffer.allocate(2).putShort(s1).array();
                byte[] bytes1 = ByteBuffer.allocate(2).putShort(s2).array();
                bo.write(bytes);
                bo.write(bytes1);
                bo.flush();
//                sql.add("1", s1);
//                sql.add("2", s2);
                Thread.sleep(100);
                i++;
                if (Thread.currentThread().isInterrupted()) {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        serverSocket.close();
                        serverSocket = null;
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }

            //    ex.printStackTrace();
            System.out.println("Simulink Disconnect");
        }
    }

    public void Blue() throws Exception {
        System.out.println("Ready");
        try {
            Bluetooth Blue = new Bluetooth();

            if (sc == null) {
                sc = Blue.go();
                reader = new BufferedReader(new InputStreamReader(sc.openInputStream()));
            }

            System.out.println("Go");
            String line = "";
            while (true) {

                char c = (char) reader.read();
                switch (c) {
                    case 'a':
                        s1 = Short.parseShort(new StringBuffer(line).reverse().toString());
                        line = "";
                        break;
                    case 'b':
                        int tmp = Integer.parseInt(new StringBuffer(line).reverse().toString()) & 0xFF;
                        tmp = (tmp & 0x80) == 0 ? tmp : tmp - 256;
                        //System.out.println("Acc: " + tmp);
                        s2 = (short) tmp;
                        line = "";
                        break;
                    default:
                        line += c;
                        continue;
                }
                if (test) {
                    break;
                }
            }
        } catch (Exception ex) {
            // ex.printStackTrace();
            System.out.println("Wireless connection error");
        }
    }

    public void Wifi() throws IOException {
        String tt = "";
        while (true) {

            Socket socket1 = new Socket("192.168.1.13", 80);

            PrintWriter pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
            BufferedInputStream bi = new BufferedInputStream(socket.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //    System.out.println("Hej");
            //while((tmp=(char)br.read())!='0'){
            //    System.out.println(tmp);
            //  }
            String s;
            while ((s = br.readLine()) != null) {
                if (!s.equals(tt)) {
                    System.out.println(new StringBuffer(s).reverse().toString());
                }
                // System.out.println("Då");
                tt = s;
                break;
            }
            // System.out.println(tt + " asf");
        }
    }

}
