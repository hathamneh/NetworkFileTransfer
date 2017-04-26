package com.company;

import java.io.BufferedWriter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * ConnectionManager
 * Created by haitham on 4/20/17.
 */
public class ConnectionManager extends Thread {

    private static final int PORT = 5555;

    Vector<Connection> connections = new Vector<>();
    ServerSocket serverSocket;

    ConnectionManager() {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        listen();
    }

    public void listen() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                Thread conn = new Connection(socket);
                System.out.println("Connection Established");
                conn.start();
            } catch (IOException e) {
                //e.printStackTrace();
                System.err.println(e);
            }
        }
    }


}
