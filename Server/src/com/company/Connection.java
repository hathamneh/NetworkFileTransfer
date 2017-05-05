package com.company;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.AccessDeniedException;

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
    private static final int UPLD_STAT = 15;
    private static final int DNLD_STAT = 16;
    private static final int RM_STAT = 17;
    private static final int EDT_STAT = 18;
    private static final int FIL_SIZ = 19;

    private int id;
    private Client user;
    private Socket socket;
    private String currentPath = File.separator;

    public Connection(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.println("Welcome to our File Transfer Service");
            writer.println("Login or register to start having fun");
            writer.println("--------------------------");
            writer.println("type 'login' or 'signup', if you need help at any time enter 'help'");
            writer.println(">");
            writer.flush();
            String command;
            try {
                while (true) {
                    command = readCmd().trim();
                    if (command == null || "exit".equals(command)) {
                        terminateConnection();
                        break;
                    }
                    execCmd(command);
                }
            } catch (SocketException e) {
                System.err.println("Connection lost!");
            } catch (NullPointerException e) {
                System.err.println("Connection lost!");
                e.printStackTrace();
            } finally {
                if(user != null && UsersManager.isLoggedIn(user))UsersManager.loggedinUsers.remove(user.getId());
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String cd(String path) {
        path = File.filterPath(path);
        String tmp_path = "";
        if ((".."+File.separator).equals(path)) {
            if (!currentPath.equals(FileManager.root_path)) {
                tmp_path = currentPath.substring(0,
                        currentPath.substring(0, currentPath.length() - 1).lastIndexOf(File.separator) + 1);
                if (tmp_path.equals(""))
                    tmp_path = File.separator;
            }
            System.err.println("You are at root path: " + pwd());

        } else if (File.separator.equals(path)) {
            tmp_path = File.separator;

        } else {
            Folder tmpFolder;
            try {
                path = path.replace(File.separator, "");
                tmpFolder = new Folder(FileManager.root_path + currentPath + path + File.separator);
                if (tmpFolder.exists()) {
                    tmp_path = currentPath + path + File.separator;
                } else
                    return "No such file or directory, you can only move one level";
            } catch (IOException e) {
                sendErr(e.getMessage());
                e.printStackTrace();
            }

        }
        if (!(user.getAccessRights() == Client.A_RESTR
                && File.separator.equals(tmp_path))) {
            currentPath = tmp_path;
            return null;
        } else {
            return "YOU HAVE NO ACCESS TO THIS FOLDER, THIS ACTION WILL BE REPORTED !!!";
        }
    }

    public String pwd() {
        return currentPath;
    }

    public String readCmd() throws IOException {
        BufferedReader reader;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return reader.readLine();
    }

    public Object readObj() {
        ObjectInputStream reader;
        try {
            reader = new ObjectInputStream(socket.getInputStream());
            return reader.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendCurrentPath() {
        sendWithCode(0, "");
    }

    public void sendMsg(String msg) {
        sendWithCode(0, msg);
    }

    public void sendErr(String error) {
        sendWithCode(ERR_STAT, error);
    }

    public void sendWithCode(int code, String msg) {
        sendRaw(code + ";" + msg + ";" + currentPath);
    }

    public void sendRaw(String data) {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.println(data);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRaw(long number) {
        try {
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            writer.writeLong(number);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void execCmd(String cmd) {
        if (user != null) {
            System.out.println(cmd);
            String[] cmd_fields = cmd.split(" ");
            switch (cmd_fields[0].trim()) {
                case "pwd":
                    System.out.println(pwd());
                    sendWithCode(0, pwd());
                    break;
                case "cd":
                    if (cmd_fields.length < 2) {
                        sendErr("Please specify the path you want to change directory to.");
                        break;
                    }
                    String msg = cd(cmd_fields[1]);
                    if (msg == null)
                        sendWithCode(CD_STAT, "");
                    else
                        sendErr(msg);
                    break;
                case "mkdir":
                    if (cmd_fields.length == 2) {
                        FileManager.mkdir(cmd_fields[1], currentPath, user);
                        cd(cmd_fields[1]);
                        sendCurrentPath();
                    } else
                        sendErr("mkdir takes only one parameter");
                    break;
                case "ls":
                    try {
                        if (cmd_fields.length == 1) {
                            String[] files = FileManager.listFiles(currentPath);
                            String data = "";
                            for (String file :
                                    files) {
                                data += file + "\\t\\t";
                            }
                            sendMsg(data);
                        } else if (cmd_fields.length == 2 && cmd_fields[1].equals("-l")) {
                            String[] files = FileManager.listFilesMore(currentPath);
                            String data = "";
                            for (String file :
                                    files) {
                                data += file + "\\n";
                            }
                            sendMsg(data);
                        }
                    } catch (Exception e) {
                        sendErr(e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case "rm":
                    if (cmd_fields.length == 2) {
                        String filename = cmd_fields[1];
                        try {
                            // get the file
                            File file = File.search(filename, currentPath.replaceFirst("/", ""), user);
                            Folder folder =  Folder.search(filename, currentPath.replaceFirst("/", ""), user);
                            if (file != null) {
                                FileManager.delete(file); // delete the file
                                // send acknowledgment
                                sendWithCode(RM_STAT, "File deleted!");
                            } else if(folder != null){
                                FileManager.delete(folder);
                                // send acknowledgment
                                sendWithCode(RM_STAT, "Folder deleted!");
                            } else
                                sendErr("Can't find "+filename);
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendErr(e.getMessage());
                        }
                    }
                    break;
                case "upload":
                    //System.out.println(cmd_fields.length);
                    if (cmd_fields.length == 4 || cmd_fields.length == 3) {
                        String filename;
                        if (cmd_fields[1].lastIndexOf("/") != -1)
                            filename = cmd_fields[1].substring(cmd_fields[1].lastIndexOf("/"));
                        else
                            filename = cmd_fields[1];
                        String accessRights = "public";
                        if(cmd_fields.length == 4)
                            accessRights = cmd_fields[2];
                        boolean fp = "private".equals(accessRights);
                        int fileSize;
                        if(cmd_fields.length == 4)
                            fileSize = Integer.parseInt(cmd_fields[3]);
                        else
                            fileSize = Integer.parseInt(cmd_fields[2]);
                        sendWithCode(UPLD_STAT,"Waiting for file");
                        try {
                            FileManager.upload(socket.getInputStream(), filename, fileSize, currentPath, user, fp);
                            sendMsg("File Uploaded");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else
                    break;
                case "download":
                    //System.out.println(cmd_fields.length);
                    if (cmd_fields.length == 2) {
                        String filename = cmd_fields[1];
                        try {
                            // get the file
                            File file = File.search(filename, currentPath.replaceFirst("/", ""), user);
                            if (file == null) throw new FileNotFoundException("Can't locate file in server!");
                            sendWithCode(DNLD_STAT, "File located, preparing to download...");
                            // send file size
                            System.out.println(file.getName()+" - "+file.length());
                            sendWithCode(FIL_SIZ, ""+file.length());
                            // send the file contnets to user
                            Thread.sleep(500);
                            FileManager.download(socket.getOutputStream(), file);
                            System.out.println("file loaded to stream, waiting acknowledgment");
                            String s = readCmd();
                            System.out.println(s);
                            if (s != null && 0 == Integer.parseInt(s))
                                sendMsg("File Downloaded");

                        } catch (Exception e) {
                            e.printStackTrace();
                            sendErr(e.getMessage());
                        }

                    }
                    break;
                case "edit":
                    if(cmd_fields.length == 2) {
                        String filname = cmd_fields[1];
                        try {
                            File file = File.search(filname, currentPath, user);
                            if (file == null) throw new FileNotFoundException("Can't locate file in server!");
                            if(file.isLocked()) {
                                throw new AccessDeniedException("File is locked (it may be opened by another connection)");
                            }
                            file.lock();
                            try {
                                sendWithCode(EDT_STAT, "File located, preparing to download...");
                                // send file size
                                sendWithCode(FIL_SIZ, ""+file.length());
                                // send file contents to user
                                Thread.sleep(500);
                                FileManager.download(socket.getOutputStream(), file);

                                DataInputStream oin = new DataInputStream(new BufferedInputStream(
                                        socket.getInputStream()));
                                int fsize = oin.readInt();
                                System.out.println(fsize);
                                FileManager.update(socket.getInputStream(), file, fsize);
                                sendMsg("File Updated");
                            } catch (FileNotFoundException e) {
                                sendErr(e.getMessage());
                            } catch (AccessDeniedException e){
                                sendErr(e.getMessage());
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                file.unlock();
                            }
                        } catch (Exception e) {
                            //sendErr(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    break;
                case "logout":
                    UsersManager.logout(user);
                    user = null;
                    currentPath = File.separator;
                    sendWithCode(LOGOUT_STAT, "Logged out");
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
                if (cmd_fields.length != 3) {
                    sendErr("Missing parameter !");
                    break;
                }
                try {
                    user = UsersManager.login(cmd_fields[1], cmd_fields[2]);
                    System.out.println("logged in user " + user.getFirstName());
                    cd(user.getHome());
                    sendWithCode(LOGIN_STAT, "Welcome " + user.getFirstName());
                } catch (IllegalArgumentException e) {
                    System.err.println(e);
                    sendErr(e.getMessage());
                }
                break;
            case "signup":
                if (cmd_fields.length != 6) {
                    sendErr("Missing parameter !");
                    break;
                }
                byte acc;
                if ("ALL".equals(cmd_fields[5].toUpperCase()))
                    acc = Client.A_ALL;
                else if ("RESTR".equals(cmd_fields[5].toUpperCase()))
                    acc = Client.A_RESTR;
                else {
                    sendErr("Access rights has wrong format.");
                    break;
                }
                try {
                    user = UsersManager.register(cmd_fields[1], cmd_fields[2], cmd_fields[3], cmd_fields[4], acc);
                    System.out.println("user '" + user.getFirstName() + "' has registered.");
                    cd(user.getHome());
                    System.out.println(currentPath);
                    sendWithCode(SIGNUP_STAT, "Thank you "+user.getFirstName()+"\\nYou have successfully signed up.");
                } catch (Exception e) {
                    sendErr(e.getMessage());
                    //System.err.println(e);
                    e.printStackTrace();
                }
                break;
            default:
                cmdNotFoundMsg();
        }
    }

    private void cmdNotFoundMsg() {
        sendErr("Command not found.\\nType 'help' to see all commands.\\nType 'help $commandName$' to command manual.");
    }

    public void terminateConnection() {
        try {
            if (user != null)
                UsersManager.logout(user);
            sendWithCode(EXIT_STAT, "Good bye");
            socket.close();
            System.out.println("Connection Terminated");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
