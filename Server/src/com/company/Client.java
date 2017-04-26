package com.company;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * User details
 * Created by haitham on 20/4/2017
 */
public class Client implements Serializable {
    static int countUsers = 0;
    static final byte A_ALL = 0;
    static final byte A_RESTR = 1;
    private int id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Date registrationDate;
    private byte accessRights;
    private File myFolder;

    Client() {}

    public Client(String firstName, String lastName, String username, String password, byte accessRights, Date registrationDate) {
        this.id = countUsers++;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.accessRights = accessRights;
        this.myFolder = new File(FileManager.root_path + username + "/");
        this.registrationDate = registrationDate;
    }

    public Client(Client client) {
        this.id = client.id;
        this.firstName = client.firstName;
        this.lastName = client.lastName;
        this.username = client.username;
        this.password = client.password;
        this.accessRights = client.accessRights;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte getAccessRights() {
        return accessRights;
    }

    public void setAccessRights(byte accessRights) {
        this.accessRights = accessRights;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    public boolean createUserDir() {
        return myFolder.mkdirs();
    }

    public String getHome() {
        return  username;
    }

    public String getData() {
        String data = "Full name: "+firstName + " " + lastName + "\n" +
                "username: " + username + "\n" +
                "password: " + password + "\n" +
                "home folder: " + getHome() + "\n" +
                "Access Rights: " + getReadableAccessRights();
        return data;
    }

    protected String getReadableAccessRights() {
        return accessRights == A_ALL ? "All" : "Restricted";
    }

    public void delete() throws IOException {
        if(!myFolder.delete())
            throw new IOException("Can't delete user folder");
    }
}
