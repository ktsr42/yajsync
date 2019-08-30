package com.github.ktsr42.yajsynclib;

import java.io.IOException;

public class Main {
    public static void main(String args[]) throws IOException,
                                                  InterruptedException
    {
        int rc = new LibServer().start();
        System.exit(rc);
    }
    
}
