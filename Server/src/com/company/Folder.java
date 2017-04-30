package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Folder
 * Created by haitham on 4/27/17.
 */
public class Folder extends java.io.File {
    private static final String detailsPath = ".data";
    private ArrayList<java.io.File> files;
    private Client owner;
    private Date creationDate;

    public Folder(String s) throws IOException {
        super(s);
        if (this.exists() && !this.isDirectory())
            throw new IOException("Not directory");
        updateFilesArray();
    }

    Folder(String s, boolean newfile) throws IOException {
        this(s);
        if(newfile) {
            mkdir();
            files = getFilesArray();

        }

    }

    static Folder create(String s, Client owner) {
        try {
            Folder newFolder = new Folder(s,true);
            if(newFolder.files == null) newFolder.files = new ArrayList<>();
            newFolder.owner = owner;
            newFolder.creationDate = new Date();
            return newFolder;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    int numFiles() {
        return (files != null) ? files.size() : 0;
    }

    void addFile(java.io.File file) {
        if(files == null) files = new ArrayList<>();
        files.add(file);
        updateDetailsFile();
    }

    ArrayList<java.io.File> getFilesArray() {
        if(files == null)
            updateFilesArray();
        return files;
    }

    void updateFilesArray() {
        java.io.File detailsFile = new java.io.File(this.getAbsolutePath() + "/" + detailsPath);
        System.out.println(detailsFile.getAbsoluteFile());
        try {
            if (!detailsFile.exists()) {
                detailsFile.createNewFile();
                files = new ArrayList<>();
                updateDetailsFile();
            } else {
                ObjectInputStream oin = new ObjectInputStream(new FileInputStream(detailsFile));
                Object o = oin.readObject();
                if (o != null)
                    files = (ArrayList<java.io.File>) o;
            }
        } catch (EOFException e) {
            files = new ArrayList<>();
            updateDetailsFile();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void updateDetailsFile() {
        java.io.File detailsFile = new java.io.File(this.getAbsolutePath() + "/" + detailsPath);
        if (!detailsFile.exists()) {
            try {
                System.out.println(detailsFile.getAbsoluteFile());
                detailsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(detailsFile));
            out.writeObject(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getOwner() {
        return owner.toString();
    }

    public void setOwner(Client owner) {
        if (this.owner.getAccessRights() == Client.A_ALL)
            this.owner = new Client(owner);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date modifyDate() {
        return new Date(lastModified());
    }

}
