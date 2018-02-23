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
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
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
    static String s1 = "";
    static String s2 = "";

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        L1.setText("Hello World!");
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
                            L1.setText(s1);
                            L2.setText(s2);
                        }

                    });
                    Thread.sleep(10);
                }
            }
        };
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();

        Thread thread = new Thread() {
            public void run() {
                try {
                    Blue();
                } catch (Exception ex) {
                    Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        thread.start();
        // L1.setText("hej");
    }

    public void Blue() throws Exception {
        System.out.println("Ready");
        s1 = "heu";
        Bluetooth Blue = new Bluetooth();

        StreamConnection sc = Blue.go();
        BufferedReader reader = new BufferedReader(new InputStreamReader(sc.openInputStream()));
        System.out.println("Go");
        String line = "";
        while (true) {
            char c = (char) reader.read();
            if (c == 'a') {
                s1 = (new StringBuffer(line).reverse().toString() + " cm");
                line = "";
            } else if (c == 'b') {
                int tmp = Integer.parseInt(new StringBuffer(line).reverse().toString()) & 0xFF;
                tmp = (tmp & 0x80) == 0 ? tmp : tmp - 256;
                //System.out.println("Acc: " + tmp);
                s2="X: "+tmp;
                line = "";
            } else {
                line += c;
                continue;
            }
        }
    }

    public void Wifi() throws IOException {
        String tt = "";
        while (true) {

            Socket socket = new Socket("192.168.1.13", 80);

            PrintWriter pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
            BufferedInputStream bi = new BufferedInputStream(socket.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //    System.out.println("Hej");
            //while((tmp=(char)br.read())!='0'){
            //    System.out.println(tmp);
            //  }
            String s = "";
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
