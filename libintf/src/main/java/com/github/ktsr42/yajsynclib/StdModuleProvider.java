/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.ktsr42.yajsynclib;

import com.github.perlundq.yajsync.internal.util.Option;
import com.github.perlundq.yajsync.server.module.ModuleException;
import com.github.perlundq.yajsync.server.module.ModuleProvider;
import com.github.perlundq.yajsync.server.module.Modules;
import com.github.perlundq.yajsync.server.module.Module;
import java.net.InetAddress;
import java.security.Principal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

/**
 *
 * @author klaas
 */
public class StdModuleProvider extends ModuleProvider {
    private Module _rootModule;
    
    public StdModuleProvider(String name, String basePath, String password) {
        RootModule rootModule = new RootModule(name, basePath);
        if(Objects.isNull(password))
            _rootModule = new RootModule(name, basePath);
        else
            _rootModule = new ProtectedModule(rootModule, password);
    }

    @Override
    public Collection<Option> options() { return new LinkedList<Option>(); }

    @Override
    public void close() {}

    @Override
    public Modules newAuthenticated(InetAddress address, Principal principal) throws ModuleException {
        return newAnonymous(address);
    }

    @Override
    public Modules newAnonymous(InetAddress address) throws ModuleException {
        return new OneModule(_rootModule);
    }
    
}
