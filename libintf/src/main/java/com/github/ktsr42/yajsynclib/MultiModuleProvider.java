package com.github.ktsr42.yajsynclib;

import com.github.perlundq.yajsync.internal.util.Option;
import com.github.perlundq.yajsync.server.module.ModuleException;
import com.github.perlundq.yajsync.server.module.ModuleProvider;
import com.github.perlundq.yajsync.server.module.Modules;

import java.net.InetAddress;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

public class MultiModuleProvider extends ModuleProvider {
    MultiModules _modules;

    MultiModuleProvider(MultiModules mm) { _modules = mm; }
    @Override
    public Collection<Option> options() { return List.of(); }

    @Override
    public void close() {}

    @Override
    public Modules newAuthenticated(InetAddress address, Principal principal) throws ModuleException {
        return _modules;
    }

    @Override
    public Modules newAnonymous(InetAddress address) throws ModuleException {
        return newAuthenticated(address, null);
    }
}
