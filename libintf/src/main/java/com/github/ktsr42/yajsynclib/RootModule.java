/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ktsr42.yajsynclib;

import com.github.perlundq.yajsync.server.module.Module;
import com.github.perlundq.yajsync.server.module.RestrictedPath;
import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.UUID;

/**
 *
 * @author klaas
 */
public class RootModule implements Module {
    private String _name;
    private RestrictedPath _path;
    
    public RootModule() {
        _name = UUID.randomUUID().toString().substring(0, 6);
        _path = new RestrictedPath(_name, FileSystems.getDefault().getPath("/"));
    }

    @Override
    public String name() {
        return _name;
    }

    @Override
    public String comment() {
        return "Everything from / down.";
    }

    @Override
    public RestrictedPath restrictedPath() { return _path; }

    @Override
    public boolean isReadable() { return true; }

    @Override
    public boolean isWritable() { return true; }
    
}
