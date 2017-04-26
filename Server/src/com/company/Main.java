package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        ConnectionManager connectionManager = new ConnectionManager();
        System.out.println("Server is listening ...");
        connectionManager.start();
        while(true) {
            BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
            try {
                String serverCmd = reader.readLine();
                ServerAdmin.execute(serverCmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
