package com.company;

import java.io.*;
import java.net.*;

public class Main {

    private static final int PORT = 5555;
    static BufferedReader reader;
    static Socket socket;
    static BufferedReader userRead;
    static PrintWriter writer;

    public static void main(String[] args) {

        String path = "";
        try {
            socket = new Socket("127.0.0.1",PORT);
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            while(!line.equals(">")){
                System.out.println(line);
                line = reader.readLine();
            }
            System.out.print(line + " ");
            userRead = new BufferedReader( new InputStreamReader(System.in) );
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            try {
                while (true) {
                    Response response;
                    String cmd = userRead.readLine();
                    if (cmd != null && !"".equals(cmd)) {
                        writer.println(cmd);
                        writer.flush();
                        String rawResponse = reader.readLine();
                        if (rawResponse != null)
                            response = Response.parseResponse(rawResponse);
                        else
                            throw new ConnectException("Connection lost !");

                        if (response.status == 1) {
                            response.printMsg();
                            socket.close();
                            System.exit(0);
                        }

                        switch (response.status) {
                            case 10:  // status 10 is for login command
                                response.printMsg();
                                path = response.currentPath;
                                break;
                            case 11:  // status 11 is for signup command
                                response.printMsg();
                                path = response.currentPath;
                                break;
                            case 9:  // status 9 is for logout
                                path = "";
                                response.printMsg();
                                break;
                            case 2:  // status 2 is for cd command
                                path = response.currentPath;
                                if (!"".equals(response.msg))
                                    response.printMsg();
                                break;
                            default:
                                response.printMsg();
                                break;
                        }
                    }
                    System.err.flush();
                    System.out.print(path + "> ");
                }
            } catch (ConnectException ce) {
                System.err.println(ce.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Can't connect please make sure the server is UP!");
        }
    }

}
