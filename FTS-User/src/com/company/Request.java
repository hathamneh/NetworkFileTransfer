package com.company;

import java.io.*;
import java.text.DecimalFormat;

/**
 * Request
 * process the user command and fix its arguments, upload and download files from server
 */
public class Request {

    String cmd_name;
    String[] args;

    private File file;
    private File tmpfile;
    private long fileSize;

    private Request(String cmd, String[] args) {
        // create temporary folder for editing files
        tmpfile = new File(System.getProperty("user.dir")+File.separator+"tmp");
        tmpfile.mkdirs();

        this.cmd_name = cmd;
        this.args = args;
        // if command is upload create File object to upload it
        if (isUpload()) {
            file = new File(args[0]);
            fileSize = file.length();
        }
        // if command is download create File object to download to it
        if (isDownload()) {
            if (args.length == 2) { // download to given path
                file = new File(args[1] + File.separator + args[0]);
                args[1] = "";
            } else // download to current directory
                file = new File(System.getProperty("user.dir") + File.separator + args[0]);
        }
        // if modify create temp file to make changes on it
        if(isModify()) {
            file = new File(tmpfile.getAbsolutePath()+File.separator+args[0]);
        }
    }

    /**
     * Parse the user command and create Request object
     * @param command
     * @return New Request object from given command
     */
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

    /**
     * whether command is upload or not
     * @return boolean
     */
    boolean isUpload() {
        return "upload".equals(cmd_name) && args != null;
    }

    /**
     * whether command is download or not
     * @return boolean
     */
    boolean isDownload() {
        return "download".equals(cmd_name) && args != null;
    }

    /**
     * whether command is edit or not
     * @return boolean
     */
    boolean isModify() {
        return "edit".equals(cmd_name);
    }

    /**
     * whether command has arguments or not
     * @return boolean
     */
    boolean hasArgs() {
        return args != null && args.length > 0;
    }

    /**
     * get the full command to be sent to the server
     * @return String command
     */
    public String getCmd() {
        String out = cmd_name + " ";
        if (hasArgs())
            for (String arg : args)
                out += arg + " ";
        if (isUpload()) out += fileSize; // add the file size to the command if it is upload
        return out;
    }

    /**
     * Does this object has a real command
     * @return boolean
     */
    public boolean isCommand() {
        return null != cmd_name;
    }

    /**
     * whether the file to make an operation on it exists or not
     * @return boolean
     */
    public boolean fileExists() {
        return file.exists();
    }

    /**
     * Send the file contents to the server
     * @param socketOS The output stream of the socket
     * @throws IOException If an error occurred while sending the file contents
     */
    public void sendFile(OutputStream socketOS) throws IOException {

        // refresh file size if it is modified
        fileSize = file.length();

        // Create stream to read from file
        FileInputStream bis = new FileInputStream(file);
        byte[] fileByts = new byte[1024];
        int sentBytes = 0, k;

        // start uploading
        System.out.println("Uploading "+hSize(fileSize)+"...");
        DecimalFormat numberFormat = new DecimalFormat("#.0"); // this line for format the percentage
        while (sentBytes < fileSize && (k = bis.read(fileByts)) > 0) {
            sentBytes += k;
            socketOS.write(fileByts, 0, k);
            socketOS.flush();
            // show percentage
            System.out.print("\r " + numberFormat.format(sentBytes * 100.0 / fileSize) + "% (Uploaded " + hSize(sentBytes) + " of " + hSize(fileSize) + ")");
        }
        // finished uploading
        System.out.print("\r 100% (Uploaded " + hSize(sentBytes) + " of " + hSize(fileSize) + ")");
        System.out.println();
        // wait the server to finish processing the file
        System.out.println("Just a sec...");
    }

    /**
     * Receive file contents from server
     * @param socketIS The input stream of the socket
     * @param fileSize Number of bytes to be received
     * @return File the object of received file
     * @throws IOException If an error occurred while receiving
     */
    public File receiveFile(InputStream socketIS, int fileSize) throws IOException {
        // create stream to write the file
        BufferedOutputStream bout = new BufferedOutputStream(
                new FileOutputStream(file));
        byte[] buff = new byte[fileSize];
        int rcvdBytes = 0, k;

        // Assure the file exists
        if (!file.exists())
            file.createNewFile();

        // start downloading
        System.out.println("Downloading " + hSize(fileSize) + ".... ");
        DecimalFormat numberFormat = new DecimalFormat("#.0"); // for percentage
        try {
            while (rcvdBytes < fileSize) {
                k = socketIS.read(buff);
                rcvdBytes += k;
                bout.write(buff, 0, k);
                bout.flush();
                System.out.print("\r " + numberFormat.format(rcvdBytes * 100.0 / fileSize) + "% (Downloaded " + hSize(rcvdBytes) + " of " + hSize(fileSize) + ")");
            }
            // finish downloading
            System.out.print("\r 100% (Downloaded " + hSize(rcvdBytes) + " of " + hSize(fileSize) + ")");
            System.out.println();
        } finally {
            bout.close();
            return file;
        }
    }

    /**
     * send modified version of file to the server
     * @param socketOS The output stream of socket
     * @throws IOException
     */
    public void updateFile(OutputStream socketOS) throws IOException {
        // send the new file size of modified file
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(socketOS));
        dout.writeInt((int) file.length());
        dout.flush();
        // send the file
        try {
            sendFile(socketOS);
        } finally {
            // delete tmp file after updating
            file.delete();
        }
    }

    /**
     * Get Human readable format of file size
     * @param bytes size to be converted
     * @return String
     */
    public static String hSize(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
