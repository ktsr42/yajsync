/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ktsr42.yajsynclib;

import com.github.perlundq.yajsync.server.module.Module;
import com.github.perlundq.yajsync.server.module.ModuleException;
import com.github.perlundq.yajsync.server.module.Modules;
import java.util.LinkedList;

/**
 *
 * @author klaas
 */
public class OneModule implements Modules {
    
    private Module _module;
    
    public OneModule(Module module) { _module = module; }

    @Override
    public Module get(String moduleName) throws ModuleException {
        if(moduleName.equals(_module.name())) {
            return _module;
        } else {
            throw new ModuleException("Unknown module: " + moduleName);
        }
    }

    @Override
    public Iterable<Module> all() {
        LinkedList<Module> ml = new LinkedList<>();
        ml.add(_module);
        return ml;
    }
    
}
