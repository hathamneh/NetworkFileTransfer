package com.company;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Main {

    private static final int PORT = 5555;
    static BufferedReader reader;
    static Socket socket;
    static BufferedReader userRead;
    static PrintWriter writer;
    static boolean loggedin = false;

    public static void main(String[] args) {
        String path = "";
        try {
            socket = new Socket("127.0.0.1", PORT);
            reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();
            while (!line.equals(">")) {
                System.out.println(line);
                line = reader.readLine();
            }
            //System.out.print(line + " ");
            userRead = new BufferedReader(new InputStreamReader(System.in));
            OutputStream socketOut = socket.getOutputStream();
            String rawResponse;
            try {
                while (true) {
                    System.out.print((loggedin ? path : "") + "> ");

                    writer = new PrintWriter(new OutputStreamWriter(socketOut));
                    Response response;
                    String cmd = userRead.readLine();

                    Request request = Request.parseRequest(cmd);

                    if (request.isCommand()) {

                        if (request.isUpload() && !request.fileExists()) {
                            System.err.println("File Not Found!!");
                            continue;
                        }

                        //System.out.println(request.getCmd());
                        writer.println(request.getCmd());
                        writer.flush();

                        if (request.isUpload()) {
                            try {
                                request.sendFile(socketOut);
                            } catch (FileNotFoundException e1) {
                                System.err.println("File Not Found!!");
                            } catch (IOException e1) {
                                System.err.println("Some error happened :(");
                            }
                        }
                        if (request.isDownload()) {
                            try {
                                request.recieveFile(socket.getInputStream());
                            } catch (FileNotFoundException e1) {
                                System.err.println("File Not Found!!");
                            } catch (IOException e1) {
                                System.err.println("Some error happened :(");
                            }
                        }

                        rawResponse = reader.readLine();
                        //System.out.println(rawResponse);
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
                                clearScreen();
                                response.printMsg();
                                path = response.currentPath;
                                loggedin = true;

                                break;
                            case 11:  // status 11 is for signup command
                                response.printMsg();
                                path = response.currentPath;
                                break;
                            case 9:  // status 9 is for logout
                                path = "";
                                loggedin = false;
                                response.printMsg();
                                break;
                            case 2:  // status 2 is for cd command
                                path = response.currentPath;
                                if (!"".equals(response.msg))
                                    response.printMsg();
                                break;
                            default:
                                path = response.currentPath;
                                response.printMsg();
                                break;
                        }
                    }
                    //System.out.print((loggedin ? path : "") + "> ");
                }
            } catch (ConnectException ce) {
                System.err.println(ce.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Can't connect please make sure the server is UP!");
        }
    }


    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
