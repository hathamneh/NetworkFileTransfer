package com.company;

import java.io.*;
import java.text.DecimalFormat;

/**
 * Request
 * Created by haitham on 4/30/17.
 */
public class Request {

    String cmd_name;
    String[] args;

    private File file;
    private File tmpfile;
    private long fileSize;

    private Request(String cmd, String[] args) {
        tmpfile = new File(System.getProperty("user.dir")+File.separator+"tmp");
        tmpfile.mkdirs();
        this.cmd_name = cmd;
        this.args = args;
        if (isUpload()) {
            file = new File(args[0]);
            fileSize = file.length();
        }
        if (isDownload()) {
            if (args.length == 2) {
                file = new File(args[1] + File.separator + args[0]);
                args[1] = "";
            } else
                file = new File(System.getProperty("user.dir") + File.separator + args[0]);
        }
        if(isModify()) {
            file = new File(tmpfile.getAbsolutePath()+File.separator+args[0]);
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
        return "upload".equals(cmd_name) && (args.length == 2 || args.length == 1);
    }

    boolean isDownload() {
        return "download".equals(cmd_name);
    }

    boolean isModify() {
        return "edit".equals(cmd_name);
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
        socketOS.flush();

        // refresh file size
        fileSize = file.length();

        //System.out.println(file.length());
        FileInputStream bis = new FileInputStream(file);
        System.out.println("Uploading...");
        byte[] fileByts = new byte[1024];
        int sentBytes = 0, k;
        DecimalFormat numberFormat = new DecimalFormat("#.0");
        while (sentBytes < fileSize && (k = bis.read(fileByts)) > 0) {
            sentBytes += k;
            socketOS.write(fileByts, 0, k);
            socketOS.flush();
            System.out.print("\r " + numberFormat.format(sentBytes * 100.0 / fileSize) + "% (Uploaded " + hSize(sentBytes) + " of " + hSize(fileSize) + ")");
        }
        System.out.print("\r 100% (Uploaded " + hSize(sentBytes) + " of " + hSize(fileSize) + ")");
        System.out.println();
        System.out.println("Just a sec...");
    }

    // Receive File from Server (Download)
    public File receiveFile(InputStream socketIS, int fileSize) throws IOException, ClassNotFoundException {
        byte[] buff = new byte[fileSize];
        //BufferedInputStream bin = new BufferedInputStream(socketIS);

        System.out.println("Downloading " + hSize(fileSize) + ".... ");
        BufferedOutputStream bout = new BufferedOutputStream(
                new FileOutputStream(file));
        if (!file.exists())
            file.createNewFile();

        //System.out.println(file.getAbsoluteFile());
        DecimalFormat numberFormat = new DecimalFormat("#.0");
        try {
            //Thread.sleep(120);
            int rcvdBytes = 0, k, i = 0;
            while (rcvdBytes < fileSize) {
                k = socketIS.read(buff);
                rcvdBytes += k;
                bout.write(buff, 0, k);
                bout.flush();
                if (i % 3 == 0)
                    System.out.print("\r " + numberFormat.format(rcvdBytes * 100.0 / fileSize) + "% (Downloaded " + hSize(rcvdBytes) + " of " + hSize(fileSize) + ")");
                i++;
            }
            System.out.print("\r 100% (Downloaded " + hSize(rcvdBytes) + " of " + hSize(fileSize) + ")");
            System.out.println();
        } finally {
            bout.close();
            return file;
        }
    }

    public void updateFile(OutputStream stream) throws IOException {
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(stream));
        dout.writeInt((int) file.length());
        dout.flush();
        sendFile(stream);
        file.delete();
    }

    public static String hSize(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
