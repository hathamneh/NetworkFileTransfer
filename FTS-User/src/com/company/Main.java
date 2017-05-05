package com.company;

import java.awt.*;
import java.io.*;
import java.net.*;

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

                        rawResponse = reader.readLine();
                        //System.out.println(rawResponse);
                        if (rawResponse != null)
                            response = Response.parseResponse(rawResponse);
                        else
                            throw new ConnectException("Connection lost !");

                        if (response.status == Response.EXIT_STAT) {
                            response.printMsg();
                            socket.close();
                            System.exit(0);
                        }

                        switch (response.status) {
                            case Response.LOGIN_STAT:  // status 10 is for login command
                                clearScreen();
                                response.printMsg();
                                path = response.currentPath;
                                loggedin = true;

                                break;
                            case Response.SIGNUP_STAT:  // status 11 is for signup command
                                clearScreen();
                                response.printMsg();
                                path = response.currentPath;
                                loggedin = true;
                                break;
                            case Response.LOGOUT_STAT:  // status 9 is for logout
                                path = "";
                                loggedin = false;
                                response.printMsg();
                                break;
                            case Response.CD_STAT:  // status 2 is for cd command
                                path = response.currentPath;
                                if (!"".equals(response.msg))
                                    response.printMsg();
                                break;
                            case Response.UPLD_STAT:
                                response.printMsg();
                                try {
                                    request.sendFile(socketOut);
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    response.printMsg();
                                } catch (FileNotFoundException e1) {
                                    System.err.println("File Not Found!!");
                                } catch (IOException e1) {
                                    System.err.println("Some error happened :(");
                                }
                                break;
                            case Response.DNLD_STAT:
                                try {
                                    response.printMsg();
                                    // get file size
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    int fsize;
                                    if(response.status == Response.FIL_SIZ)
                                        fsize = Integer.parseInt(response.msg);
                                    else
                                        throw new Exception("Couldn't receive file size from server!");
                                    //System.out.println(fsize);
                                    request.receiveFile(socket.getInputStream(), fsize);
                                    System.out.println("File fully received, sending ack");
                                    writer.println("0");
                                    writer.flush();
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    response.printMsg();
                                    socket.getInputStream().skip(socket.getInputStream().available());
                                } catch (FileNotFoundException e1) {
                                    System.err.println("File Not Found!!");
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    System.err.println("Some error happened !");
                                }
                                break;
                            case Response.EDT_STAT:
                                response.printMsg();
                                try {
                                    // get file size
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    int fsize;
                                    if(response.status == Response.FIL_SIZ)
                                        fsize = Integer.parseInt(response.msg);
                                    else
                                        throw new Exception("Couldn't receive file size from server!");

                                    File file = request.receiveFile(socket.getInputStream(), fsize);
                                    System.out.println("- Note: You only can edit files as text.");
                                    // open file using editor
                                    Desktop.getDesktop().open(file);
                                    System.out.print("Press Enter after finish editing> ");
                                    userRead.readLine();
                                    // send modified version
                                    request.updateFile(socket.getOutputStream());
                                    // wait for ack
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    response.printMsg();
                                } catch (ClassNotFoundException e) {
                                    System.err.println("File Not Found!!");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println("Some error happened !");
                                }
                                break;
                            default:
                                path = response.currentPath;
                                response.printMsg();
                        }
                    }
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
