package com.company;

import java.io.File;
import java.util.Date;

/**
 * MyFile Details
 * Created by haitham on 20/4/2017
 */
public class MyFile {
    private String name;
    private String path;
    private Client owner;
    private boolean filePrivate;
    private int size;
    private Date modifyDate, uploadDate;
    private File file;

    public MyFile(String name, String path, boolean filePrivate) {
        this.name = name;
        this.path = filterPath(path);
        file = new File(getFullPath());
        this.filePrivate = filePrivate;
        uploadDate = new Date();
        modifyDate = new Date();

    }

    MyFile() {}

    public String getFullPath(){
        return FileManager.root_path + path + name;
    }

    static String filterPath(String path) {
        return path.substring(path.length()-1).equals("/") ? path : path + "/";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOwner() {
        return owner.toString();
    }

    public void setOwner(Client owner) {
        if(this.owner.getAccessRights() == Client.A_ALL)
        this.owner = new Client(owner);
    }

    public boolean isFilePrivate() {
        return filePrivate;
    }

    public void setFilePrivate(boolean filePrivate) {
        this.filePrivate = filePrivate;
    }

    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public boolean delete() {
        return file.delete();
    }

}
