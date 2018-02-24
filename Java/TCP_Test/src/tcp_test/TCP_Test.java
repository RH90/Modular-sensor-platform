/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcp_test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 *
 * @author Rilind
 */
public class TCP_Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO code application logic here
        int i =-400;
        int u = -200;
        ServerSocket serverSocket = new ServerSocket(8000);
        Socket socket = serverSocket.accept();
        System.out.println("Connected");
        BufferedOutputStream bo = (new BufferedOutputStream(socket.getOutputStream()));
        PrintWriter pw = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (true) {
            byte[] bytes = ByteBuffer.allocate(4).putInt(u).array();
            bo.write(bytes,0,4);
            bytes = ByteBuffer.allocate(4).putInt(i).array();
            bo.write(bytes,0,4);
            bo.flush();
            System.out.println(i);
            System.out.println(u);
            i = (i + 2);
            u = (u + 3);
            Thread.sleep(100);

            if (i >= 300) {
                i = -100;
                 u = -100;
            }

        }
    }

}
