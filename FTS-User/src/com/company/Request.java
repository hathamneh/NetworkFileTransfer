package com.company;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Request
 * Created by haitham on 4/30/17.
 */
public class Request {

    String cmd_name;
    String[] args;

    private File file;
    private long fileSize;

    private Request(String cmd, String[] args) {
        this.cmd_name = cmd;
        this.args = args;
        if (isUpload()) {
            file = new File(args[0]);
            fileSize = file.length();
        }
        if(isDownload()) {
            if(args.length == 2) {
                file = new File(args[1]+ File.separator + args[0]);
                args[1] = "";
            }
            else
                file = new File(System.getProperty("user.dir")+ File.separator + args[0]);
        }

    }

    static Request parseRequest(String command) {
        if (command != null && !("".equals(command))) {
            String[] cmd_data = command.split(" ");
            String[] args = null;
            if (cmd_data.length > 1) {
                args = new String[cmd_data.length - 1];
                for (int i = 1; i < cmd_data.length; i++)
                    args[i - 1] = cmd_data[i];
            }

            return new Request(cmd_data[0], args);
        } else {
            return new Request(null, null);
        }
    }

    boolean isUpload() {
        return "upload".equals(cmd_name) && args.length == 1;
    }

    boolean isDownload() {
        return "download".equals(cmd_name);
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileAbsolutePath() {
        return file.getAbsolutePath();
    }

    boolean hasArgs() {
        return args != null && args.length > 0;
    }

    public String getCmd() {
        String out = cmd_name + " ";
        if (hasArgs())
            for (String arg : args)
                out += arg + " ";
        if (isUpload()) out += fileSize;
        return out;
    }

    public boolean isCommand() {
        return null != cmd_name;
    }

    public boolean fileExists() {
        return file.exists();
    }

    public void sendFile(OutputStream socketOS) throws IOException {
        FileInputStream bis = new FileInputStream(file);
        System.out.println("Uploading...");
        byte[] fileByts = new byte[(int) fileSize];
        while (bis.read(fileByts, 0, fileByts.length) > 0) {}
        socketOS.write(fileByts);
        socketOS.flush();
    }

    public void recieveFile(InputStream socketIS) throws IOException {
        byte[] buff = new byte[1024*1024];
        int k,count=0;
        BufferedInputStream bin = new BufferedInputStream(socketIS);
        DataInputStream din = new DataInputStream(bin);
        fileSize = din.readInt();
        System.out.println(fileSize);
        if(fileSize == -1){
            System.err.println("File not found in the server!");
            return;
        }
        System.out.println("Downloading....");
        BufferedOutputStream bout = new BufferedOutputStream(
                new FileOutputStream(file));
        if(!file.exists())
            file.createNewFile();
        System.out.println(file.getAbsoluteFile());
        while(count < fileSize) {
            k = bin.read(buff);
            bout.write(buff, 0, k);
            count += k;
        }
        bout.flush();
        bout.close();
    }
}
