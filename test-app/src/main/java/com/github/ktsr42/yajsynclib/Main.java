package com.github.ktsr42.yajsynclib;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Formatter;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException
    {
        LibServer srv = new LibServer(null, "/",12345);
        Object[] params = srv.initServer();
        System.out.println(String.format("Local port %d, modulename %s", params[1], (String)params[0]));
        srv.run();
        srv.block();
        System.exit(0);
    }
    
}
