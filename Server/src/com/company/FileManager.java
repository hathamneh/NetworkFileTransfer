package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

/**
 * FileManager
 * Created by haitham on 4/20/17.
 */
public class FileManager {
    static String root_path = "uploads" + File.separator;

    static void mkdir(String name, String currentPath, Client currentUser) {
        Folder folder;
        try {
            folder = Folder.create(root_path + currentPath + name, currentUser);
            Folder currentFolder = new Folder(root_path + currentPath);
            currentFolder.addFile(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static String[] listFiles(String folder) throws Exception {
        Folder myFolder;
        String[] out;
        myFolder = new Folder(root_path + folder);
        if (myFolder == null) throw new FileNotFoundException("can't find directory!");
        int i = 0;
        ArrayList<Folder> dirs = myFolder.getFoldersArray();
        ArrayList<File> fls = myFolder.getFilesArray();
        out = new String[dirs.size() + fls.size()];
        for (Folder tmpDir : dirs) {
            out[i] = tmpDir.getName();
            i++;
        }
        for (File tmpFile : fls) {
            out[i] = tmpFile.getName();
            i++;
        }
        return out;
    }

    static String[] listFilesMore(String folder) {
        Folder myFolder = null;
        String[] out;
        try {
            System.out.println(root_path + folder);
            myFolder = new Folder(root_path + folder);
            ArrayList<Folder> dirs = myFolder.getFoldersArray();
            ArrayList<File> fls = myFolder.getFilesArray();
            out = new String[dirs.size() + fls.size()+1];
            String format = "%-20s %-10s  %-10s  %-10s  %s";
            out[0] = String.format(format,"File name", "Size","Access", "Owner", "Last modified")
                    + "\\n" + new String(new char[85]).replace("\0", "-");
            int i = 1;
            for (Folder tmpDir : dirs) {
                out[i] = String.format(format,tmpDir.getName(), "", "", tmpDir.getOwner(), tmpDir.modifyDate().toString());
                i++;
            }
            for (File tmpFile : fls) {
                out[i] = String.format(format,tmpFile.getName(), hSize(tmpFile.length()), tmpFile.getFileAccess(), tmpFile.getOwner(), tmpFile.modifyDate().toString());
                i++;
            }
            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    synchronized static void upload(InputStream stream, String name, int fileSize, String currentPath, Client owner, boolean fprvte) {
        try {
            BufferedInputStream is = new BufferedInputStream(stream);
            File file = new File(name, currentPath, owner, fprvte, new Date());
            if (!file.exists()) {
                file.createNewFile();
                Folder parent = new Folder(file.getParent());
                parent.addFile(file);
                System.out.println("File Created!");
            } else {
                System.out.println("File is already exist");
            }
            BufferedOutputStream bout = new BufferedOutputStream(
                    new FileOutputStream(file));
            copyStream(is, bout, fileSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized static void download(OutputStream stream, File file) throws Exception {
        int fileSize = (int) file.length();
        BufferedOutputStream os = new BufferedOutputStream(stream);
        byte[] buff = new byte[fileSize];
        FileInputStream bin = new FileInputStream(file);
        if (!file.exists()) {
            throw new FileNotFoundException("Can't find the file");
        }
        try {
            int sentBytes = 0, k;
            while (sentBytes < fileSize && (k = bin.read(buff)) > 0) {
                sentBytes += k;
                os.write(buff, 0, k);
                os.flush();
            }
            //System.out.println(sentBytes);
        } finally {

            os.flush();
            bin.close();
        }
    }

    synchronized static void delete(java.io.File file) {
        try {
            Folder parent = new Folder(file.getParent());
            file.delete();
            parent.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    synchronized static void update(InputStream stream, File file, int fileSize) throws Exception {
        try {
            //BufferedInputStream is = new BufferedInputStream(stream);
            if (!file.exists()) {
                throw new FileNotFoundException("File can't be found on the server!");
            }
            file.setLastModified((new Date()).getTime());
            BufferedOutputStream bout = new BufferedOutputStream(
                    new FileOutputStream(file));
            //System.out.println(fileSize);
            copyStream(stream, bout, fileSize);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized static void copyStream(InputStream source, OutputStream destination, int size) throws IOException {
        int count = 0, k;
        byte[] buff = new byte[1024];
        try {
            while (count < size) {
                k = source.read(buff);
                destination.write(buff, 0, k);
                count += k;
                destination.flush();
            }
        } finally {
            destination.close();
        }
    }

    public static String hSize(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
