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
import java.util.Calendar;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Rilind
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String tt = "";
        launch(args);
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
