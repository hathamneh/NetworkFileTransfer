package com.company;

import java.awt.*;
import java.io.*;
import java.net.*;

public class Main {

    private static final int PORT = 5555; // server port

    static Socket socket; // socket used to connect
    static BufferedReader reader; // reader used to read responses from server
    static BufferedReader userRead; // reader used to read command from user
    static PrintWriter writer; // writer used to write requests to server

    static boolean loggedin = false; // Is user logged in?

    public static void main(String[] args) {
        String path = ""; // current path
        try {
            // connecting to socket and initialize things
            socket = new Socket("127.0.0.1", PORT);
            OutputStream socketOut = socket.getOutputStream();
            InputStream socketIn = socket.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(socketIn));
            userRead = new BufferedReader(new InputStreamReader(System.in));
            writer = new PrintWriter(new OutputStreamWriter(socketOut));

            // print welcome msg from server
            String line = reader.readLine();
            while (!line.equals(">")) {
                System.out.println(line);
                line = reader.readLine();
            }

            // some objects used
            String cmd; // hold user raw command
            Request request; // hold user command to be send to the server
            String rawResponse; // server response as raw text
            Response response; // processed server response

            try {
                // start waiting for commands from user console
                while (true) {

                    System.out.print((loggedin ? path : "") + "> "); // print console ready character
                    cmd = userRead.readLine(); // wait for command
                    request = Request.parseRequest(cmd); // process the command

                    if (request.isCommand()) { // does the user entered a real command

                        // if command is upload make sure the file exists
                        if (request.isUpload() && !request.fileExists()) {
                            System.err.println("File Not Found!!");
                            continue;
                        }

                        // send command to the server
                        writer.println(request.getCmd());
                        writer.flush();

                        // read server response and process it
                        rawResponse = reader.readLine();
                        if (rawResponse != null)
                            response = Response.parseResponse(rawResponse);
                        else
                            throw new ConnectException("Connection lost !");

                        // terminate and close if user send exit command
                        if (response.status == Response.EXIT_STAT) {
                            response.printMsg();
                            socket.close();
                            System.exit(0);
                        }

                        // choose what to do based on response status received from server
                        switch (response.status) {
                            case Response.LOGIN_STAT:  // logged in successfully
                                clearScreen();
                                response.printMsg();
                                path = response.currentPath;
                                loggedin = true;

                                break;
                            case Response.SIGNUP_STAT:  // signed up successfully
                                clearScreen();
                                response.printMsg();
                                path = response.currentPath;
                                loggedin = true;
                                break;
                            case Response.LOGOUT_STAT:  // logged out successfully
                                path = "";
                                loggedin = false;
                                response.printMsg();
                                break;
                            case Response.PASSWD_STAT:  // change password request

                                response.printMsg();
                                String oldPass = userRead.readLine();
                                writer.println(oldPass);
                                writer.flush();
                                rawResponse = reader.readLine();
                                response = Response.parseResponse(rawResponse);
                                response.printMsg();
                                if(!response.isErr()) {
                                    String newPass1 = userRead.readLine();
                                    System.out.print("one more: ");
                                    String newPass2 = userRead.readLine();
                                    if (newPass1.equals(newPass2)) {
                                        writer.println("yes");
                                        writer.flush();
                                        writer.println(newPass1);
                                        writer.flush();
                                        rawResponse = reader.readLine();
                                        response = Response.parseResponse(rawResponse);
                                        response.printMsg();
                                    } else {
                                        writer.println("no");
                                        writer.flush();
                                        System.err.println("The passwords are not equal!");
                                        System.err.flush();
                                        Thread.sleep(100);
                                    }
                                }
                                break;
                            case Response.CD_STAT:  // directory changed successfully
                                path = response.currentPath;
                                if (!"".equals(response.msg))
                                    response.printMsg();
                                break;
                            case Response.UPLD_STAT: // Upload operation accepted, start uploading
                                try {
                                    // send file through socket
                                    request.sendFile(socketOut);
                                    // wait acknowledgment
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    response.printMsg();
                                } catch (FileNotFoundException e1) {
                                    System.err.println("File Not Found!!");
                                } catch (IOException e1) {
                                    System.err.println("Some error happened :(");
                                }
                                break;
                            case Response.DNLD_STAT: // Download operation accepted, start downloading
                                try {
                                    response.printMsg();
                                    // get file size to be downloaded from server
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    int fsize;
                                    if (response.status == Response.FIL_SIZ)
                                        fsize = Integer.parseInt(response.msg);
                                    else
                                        throw new Exception("Couldn't receive file size from server!");
                                    // download the file
                                    request.receiveFile(socketIn, fsize);
                                    // send acknowledgment that the file is fully received
                                    writer.println("0");
                                    writer.flush();
                                    // wait until server finish things up
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    response.printMsg();

                                } catch (FileNotFoundException e1) {
                                    System.err.println("File Not Found!!");
                                } catch (Exception e1) {
                                    e1.printStackTrace();
                                    System.err.println("Some error happened !");
                                }
                                break;
                            case Response.EDT_STAT: // File edit accepted, start editing
                                response.printMsg();
                                try {
                                    // get file size to be edited
                                    rawResponse = reader.readLine();
                                    response = Response.parseResponse(rawResponse);
                                    int fsize;
                                    if (response.status == Response.FIL_SIZ)
                                        fsize = Integer.parseInt(response.msg);
                                    else
                                        throw new Exception("Couldn't receive file size from server!");
                                    // download the file
                                    File file = request.receiveFile(socketIn, fsize);
                                    System.out.println("- Note: You only can edit files as text.");
                                    // open file using default application
                                    Desktop.getDesktop().open(file);
                                    // enter key to determine end of editing
                                    System.out.print("Press Enter after finish editing> ");
                                    userRead.readLine();
                                    // send modified version
                                    request.updateFile(socketOut);
                                    // wait for acknowledgment
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
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("Can't connect please make sure the server is UP!");
        }
    }

    /**
     * Clear console screen
     */
    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
