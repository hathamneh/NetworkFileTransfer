package com.company;

import java.io.*;
import java.net.Socket;

/**
 * Connection
 * Created by haitham on 4/20/17.
 */
public class Connection extends Thread {
    private static final int EXIT_STAT = 1;
    private static final int CD_STAT = 2;
    private static final int SIGNUP_STAT = 11;
    private static final int LOGIN_STAT = 10;
    private static final int LOGOUT_STAT = 9;
    private static final int ERR_STAT = -1;

    private Client user;
    private Socket socket;
    private String currentPath="/";

    public Connection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            PrintWriter writer = new PrintWriter( new OutputStreamWriter(socket.getOutputStream()));
            writer.println("Welcome to our File Transfer Service");
            writer.println("to get started you should login, if you dont have an account yet yo can register now for free.");
            writer.println("--------------------------");
            writer.println("type \"login\" or \"signup\"");
            writer.println(">");
            writer.flush();
            String command;
            while (true) {
                command = readCmd();
                if(command == null || "exit".equals(command)) {
                    terminateConnection();
                    break;
                }
                execCmd(command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String cd(String path) {
        path = MyFile.filterPath(path);
        String tmp_path="";
        switch (path) {
            case "../":
                if(!currentPath.equals(FileManager.root_path)) {
                    tmp_path = currentPath.substring(0,
                            currentPath.substring(0 , currentPath.length() - 1).lastIndexOf("/")+1);
                    if(tmp_path.equals(""))
                        tmp_path = "/";
                    break;
                }
                System.err.println("You are at root path: "+pwd());
                break;
            case "/":
                tmp_path = "/";
                break;
            default:
                File tmpFile = new File(FileManager.root_path + currentPath + path);
                if(tmpFile.exists()) {
                    tmp_path = currentPath + path;
                    break;
                }

                System.err.println("Path does not exist!");
        }
        if(!(user.getAccessRights() == Client.A_RESTR
                && tmp_path.equals("/"))){
            currentPath = tmp_path;
            return null;
        } else {
            return "YOU HAVE NO ACCESS TO THIS FOLDER, THIS ACTION WILL BE REPORTED !!!";
        }
    }

    public String pwd() {
        return currentPath;
    }

    public String readCmd() {
        BufferedReader reader ;
        try {
            reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeMsg(String msg) {
        writeToUser(0,msg);
    }
    public void writeErr(String error) {
        writeToUser(ERR_STAT, error);
    }
    public void writeToUser(int code, String msg) {
        try {
            PrintWriter writer = new PrintWriter( new OutputStreamWriter(socket.getOutputStream()));
            writer.println(code+";"+msg+";"+currentPath);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void execCmd(String cmd) {
        if(user != null) {
            String[] cmd_fields = cmd.split(" ");
            switch (cmd_fields[0].trim()) {
                case "pwd":
                    System.out.println(pwd());
                    writeToUser(0,pwd());
                    break;
                case "cd":
                    String msg = cd(cmd_fields[1]);
                    if(msg == null)
                        writeToUser(CD_STAT,"");
                    else
                        writeErr(msg);
                    break;
                case "mkdir":

                    break;
                case "rm":

                    break;
                case "upload":

                    break;
                case "download":

                    break;
                case "edit":

                    break;
                case "logout":
                    UsersManager.logout(user);
                    user = null;
                    currentPath = "/";
                    writeToUser(LOGOUT_STAT,"Logged out");
                    break;
                default:
                    cmdNotFoundMsg();
            }
        } else {
            execStartCommand(cmd);
        }

    }

    public void execStartCommand(String cmd) {
        String[] cmd_fields = cmd.split(" ");
        switch (cmd_fields[0].trim()) {
            case "login":
                if(cmd_fields.length != 3) {
                    writeErr("Missing parameter !");
                    break;
                }
                try {
                    user = UsersManager.login(cmd_fields[1], cmd_fields[2]);
                    System.out.println("logged in user " + user.getFirstName());
                    cd(user.getHome());
                    writeToUser(LOGIN_STAT,"Welcome "+user.getFirstName());
                } catch (IllegalArgumentException e) {
                    System.err.println(e);
                    writeErr(e.getMessage());
                }
                break;
            case "signup":
                if(cmd_fields.length != 6) {
                    writeErr("Missing parameter !");
                    break;
                }
                byte acc;
                if("ALL".equals(cmd_fields[5].toUpperCase()))
                    acc = Client.A_ALL;
                else if("RESTR".equals(cmd_fields[5].toUpperCase()))
                    acc = Client.A_RESTR;
                else {
                    writeErr("Access rights is wrong format.");
                    break;
                }
                try {
                    user = UsersManager.register(cmd_fields[1], cmd_fields[2], cmd_fields[3], cmd_fields[4], acc);
                    System.out.println("user '"+user.getFirstName()+"' has registered.");
                    cd(user.getHome());
                    writeToUser(SIGNUP_STAT, "You have successfully signed up.");
                }
                catch (Exception e) {
                    writeErr(e.getMessage());
                    //System.err.println(e);
                    e.printStackTrace();
                }
                break;
            default:
                cmdNotFoundMsg();
        }
    }

    private void cmdNotFoundMsg() {
        writeErr("Command not found.\\nType 'help' to see all commands.\\nType 'help $commandName$' to command manual.");
    }

    public void terminateConnection() {
        try {
            if(user != null)
                UsersManager.logout(user);
            writeToUser(EXIT_STAT,"Good bye");
            socket.close();
            System.out.println("Connection Terminated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
