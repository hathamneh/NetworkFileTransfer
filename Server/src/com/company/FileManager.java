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
    static String root_path = "uploads/";

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

    static String[] listFiles(String folder) {
        Folder myFolder;
        String[] out;
        try {
            myFolder = new Folder(root_path + folder);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String[] listFilesMore(String folder) {
        Folder myFolder = null;
        String[] out;
        try {
            myFolder = new Folder(root_path + folder);
            ArrayList<Folder> dirs = myFolder.getFoldersArray();
            ArrayList<File> fls = myFolder.getFilesArray();
            out = new String[dirs.size() + fls.size()];
            int i = 0;
            for (File tmpFile : fls) {
                out[i] = (tmpFile).getFileAccess() + "\t" + tmpFile.getOwner() + "\t" + tmpFile.modifyDate().toString() + " " + tmpFile.getName();
                i++;
            }
            for (Folder tmpDir: dirs) {
                out[i] = "\\t-\t" + tmpDir.getOwner() + "\t" + tmpDir.modifyDate().toString() + " " + tmpDir.getName();
                i++;
            }
            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void upload(InputStream stream, String name, int fileSize, String currentPath, boolean filePrivate) {
        try {
            BufferedInputStream is = new BufferedInputStream(stream);

            byte[] buff = new byte[1024];
            int k, count = 0;
            File file = new File(name, currentPath, filePrivate, new Date());
            if (!file.exists()) {
                file.createNewFile();
                Folder parent =  new Folder(file.getParent());
                parent.addFile(file);
                System.out.println("File Created!");
            } else {
                System.out.println("File is already exist");
            }
            BufferedOutputStream bout = new BufferedOutputStream(
                    new FileOutputStream(file));
            try {
                while (count < fileSize) {
                    k = is.read(buff);
                    bout.write(buff,0,k);
                    count += k;
                    bout.flush();
                }
            } finally {
                bout.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void download(OutputStream stream, File file) throws Exception {
        int fileSize = (int)file.length();
        BufferedOutputStream os = new BufferedOutputStream(stream);
        byte[] buff = new byte[fileSize];
        BufferedInputStream bin = new BufferedInputStream(
                new FileInputStream(file));
        if (!file.exists()) {
            throw new FileNotFoundException("Can't find the file");
        }
        try {
            int sentBytes=0,k;
            while ((k = bin.read(buff))>0) {
                sentBytes += k;
                os.write(buff, 0, k);
                os.flush();
                if(sentBytes == fileSize) break;
            }
        } finally {
            bin.close();
        }
    }

    static void delete(java.io.File file) {
        try {
            Folder parent = new Folder(file.getParent());
            file.delete();
            parent.refresh();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
