package com.github.ktsr42.yajsynclib;

import com.github.perlundq.yajsync.server.module.Module;
import com.github.perlundq.yajsync.server.module.RestrictedPath;

import java.nio.file.FileSystems;

public class SimpleModule implements Module {
    private String name;
    private RestrictedPath path;

    public SimpleModule(String name, String path) {
        this.name = name;
        this.path = new RestrictedPath(name, FileSystems.getDefault().getPath(path));
    }

    @Override
    public String name() { return name; }

    @Override
    public String comment() { return ""; }

    @Override
    public RestrictedPath restrictedPath() { return path; }

    @Override
    public boolean isReadable() { return true; }

    @Override
    public boolean isWritable() { return true; }
}
