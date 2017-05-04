package com.company;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

/**
 * Response
 * Created by haitham on 4/23/17.
 */
public class Response {
    static final int EXIT_STAT = 1;
    static final int CD_STAT = 2;
    static final int SIGNUP_STAT = 11;
    static final int LOGIN_STAT = 10;
    static final int LOGOUT_STAT = 9;
    static final int ERR_STAT = -1;
    static final int UPLD_STAT = 15;
    static final int DNLD_STAT = 16;
    static final int RM_STAT = 17;
    static final int EDT_STAT = 18;
    static final int FIL_SIZ = 19;


    int status;
    String msg;
    String currentPath;
    private boolean isErr = false;

    public boolean isErr() {
        return isErr;
    }

    Response(String[] res) {
        if (res.length != 3)
            throw new IllegalArgumentException("response array must have length of 3");
        status = Integer.parseInt(res[0]);
        msg = unescapeJava(res[1]);
        currentPath = res[2];
        isErr = status == -1;
    }

    static Response parseResponse(String res) {
        return new Response(res.split(";"));
    }

    @Override
    public String toString() {
        return status + ";" + msg + ";" + currentPath;
    }

    void printMsg() {
        System.out.print("\r");
        if(hasMsg()) {
            if (isErr)
                System.err.println(msg);
            else
                System.out.println(msg);
            System.err.flush();
            System.out.flush();
        }
    }

    boolean hasMsg() {
        return !(null == msg || "" == msg);
    }


    public static Object receiveObj(InputStream inputStream) {
        try {
            ObjectInputStream oin = new ObjectInputStream(inputStream);
            return oin.readObject();
        } catch (IOException e) {
            System.err.println("Can't retrieve file info");
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
