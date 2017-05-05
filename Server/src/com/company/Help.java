package com.company;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Help
 * Created by haitham on 5/5/17.
 */
public class Help {
    static HashMap<String, String> syntax = new LinkedHashMap<>();
    static HashMap<String, String> messages = new LinkedHashMap<>();

    static {
        messages.put("signup", "Register new user.");
        syntax.put("signup", "signup firstName lastName userName password all|restr");

        messages.put("login", "Login user.");
        syntax.put("login", "login userName password");

        messages.put("logout", "Logout the user, you have to be logged in.");
        syntax.put("logout", "logout (should have no argument)");

        messages.put("passwd", "Change user password, you have to be logged in.");
        syntax.put("passwd", "passwd (should have no argument)");

        messages.put("pwd", "Display current directory, you have to be logged in.");
        syntax.put("pwd", "pwd (should have no argument)");

        messages.put("ls", "List contents of current directory, you have to be logged in.\\nThe -l arguments display files with details.");
        syntax.put("ls", "ls [-l]");

        messages.put("cd", "Change directory to given folder name, or go to previous folder using ..\\nYou can only change directory one level, you have to be logged in.");
        syntax.put("cd", "cd folder|..");

        messages.put("mkdir", "Create new folder with given name, you have to be logged in.");
        syntax.put("mkdir", "mkdir name");

        messages.put("rm", "Delete file, or folder recursively, you have to be logged in.");
        syntax.put("rm", "rm folder|filename");

        messages.put("download", "Download file from server to local machine, you have to be logged in.\\nIf destination isn't specified the file will be downloaded to the same folder where the app is running.");
        syntax.put("download", "download filename [destination]");

        messages.put("upload", "Upload file to the server, you have to be logged in.\\nThe default access rights to files is public unless you add private argument.");
        syntax.put("upload", "upload filename [private]");

        messages.put("edit", "Modify contents of a file, you have to be logged in.");
        syntax.put("edit", "edit filename");

        messages.put("help", "List all commands available.\\nGet help for specific command.");
        syntax.put("help", "help [command]");
    }

    static String getSyntax(String command) {
        return syntax.get(command);
    }

    static String get(String command) {
        for (Map.Entry<String, String> s : messages.entrySet())
            if (s.getKey().equals(command))
                return s.getValue() + "\\n" + getSyntax(command);

        return "Command not found!";
    }

    static String getAll() {
        StringBuffer out = new StringBuffer("");

        for (Map.Entry<String, String> c : syntax.entrySet()) {
            out.append(messages.get(c.getKey()) + "\\n");
            out.append(c.getValue() + "\\n--------\\n");
        }
        return out.toString();
    }
}
