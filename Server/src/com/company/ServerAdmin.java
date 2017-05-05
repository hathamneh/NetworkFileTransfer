package com.company;

import java.io.IOException;

/**
 * ServerAdmin
 * Created by haitham on 4/23/17.
 */
public class ServerAdmin {
    static void execute(String cmd) {
        String[] cmd_fields = cmd.split(" ");
        String[] args = null;
        if(cmd_fields.length != 1)
            cmd = cmd_fields[0]+" "+cmd_fields[1];
        if(cmd_fields.length>2) {
            args = new String[cmd_fields.length - 2];
            for (int i = 2; i < cmd_fields.length; i++) {
                args[i-2] = cmd_fields[i];
            }
        }
        switch (cmd.trim()) {
            case "users all":
                for (Client cl :
                        UsersManager.getRegisteredUsers()) {
                    System.out.println(cl);
                }
                break;
            case "users active":
                for (Client cl:
                     UsersManager.getLoggedInUsers()) {
                    System.out.println(cl);
                }
                break;
            case "user data":
                if(args != null && args.length == 1) {
                    Client user = UsersManager.checkUser(args[0]);
                    if (user != null)
                        System.out.println(user.getData());
                    else
                        System.out.println("Cant find this user");
                } else
                    System.err.println("Missing username");
                break;
            case "user delete":
                if(args != null && args.length == 1) {
                    Client user = UsersManager.checkUser(cmd_fields[2]);
                    if (user != null)
                        try {
                            UsersManager.delete(user);
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    else
                        System.out.println("Cant find this user");
                } else
                    System.err.println("Missing username");
                break;
            case "file info":
                if(args != null && args.length == 1) {
                    try {
                        File file = File.adminSearch(args[0]);
                        if(file != null) {
                            System.out.println(file.getDetails());
                        } else
                            System.err.println("File not found!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    System.err.println("Missing username");
                break;
        }
    }
}
