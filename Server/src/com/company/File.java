package com.company;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * File Details
 * Created by haitham on 20/4/2017
 */
public class File extends java.io.File {
    private Client owner;
    private Client lastUser;
    private Date uploadDate;
    private boolean filePrivate;
    private int downloadCount;

    public File(String name, String path, Client owner, boolean filePrivate, Date uploadDate) {
        super(FileManager.root_path + path + name);
        this.uploadDate = uploadDate;
        this.owner = owner;
        this.lastUser = owner;
        this.filePrivate = filePrivate;
        downloadCount = 0;
    }

    static String filterPath(String path) {
        return path.substring(path.length() - 1).equals("/") ? path : path + "/";
    }

    public String getOwner() {
        return owner.getUsername();
    }

    public String getLastUser() {
        return lastUser.getUsername();
    }

    public void editedBy(Client c) {
        lastUser = c;
        try {
            Folder f = new Folder(getParent());
            f.updateFile(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void incDowns() {
        downloadCount++;
        try {
            Folder f = new Folder(getParent());
            System.out.println(f.updateFile(this));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public Date modifyDate() {
        return new Date(lastModified());
    }

    public String getFileAccess() {
        return filePrivate ? "private" : "public";
    }

    static File search(String name, String path, Client owner) throws IOException {
        path = FileManager.root_path + path;
        //System.out.println(path + name);
        Folder folder = new Folder(path);
        folder.refresh();
        ArrayList<File> files = folder.getFilesArray();
        for (File file :
                files) {
            //System.out.println(file.getName());
            if (file.getName().equals(name)) {
                if (owner.getAccessRights() == Client.A_ALL && !file.filePrivate)
                    return file;
                if (file.getOwner().equals(owner.getUsername()))
                    return file;
                throw new IOException("You don't have access to this file");
            }
        }

        return null;
    }

    static File adminSearch(String fullPath) throws IOException {
        java.io.File tmp = new java.io.File(FileManager.root_path+fullPath);
        Folder parent = new Folder(tmp.getParent());
        parent.refresh();
        ArrayList<File> files = parent.getFilesArray();
        for (File file :
                files) {
            //System.out.println(file.getName());
            if (file.getName().equals(tmp.getName())) {
                    return file;
            }
        }
        return null;
    }

    String getDetails() {
        String format = "%21s %s";
        String out = String.format(format,"File name:",getName())
                + "\n" + String.format(format,"Full path:",getPath())
                + "\n" + String.format(format,"Access:",getFileAccess())
                + "\n" + String.format(format,"Upload date:",getUploadDate())
                + "\n" + String.format(format,"Owner uname:",getOwner())
                + "\n" + String.format(format,"Last Modified:",new Date(lastModified()))
                + "\n" + String.format(format,"Modified by:",getLastUser())
                + "\n" + String.format(format,"Number of downloads:",getDownloadCount());
        return out;
    }

    void lock() {
        try {
            java.io.File lockFile = new java.io.File(getParent()+File.separator+"."+getName()+".lock");
            lockFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void unlock() {
        if(isLocked()) {
            java.io.File lockFile = new java.io.File(getParent() + File.separator + "." + getName() + ".lock");
            lockFile.delete();
        }
    }
    boolean isLocked() {
            java.io.File lockFile = new java.io.File(getParent()+File.separator+"."+getName()+".lock");
            return lockFile.exists();
    }
}
