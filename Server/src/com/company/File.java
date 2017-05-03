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
    private Date uploadDate;
    private boolean filePrivate;

    public File(String name, String path, boolean filePrivate, Date uploadDate) {
        super(FileManager.root_path + path + name);
        this.uploadDate = uploadDate;
        this.filePrivate = filePrivate;
    }

    static String filterPath(String path) {
        return path.substring(path.length() - 1).equals("/") ? path : path + "/";
    }

    public String getOwner() {
        return owner.toString();
    }

    public void setOwner(Client owner) {
        if (this.owner.getAccessRights() == Client.A_ALL)
            this.owner = new Client(owner);
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

    static File getFile(String name, String path, Client owner) throws IOException {
        path = FileManager.root_path + path;
        System.out.println(path + name);
        Folder folder = new Folder(path);
        folder.refresh();
        ArrayList<File> files = folder.getFilesArray();
        for (File file :
                files) {
            System.out.println(file.getName());
            if (file.getName().equals(name)) {
                if (file.getFileAccess().equals("public"))
                    return file;
                if (file.getOwner().equals(owner.toString()))
                    return file;
                throw new IOException("You have no access yo this file");
            }
        }
        throw new FileNotFoundException("Can't find the file");
    }
}
