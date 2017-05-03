package com.company;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

/**
 * Response
 * Created by haitham on 4/23/17.
 */
public class Response {
    int status;
    String msg;
    String currentPath;
    private boolean isErr = false;

    public boolean isErr() {
        return isErr;
    }

    Response(String[] res) {
        if (res.length != 3)
            throw new IllegalArgumentException("res array must have length of 3");
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


}
