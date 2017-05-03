package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Folder
 * Created by haitham on 4/27/17.
 */
public class Folder extends java.io.File {
    static final String detailsPath = ".data";
    private ArrayList<File> files;
    private ArrayList<Folder> folders;
    private Client owner;
    private Date creationDate;

    public Folder(String s) throws IOException {
        super(s);
        if (this.exists() && !this.isDirectory())
            throw new IOException("Not directory");
        refresh();
    }

    Folder(String s, boolean newfile) throws IOException {
        this(s);
        if (newfile) {
            mkdir();
            files = getFilesArray();

        }

    }

    static Folder create(String s, Client owner) {
        try {
            Folder newFolder = new Folder(s, true);
            if (newFolder.files == null) newFolder.files = new ArrayList<>();
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

    void addFile(File file) {
        if (files == null) files = new ArrayList<>();
        files.add(file);
        updateDetailsFile();
    }
    void addFile(Folder file) {
        if (folders == null) folders = new ArrayList<>();
        folders.add(file);
        updateDetailsFile();
    }

    ArrayList<File> getFilesArray() {
        refresh();
        return files;
    }
    ArrayList<Folder> getFoldersArray() {
        refresh();
        return folders;
    }

    void updateContentsArray() {
        java.io.File detailsFile = new java.io.File(this.getAbsolutePath() + "/" + detailsPath);
        try {
            if (!detailsFile.exists()) {
                folders = new ArrayList<>();
                files = new ArrayList<>();
            } else {
                ObjectInputStream oin = new ObjectInputStream(new FileInputStream(detailsFile));
                Object o = oin.readObject();
                if (o != null) {
                    folders = (ArrayList<Folder>) o;
                }
                o = oin.readObject();
                if (o != null) {
                    files = (ArrayList<File>) o;
                }
            }
        } catch (EOFException e) {
            folders = new ArrayList<>();
            files = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    void refresh() {
        updateContentsArray();
        ArrayList<Folder> realFolders = new ArrayList<>();
        for (int i = 0; i < folders.size(); i++) {
            if (folders.get(i).exists()) realFolders.add(folders.get(i));
        }
        folders = realFolders;
        ArrayList<File> realFiles = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).exists()) realFiles.add(files.get(i));
        }
        files = realFiles;
        updateDetailsFile();
    }

    void updateDetailsFile() {
        java.io.File detailsFile = new java.io.File(this.getAbsolutePath() + "/" + detailsPath);
        if (!detailsFile.exists()) {
            try {
                detailsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(detailsFile));
            if(folders == null) folders = new ArrayList<>();
            if(files == null) files = new ArrayList<>();
            out.writeObject(folders);
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
