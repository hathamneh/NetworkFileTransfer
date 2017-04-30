package com.company;

import java.io.*;
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
            Folder currentFolder = new Folder(root_path+currentPath);
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
            ArrayList<java.io.File> fls = myFolder.getFilesArray();
            out = new String[fls.size()];
            for (java.io.File tmpFile :
                    fls) {
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
            ArrayList<java.io.File> fls = myFolder.getFilesArray();
            out = new String[fls.size()];
            int i = 0;
            for (java.io.File tmpFile :
                    fls) {
                if(tmpFile instanceof File)
                    out[i] = ((File)tmpFile).getFileAccess() +"\t" + ((File)tmpFile).getOwner() + "\t" + ((File)tmpFile).modifyDate().toString() +" " + tmpFile.getName();
                else if (tmpFile instanceof Folder)
                    out[i] = "\\t-\t" + ((Folder)tmpFile).getOwner() + "\t" + ((Folder)tmpFile).modifyDate().toString() +" " + tmpFile.getName();
                i++;
            }
            return out;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void upload(byte[] file, String name,String currentPath,boolean filePrivate) {
        try {
            File f = new File(name, currentPath, filePrivate,new Date());
            if(!f.exists()) {
                f.createNewFile();
                System.out.println("File Created!");
            } else {
                System.out.println("File is already exist");
            }
            BufferedOutputStream bout = new BufferedOutputStream(
                    new FileOutputStream(f));
            bout.write(file);
            bout.flush();
            Folder dir = new Folder(root_path + currentPath);
            dir.addFile(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
