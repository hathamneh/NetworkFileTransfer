package com.company;

/**
 * ServerAdmin
 * Created by haitham on 4/23/17.
 */
public class ServerAdmin {
    static void execute(String cmd) {
        String[] cmd_fields = cmd.split(" ");
        if(cmd_fields.length != 1)
            cmd = cmd_fields[0]+" "+cmd_fields[1];

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
                if(cmd_fields.length == 3) {
                    Client user = UsersManager.checkUser(cmd_fields[2]);
                    if (user != null)
                        System.out.println(user.getData());
                    else
                        System.out.println("Cant find this user");
                } else
                    System.err.println("Missing username");
                break;
            case "user delete":
                if(cmd_fields.length == 3) {
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
        }
    }
}
