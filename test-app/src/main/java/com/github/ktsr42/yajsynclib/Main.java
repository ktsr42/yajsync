package com.github.ktsr42.yajsynclib;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Formatter;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException
    {
        String[] moduleNames = { "root" };
        String[] modulePaths = { "/" };
        LibServerMulti srv = new LibServerMulti(12345, moduleNames, modulePaths);
        int port = srv.initServer();
        System.out.println(String.format("Local port %d, modulename %s", port, moduleNames[0]));
        srv.run();
        srv.block();
        System.exit(0);
    }
    
}
