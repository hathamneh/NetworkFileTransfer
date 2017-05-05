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
    // Server Statuses
    static final int EXIT_STAT = 1;
    static final int CD_STAT = 2;
    static final int SIGNUP_STAT = 11;
    static final int LOGIN_STAT = 10;
    static final int LOGOUT_STAT = 9;
    static final int PASSWD_STAT = 8;
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

    private Response(String[] res) {
        status = Integer.parseInt(res[0]);
        msg = unescapeJava(res[1]);
        currentPath = res[2];
        isErr = status == -1;
    }

    /**
     * Create Response object from raw response text
     * The only way to create response
     * @param res
     * @return
     */
    static Response parseResponse(String res) {
        String[] resArr = res.split(";");
        if (resArr.length != 3)
            throw new IllegalArgumentException("Raw Response must 3 fields");
        return new Response(resArr);
    }

    /**
     * Does the response is an error
     * @return boolean
     */
    public boolean isErr() {
        return isErr;
    }


    @Override
    public String toString() {
        return status + ";" + msg + ";" + currentPath;
    }

    /**
     * Print the message returned from server
     */
    void printMsg() {
        System.out.print("\r");
        if(hasMsg()) {
            if (isErr)
                System.err.println(msg);
            else if (status == PASSWD_STAT) {
                System.out.print(msg);
            } else
                System.out.println(msg);
            System.err.flush();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.flush();
        }
    }

    boolean hasMsg() {
        return !(null == msg || "" == msg);
    }

}
