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
            OutputStream socketOut =socket.getOutputStream();
            String rawResponse = "";
            try {
                while (true) {
                    writer = new PrintWriter(new OutputStreamWriter(socketOut));
                    Response response;
                    String cmd = userRead.readLine();

                    String[] cmd_fields = cmd.split(" ");
                    File file = null;
                    int fileSize = 0;
                    if("upload".equals(cmd_fields[0]) && cmd_fields.length == 2) {
                        file = new File(cmd_fields[1]);
                        fileSize = (int) file.length();
                        cmd = "upload "+file.getAbsolutePath()+" "+fileSize;

                    }
                    System.out.println("----------------");

                    if (cmd != null && !("".equals(cmd))) {
                        System.out.println(cmd);

                        if(null != file && !file.exists()) {
                            System.err.println("File Not Found!!!!!!!!");
                        }
                        if(!"upload".equals(cmd_fields[0])) {
                            writer.println(cmd);
                            writer.flush();
                            rawResponse = reader.readLine();
                        }


                        if("upload".equals(cmd_fields[0]) && cmd_fields.length == 2) {
                            writer.println(cmd);
                            writer.flush();

                            if(null != file && file.exists()) {
                                FileInputStream bis;

                                try {
                                    bis = new FileInputStream(file);
                                    byte[] fileByts = new byte[fileSize];
                                    int count;
                                    while((count = bis.read(fileByts)) > 0){}
                                    socketOut.write(fileByts);
                                    socketOut.flush();
                                    rawResponse = reader.readLine();
                                } catch (FileNotFoundException e1) {
                                    e1.printStackTrace();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }

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
                                path = response.currentPath;
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
