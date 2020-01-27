/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ktsr42.yajsynclib;

import com.github.perlundq.yajsync.server.module.Module;
import com.github.perlundq.yajsync.server.module.RestrictedPath;

import java.nio.file.FileSystems;

/**
 *
 * @author klaas
 */
public class RootModule implements Module {
    private String _name;
    private RestrictedPath _path;
    
    public RootModule(String name, String basePath) {
        _name = name;
        _path = new RestrictedPath(_name, FileSystems.getDefault().getPath(basePath));
    }

    @Override
    public String name() {
        return _name;
    }

    @Override
    public String comment() {
        return "Everything reachable on this host down.";
    }

    @Override
    public RestrictedPath restrictedPath() { return _path; }

    @Override
    public boolean isReadable() { return true; }

    @Override
    public boolean isWritable() { return true; }
    
}
